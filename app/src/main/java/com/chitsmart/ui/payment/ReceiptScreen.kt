package com.chitsmart.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitsmart.utils.CurrencyUtils

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg      = Color(0xFFF9F9FF)
private val NavyDeep    = Color(0xFF041B3C)
private val NavyMid     = Color(0xFF002141)
private val TealAccent  = Color(0xFF00B3A1)
private val BlueAccent  = Color(0xFF0261A2)
private val SlateBody   = Color(0xFF43474F)
private val SlateHint   = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val SuccessGreen = Color(0xFF22C55E)

// ── Data model ────────────────────────────────────────────────────────────────

data class ReceiptData(
    val receiptNumber: String = "CS-RCPT-20261015-001",
    val dateTime: String = "15 Oct 2026, 10:30 AM",
    val memberName: String = "Priya Sharma",
    val memberId: String = "CS-8402",
    val groupName: String = "Family Savings 50K",
    val installmentMonth: String = "October 2026 (Month 8 of 24)",
    val amount: Double = 2500.0,
    val lateFee: Double = 0.0,
    val totalAmount: Double = 2500.0,
    val paymentMethod: String = "UPI",
    val transactionRef: String = "TXN20261015ABCD1234",
    val gatewayRef: String = "GW20261015XYZ5678",
    val upiId: String = "priya@hdfcbank",
    val status: String = "Successful",
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    paymentId: String = "receipt_001",
    receiptData: ReceiptData = ReceiptData(),
    onBack: () -> Unit = {},
    onDownloadPDF: () -> Unit = {},
    onShareWhatsApp: () -> Unit = {},
    onShareEmail: () -> Unit = {},
) {
    var showShareSheet by remember { mutableStateOf(false) }

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
                    Text("Payment Receipt", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                },
                actions = {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = NavyDeep)
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = PageBg) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDownloadPDF,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download PDF", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            ReceiptCard(receiptData = receiptData)
            Spacer(Modifier.height(16.dp))
        }
    }

    // Share bottom sheet
    if (showShareSheet) {
        ReceiptShareBottomSheet(
            onDismiss = { showShareSheet = false },
            onDownloadPDF = { showShareSheet = false; onDownloadPDF() },
            onShareWhatsApp = { showShareSheet = false; onShareWhatsApp() },
            onShareEmail = { showShareSheet = false; onShareEmail() },
        )
    }
}

// ── Receipt card ──────────────────────────────────────────────────────────────

@Composable
private fun ReceiptCard(receiptData: ReceiptData) {
    // Perforated top border effect via dashed line
    val perforationColor = TealAccent

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Perforated top edge
        Box(
            modifier = Modifier.fillMaxWidth().height(12.dp).drawBehind {
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawLine(
                    color = perforationColor,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                // Circular notches at the edge
                val radius = 6.dp.toPx()
                drawCircle(color = PageBg, radius = radius, center = Offset(0f, size.height / 2f))
                drawCircle(color = PageBg, radius = radius, center = Offset(size.width, size.height / 2f))
            },
        )

        // Receipt body
        Column(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)).clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)).background(Color.White).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status chip + header
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE6FAF3)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Payment Successful", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                Spacer(Modifier.height(4.dp))
                Text(receiptData.receiptNumber, fontSize = 12.sp, color = SlateHint, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
            }

            HorizontalDivider(color = Color(0xFFF1F3FF))

            // Amount section
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("₹${String.format("%,.0f", receiptData.totalAmount)}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = NavyMid)
                Text(CurrencyUtils.amountToIndianWords(receiptData.totalAmount), fontSize = 12.sp, color = SlateBody, textAlign = TextAlign.Center)
            }

            HorizontalDivider(color = Color(0xFFF1F3FF))

            // Details grid
            ReceiptRow("Member Name", receiptData.memberName)
            ReceiptRow("Member ID", "#${receiptData.memberId}")
            ReceiptRow("Chit Group", receiptData.groupName)
            ReceiptRow("Installment", receiptData.installmentMonth)
            ReceiptRow("Date & Time", receiptData.dateTime)

            HorizontalDivider(color = Color(0xFFF1F3FF))

            // Amount breakdown
            ReceiptRow("Installment Amount", "₹${String.format("%,.0f", receiptData.amount)}")
            if (receiptData.lateFee > 0) {
                ReceiptRow("Late Fee", "₹${String.format("%,.0f", receiptData.lateFee)}", valueColor = Color(0xFFF59E0B))
            }
            ReceiptRow("Total Paid", "₹${String.format("%,.0f", receiptData.totalAmount)}", isBold = true)

            HorizontalDivider(color = Color(0xFFF1F3FF))

            // Payment method
            ReceiptRow("Payment Method", receiptData.paymentMethod)
            if (receiptData.upiId.isNotBlank() && receiptData.paymentMethod == "UPI") {
                ReceiptRow("UPI ID", receiptData.upiId)
            }
            ReceiptRow("Transaction Ref", receiptData.transactionRef, mono = true)
            ReceiptRow("Gateway Ref", receiptData.gatewayRef, mono = true)

            HorizontalDivider(color = Color(0xFFF1F3FF))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("ChitSmart Financial Services Pvt. Ltd.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateBody, textAlign = TextAlign.Center)
                Text("Registered under the Chit Funds Act, 1982", fontSize = 10.sp, color = SlateHint, textAlign = TextAlign.Center)
                Text("This is a system-generated receipt. No signature required.", fontSize = 10.sp, color = SlateHint, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = NavyDeep,
    isBold: Boolean = false,
    mono: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, fontSize = 13.sp, color = SlateHint, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Share bottom sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptShareBottomSheet(
    onDismiss: () -> Unit,
    onDownloadPDF: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareEmail: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Share Receipt", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Spacer(Modifier.height(4.dp))

            ShareOption(
                icon = Icons.Outlined.PictureAsPdf,
                title = "Download PDF",
                subtitle = "Save to device",
                iconBg = Color(0xFFFFECEC),
                iconTint = Color(0xFFE53935),
                onClick = onDownloadPDF,
            )
            ShareOption(
                icon = Icons.Outlined.Chat,
                title = "Share on WhatsApp",
                subtitle = "Send receipt via WhatsApp",
                iconBg = Color(0xFFE8F5E9),
                iconTint = Color(0xFF2E7D32),
                onClick = onShareWhatsApp,
            )
            ShareOption(
                icon = Icons.Outlined.Email,
                title = "Share via Email",
                subtitle = "Send to your email",
                iconBg = SurfaceBlue,
                iconTint = BlueAccent,
                onClick = onShareEmail,
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF9F9FF))
            .run {
                this.then(Modifier.padding(0.dp))
            }
            .padding(0.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text(subtitle, fontSize = 12.sp, color = SlateBody)
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SlateHint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Receipt")
@Composable
fun ReceiptScreenPreview() { MaterialTheme { ReceiptScreen() } }
