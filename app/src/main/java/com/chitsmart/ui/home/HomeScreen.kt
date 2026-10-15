package com.chitsmart.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// ── Colour tokens from Figma (node 1:467) ────────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyMid       = Color(0xFF002141)
private val TealAccent    = Color(0xFF00B3A1)
private val TealDark      = Color(0xFF003E37)
private val BlueAccent    = Color(0xFF0261A2)
private val SlateBody     = Color(0xFF43474F)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val PaidGreen     = Color(0xFF1B6E55)
private val PaidGreenBg   = Color(0xFFE6FAF3)
private val NoticeBlue    = Color(0xFF0261A2)
private val CardGradStart = Color(0xFF0B3D7A)
private val CardGradEnd   = Color(0xFF1769AA)
private val AiBannerStart = Color(0xFF003E37)
private val AiBannerEnd   = Color(0xFF006B5E)
private val SmartBadge    = Color(0xFF00B3A1)

// ── Data ──────────────────────────────────────────────────────────────────────

data class RecentTransaction(
    val groupName: String,
    val date: String,
    val monthLabel: String,
    val amount: String,
    val status: String,
)

data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val labelLine2: String = "",
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    memberName: String = "Priya",
    memberId: String = "CS-8402",
    totalInvested: String = "₹1,20,000",
    currentDue: String = "₹5,000",
    dueDate: String = "15/10/2026",
    activeGroups: Int = 3,
    onPayNow: () -> Unit = {},
    onPayInstallment: () -> Unit = {},
    onMyStatement: () -> Unit = {},
    onDownloadReceipt: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    onAiAssistant: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onViewAllTransactions: () -> Unit = {},
    recentTransactions: List<RecentTransaction> = sampleTransactions,
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            HomeTopBar(
                memberName = memberName,
                memberId = memberId,
                language = "EN",
                onNotifications = onNotifications,
            )
        },
        bottomBar = { HomeBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Member greeting ──
            item {
                Spacer(Modifier.height(12.dp))
                MemberGreetingCard(
                    greeting = greeting,
                    memberName = memberName,
                    activeGroups = activeGroups,
                )
            }

            // ── Investment summary card ──
            item {
                Spacer(Modifier.height(12.dp))
                InvestmentSummaryCard(
                    totalInvested = totalInvested,
                    activeGroups = activeGroups,
                    currentDue = currentDue,
                    dueDate = dueDate,
                    onPayNow = onPayNow,
                )
            }

            // ── Quick actions ──
            item {
                Spacer(Modifier.height(20.dp))
                QuickActionsSection(
                    onPayInstallment = onPayInstallment,
                    onMyStatement = onMyStatement,
                    onDownloadReceipt = onDownloadReceipt,
                    onContactSupport = onContactSupport,
                )
            }

            // ── AI assistant banner ──
            item {
                Spacer(Modifier.height(16.dp))
                AiAssistantBanner(onClick = onAiAssistant)
            }

            // ── Notice card ──
            item {
                Spacer(Modifier.height(12.dp))
                NoticeCard()
            }

            // ── Recent transactions ──
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Recent Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDeep,
                    )
                    Text(
                        "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BlueAccent,
                        modifier = Modifier.clickable { onViewAllTransactions() },
                    )
                }
            }

            items(recentTransactions.size) { i ->
                val tx = recentTransactions[i]
                Spacer(Modifier.height(8.dp))
                TransactionRow(transaction = tx)
            }

            // ── Security footer ──
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceBlue)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "256-bit Bank Grade Security & Chit Fund Act Compliant",
                        fontSize = 10.sp,
                        color = SlateBody,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    memberName: String,
    memberId: String,
    language: String,
    onNotifications: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ChitSmart logo
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF1F3FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("CS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "CHITSMART",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        "Home",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDeep,
                    )
                }
            }
        },
        actions = {
            // Language toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFE8EDFF))
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            ) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(NavyMid).padding(horizontal = 8.dp, vertical = 3.dp),
                ) { Text("EN", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold) }
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                ) { Text("हिंदी", fontSize = 10.sp, color = SlateBody) }
            }
            Spacer(Modifier.width(8.dp))
            // Notification bell
            Box {
                IconButton(onClick = onNotifications) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications", tint = NavyDeep)
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("2", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            // Profile avatar
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFD7E2FF), CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    memberName.first().toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        },
    )
}

// ── Member greeting card ──────────────────────────────────────────────────────

@Composable
private fun MemberGreetingCard(greeting: String, memberName: String, activeGroups: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFD7E2FF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(memberName.first().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "MEMBER #CS-8402",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = BlueAccent,
                letterSpacing = 0.5.sp,
            )
            Text(
                "$greeting, $memberName 👋",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(TealAccent),
                )
                Text(
                    "All $activeGroups circles active & healthy",
                    fontSize = 11.sp,
                    color = SlateBody,
                )
            }
        }
    }
}

// ── Investment summary card ───────────────────────────────────────────────────

