package com.chitsmart.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitsmart.ui.theme.*

// ── Colour tokens from Figma ──────────────────────────────────────────────────
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val TealDark = Color(0xFF003E37)
private val TealNet = Color(0xFF00B3A1)
private val TealBadge = Color(0x4D71F8E4) // rgba(113,248,228,0.3)
private val EscrowGreen = Color(0xFF002621)
private val BlueAccent = Color(0xFF0261A2)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val SurfaceBlue60 = Color(0x99F1F3FF)
private val BorderBlue = Color(0xFFE0E8FF)
private val BadgeBlue = Color(0xFFD3E3FF)
private val SlateBody = Color(0xFF43474F)
private val ErrorRed = Color(0xFFBA1A1A)
private val GoldAmber = Color(0xFFAD8000)
private val GoldBg = Color(0xFFFFF8E1)
private val GreenEligible = Color(0xFF1B6E55)
private val GreenEligBg = Color(0xFFE6FAF3)
private val QueueBlue = Color(0xFF0261A2)
private val QueueBg = Color(0xFFF1F3FF)
private val GuaranteeBg = Color(0xFFF0F4FF)
private val PageBg = Color(0xFFF9F9FF)

// ── Data models ───────────────────────────────────────────────────────────────

data class ReceivedPayout(
    // e.g. "MATURED CYCLE"
    val cycleLabel: String,
    // e.g. "Monthly Circle 25K"
    val groupName: String,
    // e.g. "Cycle Month 20 of 20 • Final Payout"
    val cycleDetail: String,
    // "Credited" | "Archived"
    val status: String,
    // e.g. "18 Aug 2026"
    val creditDate: String?,
    // e.g. "₹25,000"
    val grossValue: String,
    // e.g. "- ₹1,250"
    val foremanFee: String,
    // e.g. "5%"
    val foremanPct: String,
    // e.g. "₹23,750"
    val netAmount: String,
    // e.g. "HDFC Bank (A/c •••• 4092)"
    val bankDestination: String,
    // e.g. "UTR:  HDFC202608189872"
    val utrReference: String,
    // for archived rows
    val disbursedDate: String?,
    // for archived rows
    val disbursedAmount: String?,
)

