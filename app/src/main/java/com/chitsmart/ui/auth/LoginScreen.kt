package com.chitsmart.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Colour tokens from Figma (node 1:2) ──────────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyButton    = Color(0xFF002141)
private val TealAccent    = Color(0xFF00B3A1)
private val BlueAccent    = Color(0xFF0261A2)
private val SlateBody     = Color(0xFF43474F)
private val SlateHint     = Color(0xFF737780)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val InputPill     = Color(0xFFE8EDFF)
private val BadgeBlue     = Color(0xFFE0E8FF)
private val CardWhite     = Color.White
private val BorderColor   = Color(0xFFE0E8FF)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onOtpSent: (mobileNumber: String) -> Unit,
    onRegister: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mobile by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("EN") }

    // Navigate when OTP sent successfully
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.OtpSent) {
            onOtpSent("+91$mobile")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Top bar: Logo + Language toggle ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ChitSmart logo placeholder (36dp circle)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("CS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                }

                // Language toggle pill
                Row(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(100.dp))
                        .clip(RoundedCornerShape(100.dp))
                        .background(InputPill)
                        .padding(4.dp),
                ) {
                    listOf("EN", "हिंदी").forEach { lang ->
                        val selected = language == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) NavyButton else Color.Transparent)
                                .clickable { language = lang }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text(
                                lang,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) Color.White else SlateBody,
                                letterSpacing = 0.33.sp,
                            )
                        }
                    }
                }
            }

            // ── Community wealth circle banner ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBlue)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(BadgeBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Savings,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        "COMMUNITY WEALTH CIRCLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueAccent,
                        letterSpacing = 0.275.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Empowering 40,000+ members across verified rotational savings.",
                        fontSize = 12.sp,
                        color = SlateBody,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Header ────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Enter your mobile number",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    letterSpacing = (-0.6).sp,
                    lineHeight = 32.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "We'll send you a 6-digit verification code to get started.",
                    fontSize = 14.sp,
                    color = SlateBody,
                    lineHeight = 22.sp,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Phone input card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardWhite)
                    .padding(16.dp),
            ) {
                // Field label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Mobile Phone Number",
                        fontSize = 12.sp,
                        color = SlateBody,
                        letterSpacing = 0.24.sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Security,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            "100% Secure",
                            fontSize = 11.sp,
                            color = TealAccent,
                            letterSpacing = 0.33.sp,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Input row: +91 pill + number field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // +91 prefix
                    Row(
                        modifier = Modifier
                            .background(InputPill)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Indian flag (emoji or SVG — using emoji for simplicity)
                        Text("🇮🇳", fontSize = 14.sp)
                        Text(
                            "+91",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDeep,
                        )
                        Icon(
                            Icons.Outlined.ArrowDropDown,
                            contentDescription = null,
                            tint = SlateBody,
                            modifier = Modifier.size(14.dp),
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(InputPill),
                    )

                    // Number input
                    BasicTextField(
                        value = mobile,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) mobile = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = NavyDeep,
                            letterSpacing = 0.4.sp,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(TealAccent),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (mobile.isEmpty()) {
                                Text("98765 43210", fontSize = 16.sp, color = SlateHint, letterSpacing = 0.4.sp)
                            }
                            inner()
                        },
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Validation helper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = SlateHint,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        "A valid 10-digit Indian SIM number is required",
                        fontSize = 11.sp,
                        color = SlateHint,
                        letterSpacing = 0.33.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Send OTP button
                val isLoading = uiState is LoginUiState.Loading
                val isEnabled = mobile.length == 10 && !isLoading

                Button(
                    onClick = { viewModel.sendOtp("+91$mobile") },
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyButton,
                        disabledContainerColor = NavyButton.copy(alpha = 0.5f),
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "Send OTP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Register link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("New to ChitSmart?", fontSize = 14.sp, color = SlateBody)
                    Spacer(Modifier.width(6.dp))
                    Row(
                        modifier = Modifier.clickable { onRegister() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Register",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealAccent,
                        )
                        Icon(
                            Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Institutional protection badge ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBlue)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BadgeBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Security,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Column {
                    Text(
                        "Institutional Grade Protection",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDeep,
                        letterSpacing = 0.24.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Your data is protected under RBI Chit Fund Regulations and central digital grievance safeguards.",
                        fontSize = 12.sp,
                        color = SlateBody,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Social proof ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Overlapping avatars (placeholder)
                Box(modifier = Modifier.width(64.dp).height(24.dp)) {
                    listOf(0, 8, 16).forEachIndexed { i, offset ->
                        Box(
                            modifier = Modifier
                                .offset(x = (i * 16).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800))[i])
                                .border(2.dp, Color.White, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Joined by 1,420+ groups this month",
                    fontSize = 12.sp,
                    color = SlateBody,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Terms & SSL footer ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateHint, fontSize = 12.sp)) { append("By proceeding, you agree to our ") }
                        withStyle(SpanStyle(color = BlueAccent, fontSize = 12.sp, textDecoration = TextDecoration.Underline)) { append("Terms of Service") }
                        withStyle(SpanStyle(color = SlateHint, fontSize = 12.sp)) { append(" & ") }
                        withStyle(SpanStyle(color = BlueAccent, fontSize = 12.sp, textDecoration = TextDecoration.Underline)) { append("Privacy Policy") }
                        withStyle(SpanStyle(color = SlateHint, fontSize = 12.sp)) { append(".") }
                    },
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = SlateHint,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(
                        "256-Bit SSL Encrypted End-to-End",
                        fontSize = 11.sp,
                        color = SlateHint.copy(alpha = 0.8f),
                        letterSpacing = 0.33.sp,
                    )
                }
            }
        }
    }
}

// ── UI state ──────────────────────────────────────────────────────────────────
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        // Preview with stub
        Box(Modifier.fillMaxSize().background(PageBg)) {
            Text("Preview — use LoginScreen composable", Modifier.align(Alignment.Center))
        }
    }
}
