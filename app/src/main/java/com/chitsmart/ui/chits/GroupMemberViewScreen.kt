package com.chitsmart.ui.chits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val TealPaid = Color(0xFF14B8A6)
private val BlueAccent = Color(0xFF0261A2)
private val AmberDue = Color(0xFFF59E0B)
private val AmberBg = Color(0xFFFFF8E1)
private val ErrorRed = Color(0xFFEF4444)
private val ErrorBg = Color(0xFFFFDAD6)
private val SlateBody = Color(0xFF43474F)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val PaidGreen = Color(0xFF1B6E55)
private val PaidGreenBg = Color(0xFFE6FAF3)
private val UpcomingGray = Color(0xFF94A3B8)

// ── Data models ───────────────────────────────────────────────────────────────

enum class InstallmentStatus { PAID, DUE, UPCOMING, OVERDUE }

data class InstallmentCell(
    val monthNumber: Int,
    val dueDate: String,
    val amount: String,
    val status: InstallmentStatus,
    val paymentId: String? = null,
    val paidDate: String? = null,
)

data class PayoutRecord(
    val date: String,
    val amount: String,
    val bankReference: String,
    // "Credited" | "Processing"
    val status: String,
)

data class MembershipViewData(
    val groupName: String,
    val memberCode: String,
    val chitValue: String,
    val monthlyInstallment: String,
    val durationMonths: Int,
    val currentStatus: String,
    val installments: List<InstallmentCell>,
    val payoutHistory: List<PayoutRecord>,
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun GroupMemberViewScreen(
    membership: MembershipViewData = sampleMembershipView,
    onBack: () -> Unit = {},
    onPayNow: (installmentId: String) -> Unit = {},
    onViewReceipt: (paymentId: String) -> Unit = {},
) {
    val dueInstallment =
        membership.installments.firstOrNull {
            it.status == InstallmentStatus.DUE || it.status == InstallmentStatus.OVERDUE
        }

    Scaffold(
        containerColor = PageBg,
        topBar = { GroupMemberTopBar(membership = membership, onBack = onBack) },
        floatingActionButton = {
            if (dueInstallment != null) {
                ExtendedFloatingActionButton(
                    onClick = { onPayNow(dueInstallment.monthNumber.toString()) },
                    containerColor = TealAccent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Pay Now ${dueInstallment.amount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        bottomBar = { GroupMemberBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Membership summary card ──
            item {
                Spacer(Modifier.height(4.dp))
                MembershipSummaryCard(membership = membership)
            }

            // ── Progress overview ──
            item { ProgressOverviewCard(membership = membership) }

            // ── Installment calendar ──
            item {
                Text("Installment Calendar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Spacer(Modifier.height(8.dp))
                CalendarLegend()
                Spacer(Modifier.height(8.dp))
                InstallmentCalendarGrid(
                    installments = membership.installments,
                    onCellTap = { cell -> cell.paymentId?.let { onViewReceipt(it) } },
                )
            }

            // ── Payout history (read-only) ──
            item {
                Text("Payout History", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Spacer(Modifier.height(4.dp))
                if (membership.payoutHistory.isEmpty()) {
                    EmptyPayoutState()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        membership.payoutHistory.forEach { payout ->
                            PayoutHistoryRow(payout = payout)
                        }
                    }
                }
            }

            // ── Regulatory note ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Gavel, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                    Text(
                        "Governed by Chit Funds Act, 1982. All transactions are processed via RBI-compliant escrow accounts.",
                        fontSize = 11.sp,
                        color = SlateBody,
                        lineHeight = 15.sp,
                    )
                }
                Spacer(Modifier.height(80.dp)) // FAB padding
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupMemberTopBar(
    membership: MembershipViewData,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
        title = {
            Column {
                Text(membership.groupName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(membership.memberCode, fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
            }
        },
        actions = {
            Box(
                modifier =
                    Modifier.padding(
                        end = 8.dp,
                    ).clip(RoundedCornerShape(20.dp)).background(PaidGreenBg).padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(PaidGreen))
                    Text(
                        membership.currentStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PaidGreen,
                        letterSpacing = 0.33.sp,
                    )
                }
            }
        },
    )
}

// ── Membership summary card ───────────────────────────────────────────────────

@Composable
private fun MembershipSummaryCard(membership: MembershipViewData) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(colors = listOf(Color(0xFF0B3D7A), Color(0xFF1769AA))))
                .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Chit Value", fontSize = 11.sp, color = Color(0xB3FFFFFF))
                    Text(membership.chitValue, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly", fontSize = 11.sp, color = Color(0xB3FFFFFF))
                    Text(membership.monthlyInstallment, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF71F8E4))
                }
            }
            HorizontalDivider(color = Color(0x33FFFFFF))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${membership.durationMonths} month cycle", fontSize = 12.sp, color = Color(0x99FFFFFF))
                Text(membership.memberCode, fontSize = 12.sp, color = Color(0x99FFFFFF))
            }
        }
    }
}

// ── Progress card ─────────────────────────────────────────────────────────────

