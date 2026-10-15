package com.chitsmart.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Inter font family
// Add Inter font files to app/src/main/res/font/
// inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf, inter_bold.ttf
val InterFontFamily = FontFamily(
    Font(com.chitsmart.R.font.inter_regular,  FontWeight.Normal),
    Font(com.chitsmart.R.font.inter_medium,   FontWeight.Medium),
    Font(com.chitsmart.R.font.inter_semibold, FontWeight.SemiBold),
    Font(com.chitsmart.R.font.inter_bold,     FontWeight.Bold),
)

// Typography scale — from google-stitch-master-prompt.md Design System
val ChitSmartTypography = Typography(
    // Heading 1 — 24 sp / Bold
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    // Heading 2 — 20 sp / SemiBold
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    // Heading 3 — 16 sp / SemiBold
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Body Large — 16 sp / Regular
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Body Small — 14 sp / Regular
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // Caption — 12 sp / Regular
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Label / Button — 14 sp / Medium
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