data class UpcomingEligibility(
    val groupName: String,
    val cycleDetail: String,
    // "Eligible" | "Queue #18"
    val eligibilityStatus: String,
    val nextDrawInfo: String?,
    val windowSlot: String?,
    val projectedWindow: String?,
    val installmentDetail: String,
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun PayoutHistoryScreen(
    onBack: () -> Unit = {},
    onDownloadStatement: () -> Unit = {},
    onDownloadCertificate: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Sample data matching the Figma design exactly
    val receivedPayout =
        ReceivedPayout(
            cycleLabel = "MATURED CYCLE",
            groupName = "Monthly Circle 25K",
            cycleDetail = "Cycle Month 20 of 20 • Final Payout",
            status = "Credited",
            creditDate = "18 Aug 2026",
            grossValue = "₹25,000",
            foremanFee = "- ₹1,250",
            foremanPct = "5%",
            netAmount = "₹23,750",
            bankDestination = "HDFC Bank (A/c •••• 4092)",
            utrReference = "UTR:  HDFC202608189872",
            disbursedDate = null,
            disbursedAmount = null,
        )

    val upcomingEligibilities =
        listOf(
            UpcomingEligibility(
                groupName = "Community Chit 1L",
                cycleDetail = "Installment 8 of 20 Completed",
                eligibilityStatus = "Eligible",
                nextDrawInfo = "Next Expected Cycle Draw",
                windowSlot = "Window Slot #11",
                projectedWindow = "November 2026",
                installmentDetail = "Installment 8 of 20 Completed",
            ),
            UpcomingEligibility(
                groupName = "Family Savings 50K",
                cycleDetail = "Installment 15 of 24",
                eligibilityStatus = "Queue #18",
                nextDrawInfo = null,
                windowSlot = null,
                projectedWindow = "February 2027",
                installmentDetail = "Installment 15 of 24",
            ),
        )

    Scaffold(
        containerColor = PageBg,
        topBar = {
            PayoutHistoryTopBar(onBack = onBack, onDownloadStatement = onDownloadStatement)
        },
        bottomBar = {
            PayoutHistoryBottomNav()
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Hero disbursement card ──
            item { Spacer(Modifier.height(8.dp)) }
            item { HeroDisbursementCard(totalAmount = "₹82,500", bankAccount = "HDFC Bank • • • • 4092") }

            // ── Escrow verification badge ──
            item { Spacer(Modifier.height(16.dp)) }
            item { EscrowVerificationBadge() }

            // ── Segment tabs ──
            item { Spacer(Modifier.height(16.dp)) }
            item {
                SegmentTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            if (selectedTab == 0) {
                // ── Received payout card ──
                item { ReceivedPayoutCard(payout = receivedPayout, onDownloadCertificate = onDownloadCertificate) }

                // ── Archived row ──
                item { Spacer(Modifier.height(12.dp)) }
                item { ArchivedPayoutRow(groupName = "Festival Chit 60K", disbursedDate = "12 Dec 2025", amount = "₹58,750") }

                // ── Rotational eligibility section ──
                item { Spacer(Modifier.height(16.dp)) }
                item { ScheduledEligibilityHeader() }
                items(upcomingEligibilities) { eligibility ->
                    Spacer(Modifier.height(8.dp))
                    EligibilityCard(eligibility = eligibility)
                }

                // ── Informational note ──
                item { Spacer(Modifier.height(12.dp)) }
                item { EligibilityNote() }

                // ── Circle progression ──
                item { Spacer(Modifier.height(8.dp)) }
                item { CircleProgressionRow() }

                // ── Statutory guarantee ──
                item { Spacer(Modifier.height(16.dp)) }
                item { StatutoryGuaranteeCard() }
                item { Spacer(Modifier.height(16.dp)) }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Upcoming eligibility details",
                            color = SlateBody,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayoutHistoryTopBar(
    onBack: () -> Unit,
    onDownloadStatement: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        title = {
            Column {
                Text(
                    "Payouts Received",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = NavyDeep,
                )
                Text(
                    "Settlements & Banking Records",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = SlateBody,
                    letterSpacing = 0.33.sp,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep)
            }
        },
        actions = {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlue)
                        .clickable { onDownloadStatement() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = "Download statement",
                    tint = NavyMid,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
        },
    )
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroDisbursementCard(
    totalAmount: String,
    bankAccount: String,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0B3D7A), Color(0xFF1769AA)),
                    ),
                )
                .padding(16.dp),
    ) {
        Column {
            // Verified badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF71F8E4),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "TOTAL DISBURSED TO MEMBER",
                        color = Color(0xFF71F8E4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(TealBadge)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF71F8E4),
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            "Verified",
                            color = Color(0xFF71F8E4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Amount
            Text(
                totalAmount,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            )
            Text(
                "INR Total",
                color = Color(0xB3FFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(12.dp))

            // Bank row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0x99FFFFFF),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0x99FFFFFF), fontSize = 11.sp)) {
                            append("Credited directly to primary bank account\n")
                        }
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Medium, fontSize = 11.sp)) {
                            append(bankAccount)
                        }
                    },
                )
            }
        }
    }
}

// ── Escrow badge ──────────────────────────────────────────────────────────────