@Composable
private fun InvestmentSummaryCard(
    totalInvested: String,
    activeGroups: Int,
    currentDue: String,
    dueDate: String,
    onPayNow: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(colors = listOf(CardGradStart, CardGradEnd))
            )
            .padding(16.dp),
    ) {
        Column {
            // Header badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x26FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = Color(0xFF71F8E4), modifier = Modifier.size(12.dp))
                    Text("Govt. Registered Chit Circles", fontSize = 10.sp, color = Color(0xFF71F8E4), fontWeight = FontWeight.SemiBold)
                }
                Text("Updated Today", fontSize = 10.sp, color = Color(0x99FFFFFF))
            }

            Spacer(Modifier.height(12.dp))

            // Two columns: Total Invested | Current Month Due
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Invested", fontSize = 11.sp, color = Color(0xB3FFFFFF))
                    Text(
                        totalInvested,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        "Across $activeGroups chit groups",
                        fontSize = 10.sp,
                        color = Color(0x80FFFFFF),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current Month Due", fontSize = 11.sp, color = Color(0xB3FFFFFF))
                    Text(
                        currentDue,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color(0x99FFFFFF), modifier = Modifier.size(10.dp))
                        Text("Due $dueDate", fontSize = 10.sp, color = Color(0x99FFFFFF))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Group chips + Pay Now button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("50k", "1L", "25k").forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x26FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(label, fontSize = 10.sp, color = Color(0xCCFFFFFF))
                        }
                    }
                }
                Button(
                    onClick = onPayNow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Pay Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ── Quick actions ─────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    onPayInstallment: () -> Unit,
    onMyStatement: () -> Unit,
    onDownloadReceipt: () -> Unit,
    onContactSupport: () -> Unit,
) {
    val actions = listOf(
        QuickAction(Icons.Outlined.Payments, "Pay", "Installment"),
        QuickAction(Icons.Outlined.Description, "My", "Statement"),
        QuickAction(Icons.Outlined.Download, "Download", "Receipt"),
        QuickAction(Icons.Outlined.SupportAgent, "Contact", "Support"),
    )
    val callbacks = listOf(onPayInstallment, onMyStatement, onDownloadReceipt, onContactSupport)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            "Quick Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyDeep,
        )
        Text(
            "Instant Access",
            fontSize = 12.sp,
            color = BlueAccent,
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        actions.forEachIndexed { i, action ->
            Column(
                modifier = Modifier
                    .clickable { callbacks[i]() }
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(1.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        action.icon,
                        contentDescription = action.label,
                        tint = NavyMid,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    buildAnnotatedString {
                        append(action.label)
                        if (action.labelLine2.isNotEmpty()) {
                            append("\n")
                            append(action.labelLine2)
                        }
                    },
                    fontSize = 11.sp,
                    color = SlateBody,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

// ── AI assistant banner ───────────────────────────────────────────────────────

@Composable
private fun AiAssistantBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = listOf(AiBannerStart, AiBannerEnd)))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFF71F8E4), modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Ask ChitSmart AI anything",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 24.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Check payout eligibility, due dates & Chit Fund rules in your language",
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF),
                    lineHeight = 16.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Start conversation ›",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF71F8E4),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33FFFFFF))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("SMART", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealAccent, letterSpacing = 0.5.sp)
            }
        }
    }
}

// ── Notice card ───────────────────────────────────────────────────────────────

@Composable
private fun NoticeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFD7E2FF)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Groups, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NoticeBlue))
                Text("NOTICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NoticeBlue, letterSpacing = 0.4.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "New Chit Groups starting November",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                lineHeight = 18.sp,
            )
            Text(
                "Pre-register early to lock preferential dividend slots",
                fontSize = 11.sp,
                color = SlateBody,
                lineHeight = 15.sp,
            )
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SlateBody, modifier = Modifier.size(20.dp))
    }
}

// ── Transaction row ───────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(transaction: RecentTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PaidGreenBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PaidGreen, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.groupName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Text("${transaction.date} • ${transaction.monthLabel}", fontSize = 11.sp, color = SlateBody)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(transaction.amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PaidGreenBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    transaction.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PaidGreen,
                )
            }
        }
    }
}

// ── Bottom navigation ─────────────────────────────────────────────────────────

@Composable
private fun HomeBottomNav() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp),
    ) {
        val items = listOf(
            Icons.Outlined.Home to "Home",
            Icons.Outlined.AccountBalance to "My Chits",
            Icons.Outlined.Payments to "Payments",
            Icons.Outlined.SmartToy to "Assistant",
            Icons.Outlined.Person to "Profile",
        )
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = index == 0, // Home is selected
                onClick = {},
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyMid,
                    selectedTextColor = NavyMid,
                    indicatorColor = Color(0xFFF1F3FF),
                    unselectedIconColor = SlateBody,
                    unselectedTextColor = SlateBody,
                ),
            )
        }
    }
}

// ── Sample data ───────────────────────────────────────────────────────────────

private val sampleTransactions = listOf(
    RecentTransaction("Family Savings 50K", "12/10/2026", "Month 6", "₹2,500", "Paid"),
    RecentTransaction("Community Chit 1L", "10/09/2026", "Month 3", "₹5,000", "Paid"),
    RecentTransaction("Monthly Circle 25K", "15/08/2026", "Month 8", "₹1,250", "Paid"),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
