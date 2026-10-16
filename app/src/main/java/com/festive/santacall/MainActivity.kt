package com.festive.santacall

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.festive.santacall.model.ChildProfile
import com.festive.santacall.receiver.CallAlarmReceiver
import com.festive.santacall.ui.theme.*
import com.festive.santacall.util.PreferencesHelper
import com.festive.santacall.util.SnowfallEffect
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesHelper = PreferencesHelper(this)

        setContent {
            SantaCallTheme {
                MainDashboardScreen(
                    preferencesHelper = preferencesHelper,
                    onStartInstantCall = { callType ->
                        val intent = Intent(this, IncomingCallActivity::class.java).apply {
                            putExtra("CALL_TYPE", callType)
                        }
                        startActivity(intent)
                    },
                    onOpenChat = {
                        val intent = Intent(this, ChatActivity::class.java)
                        startActivity(intent)
                    },
                    onScheduleCall = { delaySeconds, callType ->
                        scheduleCall(delaySeconds, callType)
                    }
                )
            }
        }
    }

    private fun scheduleCall(seconds: Int, callType: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, CallAlarmReceiver::class.java).apply {
            putExtra("CALL_TYPE", callType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val triggerTime = SystemClock.elapsedRealtime() + (seconds * 1000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent)
        }

        Toast.makeText(
            this,
            "🎅 Đã lên lịch! Santa sẽ gọi sau $seconds giây!",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun MainDashboardScreen(
    preferencesHelper: PreferencesHelper,
    onStartInstantCall: (String) -> Unit,
    onOpenChat: () -> Unit,
    onScheduleCall: (Int, String) -> Unit
) {
    val context = LocalContext.current

    // Request necessary permissions smoothly
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val neededPerms = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            neededPerms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = neededPerms.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            permissionsLauncher.launch(notGranted.toTypedArray())
        }
    }

    var profile by remember { mutableStateOf(preferencesHelper.getChildProfile()) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }
    var selectedCallTypeForSchedule by remember { mutableStateOf("VIDEO") }

    // Christmas countdown ticker
    var countdownText by remember { mutableStateOf("...") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val christmas = Calendar.getInstance().apply {
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 24)
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) {
                    add(Calendar.YEAR, 1)
                }
            }
            val diffMs = christmas.timeInMillis - now.timeInMillis
            val days = diffMs / (1000 * 60 * 60 * 24)
            val hours = (diffMs / (1000 * 60 * 60)) % 24
            val minutes = (diffMs / (1000 * 60)) % 60
            val seconds = (diffMs / 1000) % 60
            countdownText = "${days} ngày ${hours}h ${minutes}m ${seconds}s"
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChristmasDarkBg)
    ) {
        // High-performance falling snow
        SnowfallEffect(flakeCount = 35)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // 1. Header Banner matching exactly the App Store screenshot
            item {
                AppStoreHeaderBanner(
                    onShareClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Tải ngay ứng dụng Mô Phỏng Trò chuyện Với Santa để gọi video và trò chuyện cùng Ông Già Noel! 🎅🎄")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ứng dụng"))
                    }
                )
            }

            // 2. Christmas Countdown Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E382B),
                    border = BorderStroke(1.dp, ChristmasGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎄", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Đếm ngược đến Đêm Giáng Sinh 24/12",
                                color = ChristmasGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = countdownText,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 3. Child Personalization Quick Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ChristmasSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { showProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎁", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Đang đặt tên bé: ${profile.name}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Santa sẽ gọi đúng tên bé trong cuộc gọi",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = "Đổi tên ✏️",
                            color = ChristmasGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 4. Feature Cards
            item {
                Text(
                    text = "Tính năng chính",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Video Call Card
            item {
                MainActionCard(
                    title = "Cuộc gọi Video với Santa",
                    subtitle = "FaceTime trực tiếp với Ông Già Noel tại xưởng quà",
                    icon = Icons.Default.Videocam,
                    iconBgColor = ChristmasRed,
                    accentColor = ChristmasRed,
                    onInstantClick = { onStartInstantCall("VIDEO") },
                    onScheduleClick = {
                        selectedCallTypeForSchedule = "VIDEO"
                        showScheduleDialog = true
                    }
                )
            }

            // Voice Call Card
            item {
                MainActionCard(
                    title = "Cuộc gọi Thoại với Santa",
                    subtitle = "Chuông điện thoại reo thật, nghe giọng Santa chúc mừng",
                    icon = Icons.Default.Call,
                    iconBgColor = ChristmasGreen,
                    accentColor = ChristmasGreen,
                    onInstantClick = { onStartInstantCall("VOICE") },
                    onScheduleClick = {
                        selectedCallTypeForSchedule = "VOICE"
                        showScheduleDialog = true
                    }
                )
            }

            // Chat with Santa Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ChristmasSurface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenChat() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trò chuyện cùng Santa",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Nhắn tin gửi danh sách quà Noel cho Santa",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // In-App Purchase VIP Mockup Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF261D10),
                    border = BorderStroke(1.dp, ChristmasGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { showVipDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mở Khóa Gói VIP Santa",
                                    color = ChristmasGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = ChristmasGold,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gọi video 4K không giới hạn • Xóa toàn bộ quảng cáo",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Schedule Dialog
        if (showScheduleDialog) {
            ScheduleCallDialog(
                callType = selectedCallTypeForSchedule,
                onDismiss = { showScheduleDialog = false },
                onConfirm = { delaySeconds ->
                    showScheduleDialog = false
                    onScheduleCall(delaySeconds, selectedCallTypeForSchedule)
                }
            )
        }

        // Profile Dialog
        if (showProfileDialog) {
            ChildProfileDialog(
                currentProfile = profile,
                onDismiss = { showProfileDialog = false },
                onSave = { updated ->
                    profile = updated
                    preferencesHelper.saveChildProfile(updated)
                    showProfileDialog = false
                    Toast.makeText(context, "Đã lưu hồ sơ bé: ${updated.name}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // VIP In-App Dialog
        if (showVipDialog) {
            VipPurchaseDialog(
                onDismiss = { showVipDialog = false },
                onPurchase = {
                    showVipDialog = false
                    Toast.makeText(context, "🎉 Cảm ơn bạn! Đã mở khóa thành công Gói VIP Santa!", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun AppStoreHeaderBanner(
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6B4226),
                        Color(0xFF4A4B2F),
                        Color(0xFF0F4327),
                        Color(0xFF0A301B)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon matching screenshot
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.santa_icon),
                    contentDescription = "App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Call and Video Buttons in bottom corner like screenshot
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CallDeclineRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CallAcceptGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata text matching screenshot
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mô Phỏng Trò chuyện Với Santa",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Chat, gọi video với Santa",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chỉ dành cho điện thoại\nMiễn phí · Mua in-app",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.clickable { onShareClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chia sẻ",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    accentColor: Color,
    onInstantClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ChristmasSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onInstantClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Gọi Ngay 📞", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onScheduleClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ChristmasGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Hẹn Giờ ⏰", color = ChristmasGold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ScheduleCallDialog(
    callType: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val delays = listOf(
        Pair("10 Giây (Thử nghiệm)", 10),
        Pair("30 Giây", 30),
        Pair("1 Phút", 60),
        Pair("5 Phút", 300),
        Pair("15 Phút", 900)
    )
    var selectedSeconds by remember { mutableIntStateOf(10) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChristmasSurface,
            border = BorderStroke(1.5.dp, ChristmasGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "⏰ Hẹn Giờ Cuộc Gọi Santa",
                    color = ChristmasGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Điện thoại sẽ tự động đổ chuông và hiển thị màn hình cuộc gọi ${if (callType == "VIDEO") "Video" else "Thoại"} sau khoảng thời gian bạn chọn.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                delays.forEach { (label, sec) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSeconds == sec) ChristmasRed.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { selectedSeconds = sec }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeconds == sec,
                            onClick = { selectedSeconds = sec },
                            colors = RadioButtonDefaults.colors(selectedColor = ChristmasGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Hủy", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedSeconds) },
                        colors = ButtonDefaults.buttonColors(containerColor = ChristmasRed)
                    ) {
                        Text(text = "Bắt Đầu Hẹn Giờ")
                    }
                }
            }
        }
    }
}

@Composable
fun ChildProfileDialog(
    currentProfile: ChildProfile,
    onDismiss: () -> Unit,
    onSave: (ChildProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var wishlist by remember { mutableStateOf(currentProfile.wishlist) }
    var behavior by remember { mutableStateOf(currentProfile.behaviorStatus) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChristmasSurface,
            border = BorderStroke(1.5.dp, ChristmasRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🎁 Hồ Sơ Của Bé",
                    color = ChristmasGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên của bé (Ví dụ: Bé Bo, Minh Anh)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChristmasGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = ChristmasGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = wishlist,
                    onValueChange = { wishlist = it },
                    label = { Text("Món quà bé ước (Đồ chơi, xe đua...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChristmasGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = ChristmasGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = behavior,
                    onValueChange = { behavior = it },
                    label = { Text("Lời dặn của Santa (Chăm ăn rau, ngủ sớm...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChristmasGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = ChristmasGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Đóng", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                currentProfile.copy(
                                    name = name.ifBlank { "Bé Yêu" },
                                    wishlist = wishlist.ifBlank { "Món quà bất ngờ" },
                                    behaviorStatus = behavior
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChristmasGreen)
                    ) {
                        Text(text = "Lưu Thông Tin")
                    }
                }
            }
        }
    }
}

@Composable
fun VipPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E170A),
            border = BorderStroke(2.dp, ChristmasGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎅👑", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gói VIP Santa Siêu Cấp",
                    color = ChristmasGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trải nghiệm Giáng Sinh hoàn hảo cho bé yêu",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val benefits = listOf(
                    "✨ Cuộc gọi Video 4K không giới hạn",
                    "✨ Santa gọi đúng tên riêng của từng bé",
                    "✨ Xóa bỏ hoàn toàn mọi quảng cáo",
                    "✨ Nhận giấy chứng nhận Bé Ngoan có chữ ký Santa"
                )

                benefits.forEach { b ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = b, color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onPurchase,
                    colors = ButtonDefaults.buttonColors(containerColor = ChristmasWarmGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mở Khóa Ngay • 49.000 đ",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(text = "Để sau", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}
