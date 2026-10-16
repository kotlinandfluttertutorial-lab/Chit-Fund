package com.chitsmart.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Colour tokens from Figma (node 1:1415) ───────────────────────────────────
private val SplashBg = Color(0xFF073763)
private val TealAccent = Color(0xFF4FDBC8)
private val TealGlow = Color(0xFF71F8E4)
private val BlueGlow = Color(0xFF76B8FE)
private val EmblemDark = Color(0xFF002141)
private val TextLight = Color(0xFFE0E8FF)
private val TextSubtle = Color(0x99E0E8FF)
private val BarTrack = Color(0xFF002141)

@Composable
fun SplashScreen(onNavigate: (isFirstLaunch: Boolean) -> Unit) {
    // Animated progress
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1800, easing = EaseInOut),
        label = "loadingProgress",
    )

    // Pulsing aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "auraScale",
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(2200)
        onNavigate(true) // stub — real impl reads DataStore
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        // Decorative ambient glows
        Box(
            modifier =
                Modifier
                    .size(384.dp)
                    .offset(x = (-128).dp, y = (-128).dp)
                    .clip(CircleShape)
                    .background(Color(0x1A76B8FE))
                    .blur(32.dp)
                    .align(Alignment.TopStart),
        )
        Box(
            modifier =
                Modifier
                    .size(420.dp)
                    .clip(CircleShape)
                    .background(Color(0x1A4FDBC8))
                    .blur(50.dp)
                    .align(Alignment.Center),
        )
        Box(
            modifier =
                Modifier
                    .size(320.dp)
                    .offset(x = 96.dp, y = 96.dp)
                    .clip(CircleShape)
                    .background(Color(0x1A71F8E4))
                    .blur(32.dp)
                    .align(Alignment.BottomEnd),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top spacer row (decorative icon at 40% opacity)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Icon(
                    Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = TealAccent.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp),
                )
            }

            // ── Central hero branding ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Emblem with pulsing aura
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Outer pulsing aura
                    Box(
                        modifier =
                            Modifier
                                .size((96 * auraScale).dp)
                                .clip(CircleShape)
                                .background(TealAccent.copy(alpha = 0.2f)),
                    )
                    // Inner gradient circle (emblem)
                    Box(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(EmblemDark, SplashBg),
                                    ),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Lotus/coin placeholder icon
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("₹", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TealAccent)
                        }
                    }
                }

                // Wordmark: "Chit" + "Smart" (teal)
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp),
                        ) { append("Chit") }
                        withStyle(
                            SpanStyle(color = TealAccent, fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp),
                        ) { append("Smart") }
                    },
                    textAlign = TextAlign.Center,
                )

                // Tagline
                Text(
                    "Your trusted chit fund partner",
                    fontSize = 14.sp,
                    color = Color(0xD9E0E8FF),
                    letterSpacing = 0.35.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // Regulatory badge
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF002141).copy(alpha = 0.7f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = TextLight, modifier = Modifier.size(12.dp))
                    Text(
                        "GOVT. REG. & RBI CHIT FUNDS ACT, 1982",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight,
                        letterSpacing = 0.55.sp,
                    )
                }
            }

            // ── Bottom loading section ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Progress bar
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BarTrack),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(TealAccent, TealGlow, BlueGlow),
                                    ),
                                ),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Securing ledger session...",
                        fontSize = 11.sp,
                        color = Color(0xFFD7E2FF),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.275.sp,
                    )
                    Text("100%", fontSize = 11.sp, color = TealAccent, fontWeight = FontWeight.SemiBold, letterSpacing = 0.33.sp)
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(9.dp))
                        Text(
                            "100% Encrypted & Sovereign Trust",
                            fontSize = 11.sp,
                            color = TextSubtle,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.33.sp,
                        )
                    }
                    Text(
                        "v2.4.0 (Build 8402)",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840, name = "Splash Screen")
@Composable
fun SplashScreenPreview() {
    MaterialTheme { SplashScreen(onNavigate = {}) }
}