@Composable
private fun EscrowVerificationBadge() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(EscrowGreen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "RBI Chit Fund Escrow Settlement",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                letterSpacing = 0.24.sp,
            )
            Text(
                "Authorized nodal account transfers under state gazette mandate",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SlateBody,
                letterSpacing = 0.33.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

// ── Segment tabs ──────────────────────────────────────────────────────────────

@Composable
private fun SegmentTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceBlue)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf("Received Payouts" to 1, "Upcoming Eligibility" to 2).forEachIndexed { index, (label, count) ->
            val selected = selectedTab == index
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color.White else Color.Transparent)
                        .then(
                            if (selected) {
                                Modifier.shadow(1.dp, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) NavyMid else SlateBody,
                    letterSpacing = 0.24.sp,
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (selected) BadgeBlue else BorderBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$count",
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) Color(0xFF001C39) else SlateBody,
                        letterSpacing = 0.33.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ── Received payout card ──────────────────────────────────────────────────────

@Composable
private fun ReceivedPayoutCard(
    payout: ReceivedPayout,
    onDownloadCertificate: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp),
    ) {
        // Top meta: group name + Credited badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EscrowGreen),
                    )
                    Text(
                        payout.cycleLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueAccent,
                        letterSpacing = 0.55.sp,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    payout.groupName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                )
                Text(
                    payout.cycleDetail,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateBody,
                    letterSpacing = 0.33.sp,
                )
            }
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TealBadge)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = TealDark,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        payout.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TealDark,
                        letterSpacing = 0.33.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Credit date tag
        payout.creditDate?.let { date ->
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceBlue)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = SlateBody,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                            append("Credited on ")
                        }
                        withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                            append(date)
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Financial ledger breakdown
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceBlue60)
                    .padding(8.dp),
        ) {
            LedgerRow(label = "Gross Chit Value", value = payout.grossValue, valueColor = NavyDeep)

            Spacer(Modifier.height(8.dp))

            LedgerRow(
                label = "Less Foreman Fee (${payout.foremanPct})",
                value = payout.foremanFee,
                valueColor = ErrorRed,
                showInfoIcon = true,
            )

            Spacer(Modifier.height(8.dp))

            // Net credited amount — highlighted row
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "NET CREDITED AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateBody,
                        letterSpacing = 0.33.sp,
                    )
                    Text(
                        "Into Bank A/c",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateBody,
                        letterSpacing = 0.33.sp,
                    )
                }
                Text(
                    payout.netAmount,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealNet,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bank destination + UTR reference
        LedgerMetaRow(icon = Icons.Outlined.AccountBalance, label = "Destination", value = payout.bankDestination)
        Spacer(Modifier.height(6.dp))
        LedgerMetaRow(icon = Icons.Outlined.Tag, label = "Reference UTR", value = payout.utrReference, copyable = true)

        Spacer(Modifier.height(12.dp))

        // Download certificate button
        OutlinedButton(
            onClick = { onDownloadCertificate(payout.groupName) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyMid),
            border = ButtonDefaults.outlinedButtonBorder,
        ) {
            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Download Payout Certificate",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Ledger row helper ─────────────────────────────────────────────────────────

@Composable
private fun LedgerRow(
    label: String,
    value: String,
    valueColor: Color,
    showInfoIcon: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, fontSize = 12.sp, color = SlateBody, fontWeight = FontWeight.Normal)
            if (showInfoIcon) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = SlateBody,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor, letterSpacing = 0.14.sp)
    }
}

@Composable
private fun LedgerMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    copyable: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = SlateBody, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, color = SlateBody, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 12.sp, color = NavyDeep, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (copyable) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy UTR", tint = BlueAccent, modifier = Modifier.size(14.dp))
        }
    }
}

// ── Archived payout row ───────────────────────────────────────────────────────

@Composable
private fun ArchivedPayoutRow(
    groupName: String,
    disbursedDate: String,
    amount: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(8.dp))
                .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Archive, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(groupName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Disbursed on $disbursedDate", fontSize = 11.sp, color = SlateBody)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text("Archived", fontSize = 10.sp, color = SlateBody, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Scheduled eligibility ─────────────────────────────────────────────────────

@Composable
private fun ScheduledEligibilityHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Scheduled Rotational Eligibility",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyDeep,
        )
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceBlue)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                "Automatic Queue",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = BlueAccent,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