@Composable
private fun ProgressOverviewCard(membership: MembershipViewData) {
    val paid = membership.installments.count { it.status == InstallmentStatus.PAID }
    val total = membership.installments.size
    val progress = paid.toFloat() / total

    Column(
        modifier =
            Modifier.fillMaxWidth().shadow(
                1.dp,
                RoundedCornerShape(12.dp),
            ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Installments Paid", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = NavyMid, fontWeight = FontWeight.Bold, fontSize = 16.sp)) { append("$paid") }
                    withStyle(SpanStyle(color = SlateBody, fontSize = 13.sp)) { append("/$total") }
                },
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(SurfaceBlue)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth(
                        progress,
                    ).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(TealPaid, TealAccent))),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(progress * 100).toInt()}% complete", fontSize = 12.sp, color = TealAccent, fontWeight = FontWeight.SemiBold)
            val remaining = total - paid
            Text("$remaining installments remaining", fontSize = 12.sp, color = SlateBody)
        }
    }
}

// ── Calendar ──────────────────────────────────────────────────────────────────

@Composable
private fun CalendarLegend() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val items = listOf("Paid" to TealPaid, "Due" to AmberDue, "Overdue" to ErrorRed, "Upcoming" to UpcomingGray)
        items(items.size) { i ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(items[i].second))
                Text(items[i].first, fontSize = 11.sp, color = SlateBody)
            }
        }
    }
}

@Composable
private fun InstallmentCalendarGrid(
    installments: List<InstallmentCell>,
    onCellTap: (InstallmentCell) -> Unit,
) {
    // Non-scrollable grid — parent LazyColumn handles scrolling
    val rows = (installments.size + 5) / 6 // 6 per row
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0..5) {
                    val index = row * 6 + col
                    if (index < installments.size) {
                        val cell = installments[index]
                        val (bg, textColor, borderColor) =
                            when (cell.status) {
                                InstallmentStatus.PAID -> Triple(TealPaid, Color.White, TealPaid)
                                InstallmentStatus.DUE -> Triple(AmberBg, AmberDue, AmberDue)
                                InstallmentStatus.OVERDUE -> Triple(ErrorBg, ErrorRed, ErrorRed)
                                InstallmentStatus.UPCOMING -> Triple(SurfaceBlue, SlateBody, Color(0xFFCDD0D9))
                            }
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = cell.status == InstallmentStatus.PAID) { onCellTap(cell) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    cell.monthNumber.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                )
                                if (cell.status == InstallmentStatus.PAID) {
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp),
                                    )
                                } else if (cell.status == InstallmentStatus.DUE || cell.status == InstallmentStatus.OVERDUE) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(10.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty spacer to fill row
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

// ── Payout history ────────────────────────────────────────────────────────────

@Composable
private fun EmptyPayoutState() {
    Box(
        modifier =
            Modifier.fillMaxWidth().shadow(
                1.dp,
                RoundedCornerShape(12.dp),
            ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = SlateBody, modifier = Modifier.size(32.dp))
            Text("No payout received yet", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Text(
                "Active members may receive a payout when a chit cycle completes.",
                fontSize = 12.sp,
                color = SlateBody,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun PayoutHistoryRow(payout: PayoutRecord) {
    Row(
        modifier =
            Modifier.fillMaxWidth().shadow(
                1.dp,
                RoundedCornerShape(10.dp),
            ).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(PaidGreenBg), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Savings, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(payout.date, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Text("Ref: ${payout.bankReference}", fontSize = 11.sp, color = SlateBody)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(payout.amount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PaidGreen)
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(PaidGreenBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(payout.status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PaidGreen)
            }
        }
    }
}

// ── Bottom nav ────────────────────────────────────────────────────────────────

@Composable
private fun GroupMemberBottomNav() {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        listOf(
            Icons.Outlined.Home to "Home",
            Icons.Outlined.AccountBalance to "My Chits",
            Icons.Outlined.Payments to "Payments",
            Icons.Outlined.SmartToy to "Assistant",
            Icons.Outlined.Person to "Profile",
        ).forEachIndexed {
                index,
                (icon, label),
            ->
            NavigationBarItem(
                selected = index == 1,
                onClick = {
                },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
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

// ── Sample data ───────────────────────────────────────────────────────────────

val sampleMembershipView =
    MembershipViewData(
        groupName = "Family Savings 50K",
        memberCode = "Member #07",
        chitValue = "₹50,000",
        monthlyInstallment = "₹2,500",
        durationMonths = 24,
        currentStatus = "Active",
        installments =
            (1..24).map { month ->
                InstallmentCell(
                    monthNumber = month,
                    dueDate = "15/${month.toString().padStart(2, '0')}/2026",
                    amount = "₹2,500",
                    status =
                        when {
                            month <= 6 -> InstallmentStatus.PAID
                            month == 7 -> InstallmentStatus.OVERDUE
                            month == 8 -> InstallmentStatus.DUE
                            else -> InstallmentStatus.UPCOMING
                        },
                    paymentId = if (month <= 6) "PAY-2026-$month" else null,
                    paidDate = if (month <= 6) "15/${month.toString().padStart(2, '0')}/2026" else null,
                )
            },
        payoutHistory =
            listOf(
                // Empty for active members who haven't received a payout yet
                // Uncomment to test the payout row:
                // PayoutRecord("18 Aug 2026", "₹23,750", "HDFC202608189872", "Credited")
            ),
    )

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Group Member View")
@Composable
fun GroupMemberViewScreenPreview() {
    MaterialTheme { GroupMemberViewScreen() }
}
