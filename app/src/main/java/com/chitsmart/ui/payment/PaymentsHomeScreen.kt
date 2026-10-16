package com.chitsmart.ui.payment

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens from Figma (node 1:717) ────────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyBanner = Color(0xFF073763)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val BlueAccent = Color(0xFF0261A2)
private val SlateBody = Color(0xFF43474F)
private val SlateBlue = Color(0xFF7DA1D3)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val InputPill = Color(0xFFE8EDFF)
private val ErrorRed = Color(0xFFBA1A1A)
private val ErrorBg = Color(0xFFFFDAD6)
private val ErrorText = Color(0xFF93000A)
private val PaidGreen = Color(0xFF1B6E55)
private val PaidGreenBg = Color(0xFFE6FAF3)
private val TealCTA = TealAccent

data class InstallmentDue(
    val groupName: String,
    val chitValue: String,
    val installmentNo: Int,
    val totalInstallments: Int,
    val monthlyAmount: String,
    val lateFee: String?,
    val totalDue: String,
    val dueDate: String,
    val isOverdue: Boolean,
    val overdueDays: Int = 0,
    val isPaid: Boolean = false,
    val paidDate: String? = null,
)

@Composable
fun PaymentsHomeScreen(
    dues: List<InstallmentDue> = sampleDues,
    onPayDue: (InstallmentDue) -> Unit = {},
    onViewHistory: () -> Unit = {},
    onViewPayouts: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    val filters = listOf("All Dues (2)", "Due Today", "Overdue (1)", "Paid This Month (1)")
    var selectedFilter by remember { mutableIntStateOf(0) }

    val totalDue = "₹7,550"

    Scaffold(
        containerColor = PageBg,
        topBar = { PaymentsTopBar(onNotifications = onNotifications) },
        bottomBar = { PaymentsBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Overview banner ──
            item {
                Spacer(Modifier.height(0.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyBanner)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                ) {
                    // Decorative blur circle
                    Box(
                        modifier =
                            Modifier
                                .size(128.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 24.dp, y = 24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0261A2).copy(alpha = 0.2f)),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "TOTAL PAYABLE DUES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateBlue,
                                letterSpacing = 0.275.sp,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                totalDue,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = SlateBlue,
                                    modifier = Modifier.size(11.dp),
                                )
                                Text("Across 2 active groups this cycle", fontSize = 11.sp, color = SlateBlue, letterSpacing = 0.33.sp)
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1E4FF).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // ── Filter chips ──
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters.size) { i ->
                        val selected = selectedFilter == i
                        Row(
                            modifier =
                                Modifier
                                    .shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(100.dp))
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (selected) NavyMid else InputPill)
                                    .clickable { selectedFilter = i }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (i == 2) { // Overdue
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (selected) Color.White else ErrorRed))
                            }
                            Text(
                                filters[i],
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) Color.White else SlateBody,
                                letterSpacing = 0.24.sp,
                            )
                        }
                    }
                }
            }

            // ── Section header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Active Installment Dues", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text("Pay All →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.33.sp)
                }
            }

            // ── Due cards ──
            items(dues.size) { i ->
                DueInstallmentCard(due = dues[i], onPay = { onPayDue(dues[i]) })
            }

            // ── Quick access section ──
            item {
                Text("Quick Access", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.History,
                        title = "Payment History",
                        subtitle = "View all transactions",
                        onClick = onViewHistory,
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "Payout History",
                        subtitle = "Received disbursements",
                        onClick = onViewPayouts,
                    )
                }
            }

            // ── Security footer ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("All transactions secured by RBI-compliant gateway", fontSize = 10.sp, color = SlateBody)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DueInstallmentCard(
    due: InstallmentDue,
    onPay: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                // Overdue / Due badge
                if (due.isOverdue) {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ErrorBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = ErrorText, modifier = Modifier.size(11.dp))
                        Text(
                            "Overdue by ${due.overdueDays} days",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorText,
                            letterSpacing = 0.33.sp,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(due.groupName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(
                    "Chit Value: ${due.chitValue} • Installment ${due.installmentNo} of ${due.totalInstallments}",
                    fontSize = 12.sp,
                    color = SlateBody,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Due Date", fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
                Text(
                    due.dueDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (due.isOverdue) ErrorRed else NavyDeep,
                    letterSpacing = 0.24.sp,
                )
            }
        }

        // Financial calculation row
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceBlue)
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Monthly Contribution", fontSize = 12.sp, color = SlateBody)
                Text(due.monthlyAmount, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
            }
            if (due.lateFee != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Late Fee", fontSize = 12.sp, color = ErrorRed)
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(11.dp))
                    }
                    Text("+ ${due.lateFee}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ErrorRed)
                }
            }
            // Total row
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8EDFF).copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Total Payable: ") }
                        withStyle(SpanStyle(color = NavyDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold)) { append(due.totalDue) }
                    },
                )
                // Pay CTA
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealCTA)
                            .clickable { onPay() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onClick() }
                .padding(14.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
        Text(subtitle, fontSize = 11.sp, color = SlateBody)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentsTopBar(onNotifications: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        title = {
            Column {
                Text("Payments", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Installments & Settlements", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
            }
        },
        actions = {
            Row(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(InputPill).padding(horizontal = 2.dp, vertical = 2.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(NavyMid).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("EN", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) { Text("हिंदी", fontSize = 10.sp, color = SlateBody) }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = onNotifications,
            ) { Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications", tint = NavyDeep) }
            Box(
                modifier =
                    Modifier.padding(
                        end = 8.dp,
                    ).size(44.dp).clip(CircleShape).border(2.dp, Color(0xFFD7E2FF), CircleShape).background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center,
            ) {
                Text("P", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
    )
}

@Composable
private fun PaymentsBottomNav() {
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
                selected = index == 2,
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

private val sampleDues =
    listOf(
        InstallmentDue("Family Savings 50K", "₹50,000", 7, 24, "₹2,500", "₹50", "₹2,550", "13/10/2026", isOverdue = true, overdueDays = 2),
        InstallmentDue("Community Chit 1L", "₹1,00,000", 15, 20, "₹5,000", null, "₹5,000", "20/10/2026", isOverdue = false),
    )

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Payments Home")
@Composable
fun PaymentsHomeScreenPreview() {
    MaterialTheme { PaymentsHomeScreen() }
}