@Composable
private fun EligibilityCard(eligibility: UpcomingEligibility) {
    val isEligible = eligibility.eligibilityStatus == "Eligible"

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, if (isEligible) Color(0xFFB2DFDB) else Color(0xFFE0E8FF), RoundedCornerShape(10.dp))
                .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isEligible) GreenEligBg else QueueBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (isEligible) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (isEligible) GreenEligible else QueueBlue,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column {
                    Text(eligibility.groupName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text(eligibility.cycleDetail, fontSize = 11.sp, color = SlateBody)
                }
            }
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isEligible) GreenEligBg else QueueBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    eligibility.eligibilityStatus,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEligible) GreenEligible else QueueBlue,
                    letterSpacing = 0.22.sp,
                )
            }
        }

        if (eligibility.nextDrawInfo != null) {
            Spacer(Modifier.height(8.dp))
            Text(eligibility.nextDrawInfo, fontSize = 11.sp, color = SlateBody)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    eligibility.projectedWindow ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                )
                eligibility.windowSlot?.let { slot ->
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceBlue)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(slot, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = BlueAccent)
                    }
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Projected Window", fontSize = 11.sp, color = SlateBody)
                Text(eligibility.projectedWindow ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
            }
        }
    }
}

// ── Eligibility note ──────────────────────────────────────────────────────────

@Composable
private fun EligibilityNote() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Outlined.PersonSearch,
            contentDescription = null,
            tint = SlateBody,
            modifier = Modifier.size(14.dp).padding(top = 2.dp),
        )
        Text(
            "Active members who complete regular installments receive payout upon scheduled rotation " +
                "disbursement under Chit Funds Act, 1982 rules.",
            fontSize = 11.sp,
            color = SlateBody,
            lineHeight = 16.sp,
        )
    }
}

// ── Circle progression ────────────────────────────────────────────────────────

@Composable
private fun CircleProgressionRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Circle Progression", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateBody)
        Text("40% Cycle Duration", fontSize = 11.sp, color = SlateBody)
    }
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(
        progress = { 0.4f },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        color = TealNet,
        trackColor = SurfaceBlue,
    )
}

// ── Statutory guarantee card ──────────────────────────────────────────────────

@Composable
private fun StatutoryGuaranteeCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GuaranteeBg)
                .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Security, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
            Text(
                "STATUTORY SECURITY GUARANTEE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyMid,
                letterSpacing = 0.44.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "All disbursements are routed through statutory state-regulated escrow accounts monitored " +
                "under Section 20 of Chit Funds Act, 1982.",
            fontSize = 11.sp,
            color = SlateBody,
            lineHeight = 16.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
            Text(
                "Registered Foreman License No: TN-RO-2024-CF091",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = BlueAccent,
            )
        }
    }
}

// ── Bottom navigation ─────────────────────────────────────────────────────────

@Composable
private fun PayoutHistoryBottomNav() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp),
    ) {
        val items =
            listOf(
                Icons.Outlined.Home to "Home",
                Icons.Outlined.AccountBalance to "My Chits",
                Icons.Outlined.Payments to "Payments",
                Icons.Outlined.SmartToy to "Assistant",
                Icons.Outlined.Person to "Profile",
            )
        items.forEachIndexed { index, (icon, label) ->
            val selected = index == 2 // Payments tab is active
            NavigationBarItem(
                selected = selected,
                onClick = {},
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = { Text(label, fontSize = 10.sp) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyMid,
                        selectedTextColor = NavyMid,
                        indicatorColor = SurfaceBlue,
                        unselectedIconColor = SlateBody,
                        unselectedTextColor = SlateBody,
                    ),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Payout History")
@Composable
fun PayoutHistoryScreenPreview() {
    MaterialTheme {
        PayoutHistoryScreen()
    }
}
