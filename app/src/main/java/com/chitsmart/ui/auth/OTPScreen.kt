package com.chitsmart.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Colour tokens from Figma (node 1:115) ────────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyButton    = Color(0xFF002141)
private val TealCursor    = Color(0xFF0F766E)
private val TealEdit      = Color(0xFF0F766E)
private val SlateBody     = Color(0xFF43474F)
private val SlateHint     = Color(0xFF737780)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val ErrorBg       = Color(0xB3FFDAD6)  // rgba(255,218,214,0.7)
private val ErrorTitle    = Color(0xFF93000A)
private val ErrorBody     = Color(0xFFBA1A1A)
private val DigitFilled   = Color.White
private val DigitEmpty    = SurfaceBlue
private val DigitBorder   = Color(0x4DC3C6D0)   // rgba(195,198,208,0.3)

@Composable
fun OTPScreen(
    mobileNumber: String = "+91 98765 •••••",
    onVerified: (isNewUser: Boolean) -> Unit = {},
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorAttemptsLeft by remember { mutableIntStateOf(2) }
    var countdownSeconds by remember { mutableIntStateOf(45) }
    var canResend by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        }
        canResend = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        // ── Top navigation bar ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceBlue)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep, modifier = Modifier.size(15.dp))
            }

            // ChitSmart wordmark
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Text("Chit", fontSize = 16.sp, color = NavyButton, letterSpacing = (-0.4).sp)
                Text("Smart", fontSize = 16.sp, color = Color(0xFF4FDBC8), letterSpacing = (-0.4).sp)
            }

            // Help button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.HelpOutline, contentDescription = "Help", tint = NavyDeep, modifier = Modifier.size(17.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Title + mobile number ─────────────────────────────────────────────
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                "Verify your number",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                letterSpacing = (-0.6).sp,
                lineHeight = 32.sp,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Enter 6-digit code sent to", fontSize = 14.sp, color = SlateBody)
                Text(mobileNumber, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                Spacer(Modifier.width(2.dp))
                Row(
                    modifier = Modifier.clickable { onEdit() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TealEdit, letterSpacing = 0.14.sp)
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = TealEdit, modifier = Modifier.size(11.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── 6-digit OTP boxes ─────────────────────────────────────────────────
        OTPInputRow(
            otp = otp,
            hasError = showError,
            onOtpChange = { newOtp ->
                if (newOtp.length <= 6 && newOtp.all { it.isDigit() }) {
                    otp = newOtp
                    showError = false
                }
            },
        )

        Spacer(Modifier.height(16.dp))

        // ── Countdown / Resend ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Timer, contentDescription = null, tint = SlateBody, modifier = Modifier.size(13.dp))
                if (!canResend) {
                    Text(
                        buildAnnotatedText_Resend(countdownSeconds),
                        fontSize = 12.sp,
                        letterSpacing = 0.24.sp,
                    )
                } else {
                    Text("OTP expired", fontSize = 12.sp, color = ErrorBody)
                }
            }
            Text(
                "Resend SMS",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (canResend) TealEdit else SlateHint,
                letterSpacing = 0.14.sp,
                modifier = Modifier
                    .alpha(if (canResend) 1f else 0.6f)
                    .clickable(enabled = canResend) {
                        countdownSeconds = 45
                        canResend = false
                        otp = ""
                    },
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Error card ────────────────────────────────────────────────────────
        if (showError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorBg)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = ErrorTitle, modifier = Modifier.size(19.dp))
                Column {
                    Text(
                        "Wrong OTP entered",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorTitle,
                        letterSpacing = 0.14.sp,
                    )
                    Text(
                        "$errorAttemptsLeft verification attempts remaining before a 15-minute lockout.",
                        fontSize = 12.sp,
                        color = ErrorBody,
                        lineHeight = 16.sp,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Verify button ─────────────────────────────────────────────────────
        Button(
            onClick = {
                if (otp.length == 6) {
                    isLoading = true
                    // TODO: call viewModel.verifyOtp() — stubbed here
                    showError = otp != "123456"  // stub: only 123456 works in preview
                    if (showError) {
                        errorAttemptsLeft--
                        isLoading = false
                    }
                }
            },
            enabled = otp.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyButton,
                disabledContainerColor = Color(0xFF002141).copy(alpha = 0.5f),
            ),
            elevation = ButtonDefaults.buttonElevation(4.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Verify & Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Trust indicators ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceBlue)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TrustRow(icon = Icons.Outlined.Security, text = "256-Bit SSL encrypted OTP delivery")
            TrustRow(icon = Icons.Outlined.Lock, text = "OTP valid for 10 minutes only")
            TrustRow(icon = Icons.Outlined.VerifiedUser, text = "Rate-limited: 3 attempts per hour")
        }
    }
}

@Composable
private fun OTPInputRow(
    otp: String,
    hasError: Boolean,
    onOtpChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Hidden actual text field
        BasicTextField(
            value = TextFieldValue(otp, selection = TextRange(otp.length)),
            onValueChange = { onOtpChange(it.text) },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
        )

        // Visual digit boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (i in 0..5) {
                val char = otp.getOrNull(i)
                val isActive = otp.length == i
                val isFilled = char != null
                val isError = hasError

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isError && isFilled -> Color(0xFFFFDAD6).copy(alpha = 0.7f)
                                isFilled -> DigitFilled
                                else -> DigitEmpty
                            }
                        )
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = when {
                                isError && isFilled -> ErrorBody
                                isActive -> TealCursor
                                else -> DigitBorder
                            },
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isActive && !isFilled) {
                        // Blinking cursor
                        BlinkingCursor()
                    } else if (char != null) {
                        Text(
                            char.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) ErrorTitle else NavyButton,
                            letterSpacing = (-0.32).sp,
                        )
                    } else {
                        // Unfilled dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SlateHint.copy(alpha = 0.4f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val alpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(TealCursor.copy(alpha = alpha)),
    )
}

@Composable
private fun TrustRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0261A2), modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF43474F))
    }
}

// Build the "Resend OTP in 00:XX" annotated string
@Composable
private fun buildAnnotatedText_Resend(seconds: Int): androidx.compose.ui.text.AnnotatedString {
    val mm = (seconds / 60).toString().padStart(2, '0')
    val ss = (seconds % 60).toString().padStart(2, '0')
    return androidx.compose.ui.text.buildAnnotatedString {
        pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF43474F), fontSize = 12.sp, fontWeight = FontWeight.Medium))
        append("Resend OTP in ")
        pop()
        pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF041B3C), fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        append("$mm:$ss")
        pop()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700, name = "OTP Screen")
@Composable
fun OTPScreenPreview() {
    MaterialTheme { OTPScreen() }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700, name = "OTP Error State")
@Composable
fun OTPScreenErrorPreview() {
    MaterialTheme { OTPScreen() }
}
