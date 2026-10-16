package com.festive.santacall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.festive.santacall.ui.theme.*
import com.festive.santacall.util.SnowfallEffect
import com.festive.santacall.util.SoundManager
import kotlinx.coroutines.delay

class VoiceCallActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        soundManager.playSantaGreeting()

        val childName = intent.getStringExtra("CHILD_NAME") ?: "Bé Yêu"

        setContent {
            SantaCallTheme {
                VoiceCallScreen(
                    childName = childName,
                    onEndCall = {
                        soundManager.stopSantaGreeting()
                        finish()
                    },
                    onSwitchToVideo = {
                        soundManager.stopSantaGreeting()
                        val videoIntent = Intent(this@VoiceCallActivity, VideoCallActivity::class.java).apply {
                            putExtra("CHILD_NAME", childName)
                        }
                        startActivity(videoIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.releaseAll()
    }
}

@Composable
fun VoiceCallScreen(
    childName: String,
    onEndCall: () -> Unit,
    onSwitchToVideo: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    // Call duration timer
    var callSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            callSeconds++
        }
    }

    val formattedDuration = remember(callSeconds) {
        val m = callSeconds / 60
        val s = callSeconds % 60
        String.format("%02d:%02d", m, s)
    }

    // Audio wave pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "w1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a1"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E0707),
                        Color(0xFF0F1E15),
                        Color(0xFF070E0A)
                    )
                )
            )
    ) {
        SnowfallEffect(flakeCount = 25)

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
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = "Ông Già Noel 🎅",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formattedDuration,
                    color = ChristmasGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                if (childName.isNotBlank() && childName != "Bé Yêu") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Santa đang dặn dò: $childName",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
            }

            // Santa Avatar with audio wave rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Expanding wave 1
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(waveScale1)
                        .clip(CircleShape)
                        .background(ChristmasGold.copy(alpha = waveAlpha1))
                )

                // Avatar image
                Image(
                    painter = painterResource(id = R.drawable.santa_avatar),
                    contentDescription = "Santa Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .border(3.dp, ChristmasGold, CircleShape)
                )
            }

            // In-Call Grid Buttons (iOS / Android phone style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "Tắt tiếng",
                        isActive = isMuted,
                        onClick = { isMuted = !isMuted }
                    )
                    CallActionButton(
                        icon = Icons.Default.Videocam,
                        label = "Video Call",
                        isActive = false,
                        onClick = onSwitchToVideo
                    )
                    CallActionButton(
                        icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = "Loa ngoài",
                        isActive = isSpeakerOn,
                        onClick = { isSpeakerOn = !isSpeakerOn }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // End Call Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CallDeclineRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Kết thúc",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}
