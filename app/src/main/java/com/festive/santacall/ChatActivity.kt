package com.festive.santacall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.festive.santacall.model.ChatMessage
import com.festive.santacall.ui.theme.*
import com.festive.santacall.util.PreferencesHelper
import com.festive.santacall.util.SantaDialogueManager
import com.festive.santacall.util.SnowfallEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesHelper = PreferencesHelper(this)
        val profile = preferencesHelper.getChildProfile()

        setContent {
            SantaCallTheme {
                ChatScreen(
                    preferencesHelper = preferencesHelper,
                    childName = profile.name,
                    wishlist = profile.wishlist,
                    onBack = { finish() },
                    onStartVideoCall = {
                        val intent = Intent(this, VideoCallActivity::class.java).apply {
                            putExtra("CHILD_NAME", profile.name)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    preferencesHelper: PreferencesHelper,
    childName: String,
    wishlist: String,
    onBack: () -> Unit,
    onStartVideoCall: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isSantaTyping by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Load saved conversation from persistent storage or initialize with Santa greeting
    val messages = remember {
        val saved = preferencesHelper.getChatMessages()
        val initialList = if (saved.isNotEmpty()) {
            saved
        } else {
            val defaults = listOf(
                ChatMessage(
                    sender = "Santa",
                    text = "Ho Ho Ho! 🎅 Chào cháu $childName yêu quý! Ta đang kiểm tra lại danh sách quà tại Bắc Cực đây!",
                    isFromSanta = true
                ),
                ChatMessage(
                    sender = "Santa",
                    text = "Năm nay cháu đã làm được nhiều việc tốt chưa nào? Cháu muốn xin món quà gì trong đêm Noel?",
                    isFromSanta = true
                )
            )
            preferencesHelper.saveChatMessages(defaults)
            defaults
        }
        mutableStateListOf<ChatMessage>().apply { addAll(initialList) }
    }

    // Auto scroll to latest message when screen opens
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            delay(150)
            listState.scrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Cháu muốn xin quà Noel! 🎁",
        "Bắc Cực có lạnh lắm không ạ? ❄️",
        "Tuần lộc Rudolph đang làm gì? 🦌",
        "Cháu có nằm trong danh sách Bé Ngoan không? ⭐",
        "Làm sao để gặp Santa đêm Noel? 🎄"
    )

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            sender = childName,
            text = text,
            isFromSanta = false
        )
        messages.add(userMsg)
        preferencesHelper.saveChatMessages(messages.toList())
        inputText = ""

        coroutineScope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)

            // Santa typing delay
            isSantaTyping = true
            delay(1200)
            isSantaTyping = false

            val reply = generateSantaReply(text, childName, wishlist)
            val santaMsg = ChatMessage(
                sender = "Santa",
                text = reply,
                isFromSanta = true
            )
            messages.add(santaMsg)
            preferencesHelper.saveChatMessages(messages.toList())
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Confirmation dialog to clear/reset chat history
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Làm mới cuộc trò chuyện? 🎅",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Lịch sử tin nhắn sẽ được xóa và Santa sẽ bắt đầu cuộc trò chuyện mới cùng cháu.",
                    color = Color.White.copy(alpha = 0.85f)
                )
            },
            containerColor = ChristmasSurface,
            confirmButton = {
                Button(
                    onClick = {
                        preferencesHelper.clearChatHistory()
                        messages.clear()
                        val resetList = listOf(
                            ChatMessage(
                                sender = "Santa",
                                text = "Ho Ho Ho! 🎅 Chào cháu $childName yêu quý! Ta đang kiểm tra lại danh sách quà tại Bắc Cực đây!",
                                isFromSanta = true
                            ),
                            ChatMessage(
                                sender = "Santa",
                                text = "Năm nay cháu đã làm được nhiều việc tốt chưa nào? Cháu muốn xin món quà gì trong đêm Noel?",
                                isFromSanta = true
                            )
                        )
                        messages.addAll(resetList)
                        preferencesHelper.saveChatMessages(resetList)
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChristmasRed)
                ) {
                    Text("Xóa & Bắt đầu lại")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Hủy", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.santa_avatar),
                            contentDescription = "Santa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, ChristmasGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ông Già Noel 🎅",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSantaTyping) "Đang soạn tin nhắn..." else "Trực tuyến tại Bắc Cực 🟢",
                                color = if (isSantaTyping) ChristmasGold else Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Làm mới chat",
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    IconButton(onClick = onStartVideoCall) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Gọi Video",
                            tint = ChristmasGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChristmasDarkRed
                )
            )
        },
        containerColor = ChristmasDarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SnowfallEffect(flakeCount = 20)

            Column(modifier = Modifier.fillMaxSize()) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(message = msg)
                    }

                    if (isSantaTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }

                // Quick Prompt Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQuestions) { question ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ChristmasCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChristmasGreen),
                            modifier = Modifier.clickable { sendUserMessage(question) }
                        ) {
                            Text(
                                text = question,
                                color = ChristmasCream,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Bottom Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChristmasSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Nhắn tin gửi Santa...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E382B),
                            unfocusedContainerColor = Color(0xFF1E382B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ChristmasGold,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 100.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendUserMessage(inputText) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ChristmasRed)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isSanta = message.isFromSanta
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSanta) Arrangement.Start else Arrangement.End
    ) {
        if (isSanta) {
            Image(
                painter = painterResource(id = R.drawable.santa_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isSanta) Alignment.Start else Alignment.End) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isSanta) 2.dp else 16.dp,
                    bottomEnd = if (isSanta) 16.dp else 2.dp
                ),
                color = if (isSanta) Color(0xFF7A1515) else Color(0xFF1B5E20),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedTime,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF7A1515).copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🎅 Santa đang gõ tin nhắn...",
            color = ChristmasGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun generateSantaReply(input: String, childName: String, wishlist: String): String {
    return SantaDialogueManager.getSantaReply(input, childName, wishlist)
}
