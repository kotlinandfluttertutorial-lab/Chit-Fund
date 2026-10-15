package com.chitsmart.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Colour tokens from Figma (node 1:1468) ───────────────────────────────────
private val PageBg         = Color(0xFFF9F9FF)
private val IllustrationBg = Color(0xFFE8EDFF)
private val IllustBgGrad1  = Color(0xFFE8EDFF)
private val IllustBgGrad2  = Color(0xFFE0E8FF)
private val TealAccent     = Color(0xFF4FDBC8)
private val TealDot        = Color(0xFF4FDBC8)
private val NavyDeep       = Color(0xFF041B3C)
private val BlueAccent     = Color(0xFF0261A2)
private val NavyButton     = Color(0xFF073763)
private val SlateBody      = Color(0xFF43474F)
private val DotActive      = Color(0xFF4FDBC8)
private val DotInactive    = Color(0xFFD7E2FF)
private val BadgeBg        = Color(0xFFD1E4FF)

// ── Slide data (from Figma exactly) ──────────────────────────────────────────

data class OnboardingSlide(
    val stepLabel: String,
    val icon: ImageVector,
    val iconBg: Color,
    val illustrationTitle: String,
    val illustrationSubtitle: String,
    val headline: String,
    val headlineHindi: String,
    val body: String,
    val trustBadges: List<String>,
)

private val slides = listOf(
    OnboardingSlide(
        stepLabel = "STEP 1 OF 3",
        icon = Icons.Outlined.Groups,
        iconBg = Color(0xFF4FDBC8).copy(0.3f),
        illustrationTitle = "Community Savings Circle",
        illustrationSubtitle = "Mutual Trust Circle • Chit Certified",
        headline = "Grow Together",
        headlineHindi = "बड़े सपने, सुरक्षित बचत",
        body = "Participate in time-tested rotational savings circles backed by legal security, predictable returns, and monthly community dividends.",
        trustBadges = listOf(
            "100% Legal & Registered Chit Circles",
            "Transparent Monthly Group Books",
            "Instant & Automated Bank Mandates",
        ),
    ),
    OnboardingSlide(
        stepLabel = "STEP 2 OF 3",
        icon = Icons.Outlined.Payments,
        iconBg = Color(0xFF002141).copy(0.15f),
        illustrationTitle = "Smart Payment System",
        illustrationSubtitle = "Secure UPI & Bank Integration",
        headline = "Pay Easily, Track Everything",
        headlineHindi = "आसान भुगतान, पूरी पारदर्शिता",
        body = "Never miss an installment. Get smart reminders, pay via UPI or bank transfer, and track every rupee with a clear digital ledger.",
        trustBadges = listOf(
            "UPI, Net Banking & NEFT Supported",
            "Instant Payment Confirmation",
            "Auto-Debit Mandate Available",
        ),
    ),
    OnboardingSlide(
        stepLabel = "STEP 3 OF 3",
        icon = Icons.Outlined.AutoAwesome,
        iconBg = Color(0xFF14B8A6).copy(0.2f),
        illustrationTitle = "AI-Powered Assistant",
        illustrationSubtitle = "Multilingual Support • Policy Expert",
        headline = "AI That Understands You",
        headlineHindi = "AI जो आपकी भाषा बोलता है",
        body = "Ask ChitSmart AI anything about your account, due dates, or chit fund rules — in English, Hindi, or your regional language.",
        trustBadges = listOf(
            "6 Indian Languages Supported",
            "RBI Policy Knowledge Base",
            "24/7 Instant Responses",
        ),
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            OnboardingPage(
                slide = slides[page],
                isLastPage = page == slides.lastIndex,
                onSkip = onComplete,
                onNext = {
                    if (page < slides.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(page + 1) }
                    } else {
                        onComplete()
                    }
                },
                currentPage = pagerState.currentPage,
                totalPages = slides.size,
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    slide: OnboardingSlide,
    isLastPage: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    currentPage: Int,
    totalPages: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(horizontal = 16.dp),
    ) {
        // ── Top nav: step indicator + Skip ──
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealDot))
                Text(slide.stepLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.55.sp)
            }
            Text(
                "Skip",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BlueAccent,
                letterSpacing = 0.14.sp,
                modifier = Modifier.clickable { onSkip() }.padding(8.dp),
            )
        }

        // ── Illustration card ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(IllustBgGrad1, IllustBgGrad2, Color(0x33D1E4FF)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(slide.iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(slide.icon, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    slide.illustrationTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    textAlign = TextAlign.Center,
                )
            }

            // Trust capsule overlay at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = TealAccent, modifier = Modifier.size(11.dp))
                Text(slide.illustrationSubtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = NavyDeep, letterSpacing = 0.33.sp)
            }
        }

        // ── Page dots ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalPages) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (i == currentPage) 28.dp else 8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i == currentPage) DotActive else DotInactive),
                )
            }
        }

        // ── Content & typography ──
        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(slide.headline, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(slide.headlineHindi, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(slide.body, fontSize = 14.sp, color = SlateBody, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }

        // ── Trust badges ──
        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            slide.trustBadges.forEach { badge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F3FF))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(BadgeBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(11.dp))
                    }
                    Text(badge, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.24.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── CTA ──
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 0.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyButton),
            elevation = ButtonDefaults.buttonElevation(1.dp),
        ) {
            Text(
                if (isLastPage) "Get Started" else "Next",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.14.sp,
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 755, name = "Onboarding Slide 1")
@Composable
fun OnboardingScreenPreview() { MaterialTheme { OnboardingScreen(onComplete = {}) } }
