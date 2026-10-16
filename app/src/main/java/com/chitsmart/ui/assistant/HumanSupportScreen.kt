package com.chitsmart.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg      = Color(0xFFF9F9FF)
private val NavyDeep    = Color(0xFF041B3C)
private val NavyMid     = Color(0xFF002141)
private val TealAccent  = Color(0xFF00B3A1)
private val BlueAccent  = Color(0xFF0261A2)
private val SlateBody   = Color(0xFF43474F)
private val SlateHint   = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HumanSupportScreen(
    onBack: () -> Unit = {},
    onEmailSupport: () -> Unit = {},
) {
    // Phase: 0 = connecting animation, 1 = unavailability card
    var phase by remember { mutableStateOf(0) }
    var shareTranscript by remember { mutableStateOf(false) }

    // Auto-advance from connecting to unavailability after 2 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        phase = 1
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep)
                    }
                },
                title = {
                    Column {
                        Text("Human Support", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("Connect with our support team", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (phase) {
                0 -> {
                    // Connecting animation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = TealAccent,
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 4.dp,
                            )
                            Box(
                                modifier = Modifier.size(52.dp).clip(CircleShape).background(SurfaceBlue),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.SupportAgent, contentDescription = null, tint = NavyMid, modifier = Modifier.size(26.dp))
                            }
                        }
                        Text("Connecting to support…", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, textAlign = TextAlign.Center)
                        Text("Please wait while we connect you with a support agent.", fontSize = 13.sp, color = SlateBody, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    }
                }
                else -> {
                    // Unavailability card
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFFF3CD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(40.dp))
                        }

                        // Heading
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Support Agents Unavailable", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyDeep, textAlign = TextAlign.Center)
                            Text(
                                "Our human support agents are currently offline. Support is available Monday–Saturday, 9 AM – 6 PM IST.",
                                fontSize = 14.sp,
                                color = SlateBody,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                            )
                        }

                        // Email CTA card
                        Column(
                            modifier = Modifier.fillMaxWidth().background(SurfaceBlue, RoundedCornerShape(12.dp)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(20.dp))
                                Text("Email Support", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                            }
                            Text("We'll respond within 24 business hours.", fontSize = 12.sp, color = SlateBody)
                            Button(
                                onClick = onEmailSupport,
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Email us at support@chitsmart.in", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Share transcript toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Share AI Transcript", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                                Text("Attach your AI chat history to the email for context.", fontSize = 12.sp, color = SlateBody)
                            }
                            Switch(
                                checked = shareTranscript,
                                onCheckedChange = { shareTranscript = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = TealAccent),
                            )
                        }

                        // AI assistant CTA
                        Column(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFE6FAF3), RoundedCornerShape(12.dp)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                                Text("Try ChitSmart AI first", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                            }
                            Text("Our AI assistant can instantly answer most questions about your account and chit fund policies.", fontSize = 12.sp, color = SlateBody, lineHeight = 18.sp)
                        }

                        // Go Back button
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid),
                        ) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Go Back", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Human Support")
@Composable
fun HumanSupportScreenPreview() { MaterialTheme { HumanSupportScreen() } }
