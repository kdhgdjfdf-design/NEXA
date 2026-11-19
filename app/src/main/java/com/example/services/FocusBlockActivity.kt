package com.example.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.MainActivity
import com.example.NexaApplication
import com.example.R
import com.example.domain.i18n.NexaLang
import com.example.domain.i18n.NexaStrings
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Full-screen customizable Focus Guard block screen with:
 * - Custom user image or built-in tranquil hourglass illustration
 * - Block reason & remaining time
 * - BackHandler routing back to NEXA / Launcher Home instead of the blocked app
 * - Clean Emergency Exit handling for FOCUS, PRAYER, TASK, and DAILY_LIMIT reasons
 */
class FocusBlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NexaApplication
        val blockedPkg = intent.getStringExtra("blocked_pkg") ?: ""
        val blockedLabel = intent.getStringExtra("blocked_label") ?: "App"
        val reason = intent.getStringExtra("block_reason") ?: "FOCUS"

        fun returnToStudySafely() {
            val studyIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(studyIntent)
            finish()
        }

        fun endBlockEarlyAndExit() {
            if (reason == "FOCUS") {
                app.container.focusGuardManager.completeSession(autoFinished = false)
            } else {
                val graceMs = if (reason == "PRAYER") 20 * 60_000L else 10 * 60_000L
                app.container.focusGuardManager.grantTemporaryBypass(blockedPkg, reason, graceMs)
            }
            finish()
        }

        setContent {
            val prefs by app.container.prefs.state.collectAsState(initial = null)
            val profiles by app.container.db.designDao().observeProfiles().collectAsState(initial = emptyList())
            val timerState by app.container.focusGuardManager.timerState.collectAsState()

            val activeProfile = profiles.firstOrNull { it.id == prefs?.activeProfileId } ?: profiles.firstOrNull()
            val customImageUri = activeProfile?.focusBlockImageUri.orEmpty()
            val lang = NexaLang.fromCode(prefs?.language ?: "ar")
            val strings = NexaStrings.forLang(lang)

            var showConfirmDialog by remember { mutableStateOf(false) }
            var undoSeconds by remember { mutableIntStateOf(0) }

            BackHandler(enabled = true) {
                returnToStudySafely()
            }

            LaunchedEffect(undoSeconds) {
                if (undoSeconds > 0) {
                    delay(1000L)
                    undoSeconds--
                    if (undoSeconds == 0) {
                        endBlockEarlyAndExit()
                    }
                }
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF171310)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (customImageUri.isNotBlank()) {
                            AsyncImage(
                                model = Uri.parse(customImageUri),
                                contentDescription = strings.focusGuard,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_focus_block),
                                contentDescription = strings.focusGuard,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xBB171310),
                                            Color(0xE0120E0C)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .systemBarsPadding()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFF281F1A),
                                    border = BorderStroke(1.dp, Color(0xFFE7C7A3).copy(alpha = 0.45f)),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFE7C7A3),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = strings.focusGuard,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0x88241A15),
                                    border = BorderStroke(1.dp, Color(0xFFE7C7A3).copy(alpha = 0.42f))
                                ) {
                                    Text(
                                        text = "$blockedLabel • ${strings.blockReasonLabel(reason)}",
                                        color = Color(0xFFF3DEC6),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xB31D1511),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                shadowElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val rem = timerState.remainingSeconds
                                    val mm = rem / 60
                                    val ss = rem % 60
                                    Text(
                                        text = if (rem > 0) String.format(Locale.US, "%02d:%02d", mm, ss) else strings.enabledLabel,
                                        style = MaterialTheme.typography.displayMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val defaultMsg = when (lang) {
                                        NexaLang.AR -> "احفظ وقتك وواصل تركيزك بهدوء واطمئنان."
                                        NexaLang.FR -> "Protégez votre temps et gardez votre concentration."
                                        NexaLang.EN -> "Stay present and protect your calm focus."
                                    }
                                    Text(
                                        text = prefs?.focusGuardCustomMessage?.takeIf { it.isNotBlank() } ?: defaultMsg,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFFEDE2D6),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (undoSeconds > 0) {
                                    Surface(
                                        color = Color(0xFF7A2E22),
                                        shape = RoundedCornerShape(18.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.45f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${strings.undoGraceTitle} (${undoSeconds}s)",
                                                color = Color.White
                                            )
                                            TextButton(onClick = { undoSeconds = 0 }) {
                                                Text(strings.cancel, color = Color(0xFFFFE082))
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { returnToStudySafely() },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF171310)
                                    )
                                ) {
                                    Text(strings.returnHomeLabel, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showConfirmDialog = true },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(strings.finishFocus, color = Color.White)
                                }

                                TextButton(
                                    onClick = { endBlockEarlyAndExit() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Color(0xFFFFB74D))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.emergencyExit, color = Color(0xFFFFB74D))
                                }
                            }
                        }

                        if (showConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = { showConfirmDialog = false },
                                title = { Text(strings.confirmEarlyExit) },
                                text = { Text(strings.undoGraceTitle) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showConfirmDialog = false
                                        undoSeconds = 5
                                    }) {
                                        Text(strings.confirm)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmDialog = false }) {
                                        Text(strings.cancel)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
