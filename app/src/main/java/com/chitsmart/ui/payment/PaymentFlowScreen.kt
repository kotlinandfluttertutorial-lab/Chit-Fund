package com.chitsmart.ui.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens from Figma (node 1:939) ────────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyDark = Color(0xFF073763)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val TealRecommend = Color(0x4D4FDBC8)
private val TealDark = Color(0xFF003E37)
private val BlueAccent = Color(0xFF0261A2)
private val BlueBadge = Color(0xFFD1E4FF)
private val BlueText = Color(0xFF00497C)
private val SlateBody = Color(0xFF43474F)
private val SlateHint = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val InputPill = Color(0xFFE8EDFF)
private val PolicyPill = Color(0xFFE8EDFF)
private val PolicyText = Color(0xFF005048)
private val GrayBorder = Color(0xFFC3C6D0)
private val SuccessGreen = Color(0xFF22C55E)
private val ErrorRed = Color(0xFFEF4444)

// ── Payment flow state ────────────────────────────────────────────────────────

data class PaymentSummaryData(
    val groupName: String = "Family Savings 50K",
    val installmentMonth: String = "October 2026",
    val installmentNo: Int = 7,
    val totalInstallments: Int = 24,
    val amount: String = "₹2,500",
    val lateFee: String? = "₹50",
    val total: String = "₹2,550",
    val savedUpiIds: List<Pair<String, String>> =
        listOf(
            "priya@ybl" to "Yes Bank",
            "priya98765@okicici" to "ICICI Bank",
        ),
)

enum class PaymentMethod { UPI, NET_BANKING, DEBIT_CARD, NEFT }

@Composable
fun PaymentFlowScreen(
    data: PaymentSummaryData = PaymentSummaryData(),
    onBack: () -> Unit = {},
    onPaymentComplete: (success: Boolean, txnId: String?) -> Unit = { _, _ -> },
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var selectedUpiIndex by remember { mutableIntStateOf(0) }

    BackHandler(enabled = step == 3) { /* block back during processing */ }
    BackHandler(enabled = step > 1 && step != 3) { step-- }

    when (step) {
        1 ->
            PaymentSummaryStep(
                data = data,
                onBack = onBack,
                onContinue = { step = 2 },
            )
        2 ->
            SelectMethodStep(
                data = data,
                selectedMethod = selectedMethod,
                selectedUpiIndex = selectedUpiIndex,
                onMethodChange = { selectedMethod = it },
                onUpiChange = { selectedUpiIndex = it },
                onBack = { step = 1 },
                onPay = { step = 3 },
            )
        3 ->
            ProcessingStep(
                amount = data.total,
                onComplete = { success ->
                    step = if (success) 4 else 5
                    onPaymentComplete(success, if (success) "TXN-20261015-00001" else null)
                },
            )
        4 -> PaymentSuccessStep(data = data, onDone = onBack)
        5 -> PaymentFailureStep(onRetry = { step = 1 }, onTryOther = { step = 2 }, onSupport = {})
    }
}

// ── Step 1: Summary ───────────────────────────────────────────────────────────

@Composable
private fun PaymentSummaryStep(
    data: PaymentSummaryData,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PageBg)
                .systemBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(InputPill).clickable { onBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep, modifier = Modifier.size(13.dp))
                }
                Column {
                    Text("Pay Installment", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
                    Text("Secure RBI Payment Gateway", fontSize = 12.sp, color = SlateBody)
                }
            }
            Row(
                modifier =
                    Modifier.clip(
                        RoundedCornerShape(100.dp),
                    ).background(Color(0xFFE0E8FF)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = NavyMid, modifier = Modifier.size(10.dp))
                Text("SSL Secured", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyMid, letterSpacing = 0.33.sp)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // Step progress
            StepProgressIndicator(currentStep = 1, totalSteps = 2, label = "Summary & Payment Method")
            Spacer(Modifier.height(16.dp))

            // Payment summary card
            Column(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Text(data.groupName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text(
                            "${data.installmentMonth} • Installment ${data.installmentNo}/${data.totalInstallments}",
                            fontSize = 12.sp,
                            color = SlateBody,
                        )
                    }
                    Box(
                        modifier =
                            Modifier.clip(
                                RoundedCornerShape(20.dp),
                            ).background(SurfaceBlue).padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealAccent)
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F3FF))
                Spacer(Modifier.height(12.dp))

                // Line items
                PaymentLineItem("Installment Amount", data.amount)
                if (data.lateFee != null) {
                    Spacer(Modifier.height(6.dp))
                    PaymentLineItem("Late Fee", data.lateFee, valueColor = ErrorRed, showInfo = true)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F3FF))
                Spacer(Modifier.height(8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Total Payable", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text(data.total, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TealAccent)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Policy citation
            Row(
                modifier =
                    Modifier.fillMaxWidth().clip(
                        RoundedCornerShape(8.dp),
                    ).background(PolicyPill).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Gavel, contentDescription = null, tint = PolicyText, modifier = Modifier.size(12.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(color = PolicyText, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        ) { append("Chit Funds Act, 1982 §4 — ") }
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Foreman commission max 5%") }
                    },
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = PolicyText, modifier = Modifier.size(10.dp))
            }

            Spacer(Modifier.height(16.dp))
        }

        // Continue button
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Text("Continue to Payment →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun PaymentLineItem(
    label: String,
    value: String,
    valueColor: Color = NavyDeep,
    showInfo: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 14.sp, color = SlateBody)
            if (showInfo) Icon(Icons.Outlined.Info, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(12.dp))
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ── Step 2: Select Method ─────────────────────────────────────────────────────

