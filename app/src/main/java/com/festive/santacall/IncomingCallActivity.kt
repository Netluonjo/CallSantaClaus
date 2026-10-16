package com.festive.santacall

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.festive.santacall.ui.theme.*
import com.festive.santacall.util.PreferencesHelper
import com.festive.santacall.util.SnowfallEffect
import com.festive.santacall.util.SoundManager

class IncomingCallActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlock()

        soundManager = SoundManager(this)
        soundManager.startRingtoneAndVibration()

        val callType = intent.getStringExtra("CALL_TYPE") ?: "VIDEO"
        val preferencesHelper = PreferencesHelper(this)
        val profile = preferencesHelper.getChildProfile()

        setContent {
            SantaCallTheme {
                IncomingCallScreen(
                    callType = callType,
                    childName = profile.name,
                    onAccept = {
                        soundManager.stopRingtoneAndVibration()
                        val targetClass = if (callType == "VIDEO") VideoCallActivity::class.java else VoiceCallActivity::class.java
                        val callIntent = Intent(this@IncomingCallActivity, targetClass).apply {
                            putExtra("CHILD_NAME", profile.name)
                        }
                        startActivity(callIntent)
                        finish()
                    },
                    onDecline = {
                        soundManager.stopRingtoneAndVibration()
                        finish()
                    }
                )
            }
        }
    }

    private fun wakeAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.releaseAll()
    }
}

@Composable
fun IncomingCallScreen(
    callType: String,
    childName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C0A0A),
                        Color(0xFF0D1E15),
                        Color(0xFF050B08)
                    )
                )
            )
    ) {
        // Falling snowflakes in background
        SnowfallEffect(flakeCount = 35)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ChristmasRed.copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = null,
                            tint = ChristmasGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (callType == "VIDEO") "Cuộc gọi Video đến..." else "Cuộc gọi Thoại đến...",
                            color = ChristmasGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Ông Già Noel 🎅",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Xưởng Quà Bắc Cực • +1 (800) SANTA-POLE",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                if (childName.isNotBlank() && childName != "Bé Yêu") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Santa đang tìm gặp: $childName",
                        color = ChristmasGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Santa Avatar with glowing pulse
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Pulsing outer glow ring
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(ChristmasGold.copy(alpha = 0.15f))
                )

                // Avatar image
                Image(
                    painter = painterResource(id = R.drawable.santa_avatar),
                    contentDescription = "Santa Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .border(3.dp, ChristmasGold, CircleShape)
                )
            }

            // Accept & Decline Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline Button (Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CallDeclineRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Từ chối",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Từ chối",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                // Accept Button (Green)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(CallAcceptGreen)
                    ) {
                        Icon(
                            imageVector = if (callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Nghe máy",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nghe máy",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