@Composable
private fun SelectMethodStep(
    data: PaymentSummaryData,
    selectedMethod: PaymentMethod,
    selectedUpiIndex: Int,
    onMethodChange: (PaymentMethod) -> Unit,
    onUpiChange: (Int) -> Unit,
    onBack: () -> Unit,
    onPay: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(InputPill).clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep, modifier = Modifier.size(13.dp))
            }
            Column {
                Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Choose how to pay ${data.total}", fontSize = 12.sp, color = SlateBody)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            StepProgressIndicator(currentStep = 2, totalSteps = 2, label = "Select Payment Method")
            Spacer(Modifier.height(16.dp))

            // Payment method card
            Column(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Box(
                        modifier =
                            Modifier.clip(
                                RoundedCornerShape(100.dp),
                            ).background(BlueBadge).padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("Step 2", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueText, letterSpacing = 0.33.sp)
                    }
                }

                // UPI option (expanded)
                Column(
                    modifier =
                        Modifier.fillMaxWidth().clip(
                            RoundedCornerShape(12.dp),
                        ).background(
                            if (selectedMethod == PaymentMethod.UPI) SurfaceBlue else Color.White,
                        ).border(
                            1.dp,
                            if (selectedMethod == PaymentMethod.UPI) Color(0xFFBFD3FF) else GrayBorder.copy(0.3f),
                            RoundedCornerShape(12.dp),
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onMethodChange(PaymentMethod.UPI) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Radio button
                            Box(
                                modifier =
                                    Modifier.size(
                                        20.dp,
                                    ).clip(
                                        CircleShape,
                                    ).background(
                                        if (selectedMethod == PaymentMethod.UPI) NavyDark else Color.Transparent,
                                    ).border(2.dp, if (selectedMethod == PaymentMethod.UPI) NavyDark else GrayBorder, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selectedMethod == PaymentMethod.UPI) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White),
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(InputPill),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = BlueAccent,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "UPI Options",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NavyDeep,
                                        letterSpacing = 0.14.sp,
                                    )
                                    Box(
                                        modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(100.dp),
                                            ).background(TealRecommend).padding(horizontal = 8.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            "Recommended",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealDark,
                                            letterSpacing = 0.33.sp,
                                        )
                                    }
                                }
                                Text("Instant, no charges", fontSize = 12.sp, color = SlateBody)
                            }
                        }
                        Icon(
                            if (selectedMethod == PaymentMethod.UPI) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = SlateBody,
                            modifier = Modifier.size(10.dp),
                        )
                    }

                    // UPI expanded content
                    if (selectedMethod == PaymentMethod.UPI) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White).padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "SAVED UPI ACCOUNTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateBody,
                                    letterSpacing = 0.55.sp,
                                )
                                data.savedUpiIds.forEachIndexed { i, (vpa, bank) ->
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth().clip(
                                                RoundedCornerShape(8.dp),
                                            ).background(
                                                if (selectedUpiIndex == i) Color(0xFFE8EDFF).copy(0.6f) else Color.White,
                                            ).clickable {
                                                onUpiChange(i)
                                            }.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            // Radio
                                            Box(
                                                modifier =
                                                    Modifier.size(
                                                        16.dp,
                                                    ).clip(
                                                        CircleShape,
                                                    ).background(
                                                        Color.White,
                                                    ).border(
                                                        if (selectedUpiIndex == i) 2.dp else 1.dp,
                                                        if (selectedUpiIndex == i) NavyDark else Color(0xFF767676),
                                                        CircleShape,
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (selectedUpiIndex == i) {
                                                    Box(
                                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(NavyDark),
                                                    )
                                                }
                                            }
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Text(
                                                        vpa,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = NavyDeep,
                                                        letterSpacing = 0.24.sp,
                                                    )
                                                    Icon(
                                                        Icons.Outlined.Verified,
                                                        contentDescription = null,
                                                        tint = TealAccent,
                                                        modifier = Modifier.size(11.dp),
                                                    )
                                                }
                                                Text(bank, fontSize = 12.sp, color = SlateHint)
                                            }
                                        }
                                        if (selectedUpiIndex == i) {
                                            Box(
                                                modifier =
                                                    Modifier.clip(
                                                        RoundedCornerShape(4.dp),
                                                    ).background(Color(0xFFD1E4FF).copy(0.4f)).padding(horizontal = 8.dp, vertical = 2.dp),
                                            ) {
                                                Text(
                                                    "Default",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BlueAccent,
                                                    letterSpacing = 0.33.sp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // Add new UPI / QR buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier =
                                        Modifier.weight(
                                            1f,
                                        ).shadow(
                                            1.dp,
                                            RoundedCornerShape(8.dp),
                                        ).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add New UPI ID", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                                }
                                Row(
                                    modifier =
                                        Modifier.weight(
                                            1f,
                                        ).shadow(
                                            1.dp,
                                            RoundedCornerShape(8.dp),
                                        ).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Outlined.QrCode, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Scan QR Code", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                                }
                            }
                        }
                    }
                }

                // Other method rows
                listOf(
                    PaymentMethod.NET_BANKING to "Net Banking",
                    PaymentMethod.DEBIT_CARD to "Debit Card",
                    PaymentMethod.NEFT to "NEFT / RTGS",
                ).forEach {
                        (method, label) ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().clip(
                                RoundedCornerShape(8.dp),
                            ).background(Color.White).border(1.dp, GrayBorder.copy(0.3f), RoundedCornerShape(8.dp)).clickable {
                                onMethodChange(method)
                            }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier.size(
                                    20.dp,
                                ).clip(
                                    CircleShape,
                                ).border(
                                    2.dp,
                                    if (selectedMethod == method) NavyDark else GrayBorder,
                                    CircleShape,
                                ).background(if (selectedMethod == method) NavyDark else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selectedMethod == method) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = onPay,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pay Securely ${data.total}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Step 3: Processing ────────────────────────────────────────────────────────

@Composable
private fun ProcessingStep(
    amount: String,
    onComplete: (Boolean) -> Unit,
) {
    // Block back navigation during processing
    BackHandler { /* intentionally blocked */ }

    // Simulate processing after 3 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onComplete(true) // stub: always succeed in preview
    }

    Box(modifier = Modifier.fillMaxSize().background(PageBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(SurfaceBlue), contentAlignment = Alignment.Center) {
                Text("CS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
            }
            CircularProgressIndicator(modifier = Modifier.size(56.dp), color = TealAccent, strokeWidth = 3.dp)
            Text(
                "Processing your payment…",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                textAlign = TextAlign.Center,
            )
            Text(amount, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
            Text(
                "Please do not press back or close the app",
                fontSize = 14.sp,
                color = SlateBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}

// ── Step 4: Success ───────────────────────────────────────────────────────────

@Composable
private fun PaymentSuccessStep(
    data: PaymentSummaryData,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF22C55E).copy(0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(data.total, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
        Text("Payment Successful!", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
        Spacer(Modifier.height(24.dp))

        // Details card
        Column(
            modifier =
                Modifier.fillMaxWidth().shadow(
                    1.dp,
                    RoundedCornerShape(12.dp),
                ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReceiptRow("Transaction ID", "TXN-20261015-00001")
            ReceiptRow("Date & Time", "15 Oct 2026, 10:32 AM")
            ReceiptRow("Group", data.groupName)
            ReceiptRow("Installment", data.installmentMonth)
            ReceiptRow("Payment Method", "UPI — priya@ybl")
        }
        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = {
            },
            modifier =
                Modifier.fillMaxWidth().height(
                    48.dp,
                ),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid),
        ) {
            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Download Receipt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
        ) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = SlateBody)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
    }
}

// ── Step 5: Failure ───────────────────────────────────────────────────────────

@Composable
private fun PaymentFailureStep(
    onRetry: () -> Unit,
    onTryOther: () -> Unit,
    onSupport: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(ErrorRed.copy(0.1f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Payment Failed", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
        Spacer(Modifier.height(8.dp))
        Text("Your bank declined this transaction", fontSize = 16.sp, color = SlateBody, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
        ) {
            Text("Retry Payment", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onTryOther,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid),
        ) {
            Text("Try Different Method", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSupport) { Text("Contact Support", fontSize = 14.sp, color = BlueAccent) }
        Spacer(Modifier.height(24.dp))
    }
}

// ── Shared: Step progress indicator ──────────────────────────────────────────

@Composable
private fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    label: String,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth().shadow(
                1.dp,
                RoundedCornerShape(12.dp),
            ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(BlueBadge).padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        "Step $currentStep of $totalSteps",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueText,
                        letterSpacing = 0.33.sp,
                    )
                }
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.24.sp)
            }
            Text(
                "${(currentStep * 100 / totalSteps)}% complete",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BlueAccent,
                letterSpacing = 0.33.sp,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(100.dp)).background(InputPill)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(
                        currentStep.toFloat() / totalSteps,
                    ).fillMaxHeight().clip(RoundedCornerShape(100.dp)).background(NavyDark),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = NavyDark, modifier = Modifier.size(12.dp))
                Text("1. Select Method", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyDark, letterSpacing = 0.33.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    if (currentStep >= 2) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (currentStep >= 2) NavyDark else SlateHint,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    "2. Confirm & Pay",
                    fontSize = 11.sp,
                    fontWeight = if (currentStep >= 2) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (currentStep >= 2) NavyDark else SlateHint,
                    letterSpacing = 0.33.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Payment Summary")
@Composable
fun PaymentFlowPreview() {
    MaterialTheme { PaymentFlowScreen() }
}
