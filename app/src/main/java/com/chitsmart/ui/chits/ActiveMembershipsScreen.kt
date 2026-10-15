package com.chitsmart.ui.chits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

// ── Colour tokens from Figma (node 1:263) ────────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyMid       = Color(0xFF002141)
private val NavyButton    = Color(0xFF002141)
private val TealAccent    = Color(0xFF00B3A1)
private val TealBadgeBg   = Color(0xFFE6FAF6)
private val BlueAccent    = Color(0xFF0261A2)
private val SlateBody     = Color(0xFF43474F)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val ProgressTrack = Color(0xFFE0E8FF)
private val GoldBg        = Color(0xFFFFF8E1)
private val GoldText      = Color(0xFF795500)
private val CompletedGray = Color(0xFF6B7280)
private val CertBlue      = BlueAccent
private val BadgeLight    = Color(0xFFD3E3FF)

// ── Data models ───────────────────────────────────────────────────────────────

enum class MembershipStatus { ACTIVE, COMPLETED, PENDING_ENROLLMENT }

data class Membership(
    val groupName: String,
    val chitValue: String,
    val monthlyContribution: String,
    val installmentsPaid: Int,
    val installmentsTotal: Int,
    val progressPercent: Float,
    val nextDueDate: String?,
    val status: MembershipStatus,
    val payoutReceived: Boolean,
    val payoutAmount: String?,
    val payoutDate: String?,
    val completedDetails: String? = null,
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ActiveMembershipsScreen(
    memberships: List<Membership> = sampleMemberships,
    onPayNow: (Membership) -> Unit = {},
    onViewDetails: (Membership) -> Unit = {},
    onDownloadCertificate: (Membership) -> Unit = {},
    onBrowseGroups: () -> Unit = {},
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    val filterOptions = listOf("All (${memberships.size})", "Active (${memberships.count { it.status == MembershipStatus.ACTIVE }})", "Completed (${memberships.count { it.status == MembershipStatus.COMPLETED }})")
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filtered = when (selectedFilter) {
        1 -> memberships.filter { it.status == MembershipStatus.ACTIVE }
        2 -> memberships.filter { it.status == MembershipStatus.COMPLETED }
        else -> memberships
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            ActiveMembershipsTopBar(onBack = onBack, onNotifications = onNotifications)
        },
        bottomBar = { ActiveMembershipsBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // ── Filter chips ──
            item {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    items(filterOptions.size) { i ->
                        val selected = selectedFilter == i
                        Box(
                            modifier = Modifier
                                .shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(100.dp))
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) NavyButton else SurfaceBlue)
                                .clickable { selectedFilter = i }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                filterOptions[i],
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) Color.White else NavyDeep,
                                letterSpacing = 0.24.sp,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Membership cards ──
            items(filtered.size) { i ->
                val membership = filtered[i]
                if (i > 0) Spacer(Modifier.height(14.dp))
                when (membership.status) {
                    MembershipStatus.ACTIVE -> ActiveMembershipCard(
                        membership = membership,
                        onPayNow = { onPayNow(membership) },
                        onViewDetails = { onViewDetails(membership) },
                    )
                    MembershipStatus.COMPLETED -> CompletedMembershipCard(
                        membership = membership,
                        onDownloadCertificate = { onDownloadCertificate(membership) },
                    )
                    else -> {}
                }
            }

            // ── Trust indicator ──
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "RBI Regulated • State Licensed Foreman • Escrow Protected",
                        fontSize = 10.sp,
                        color = SlateBody,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // ── Explore more CTA ──
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBrowseGroups,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyButton),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Explore More Chit Groups",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Active membership card ────────────────────────────────────────────────────

@Composable
private fun ActiveMembershipCard(
    membership: Membership,
    onPayNow: () -> Unit,
    onViewDetails: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onViewDetails() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Card header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // Group name + chit value
            Column {
                Text(
                    membership.groupName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp, fontWeight = FontWeight.Normal)) { append("Chit Value: ") }
                        withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append(membership.chitValue) }
                    },
                )
            }
            // Active badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(TealBadgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    "Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealAccent,
                    letterSpacing = 0.33.sp,
                )
            }
        }

        // ── Key stats row ──
        Row(modifier = Modifier.fillMaxWidth()) {
            // Monthly contribution
            StatColumn(
                label = "Monthly Contribution",
                value = membership.monthlyContribution,
                modifier = Modifier.weight(1f),
            )
            // Payout received
            Column(modifier = Modifier.weight(1f)) {
                Text("Payout Received", fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
                if (membership.payoutReceived && membership.payoutAmount != null) {
                    Text(membership.payoutAmount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text("Paid on ${membership.payoutDate}", fontSize = 10.sp, color = SlateBody)
                } else {
                    Text("(Not yet drawn)", fontSize = 12.sp, color = SlateBody)
                }
            }
        }

        // ── Progress section ──
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Progress label row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Installments Paid: ") }
                        withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                            append("${membership.installmentsPaid}/${membership.installmentsTotal}")
                        }
                    },
                )
                Text(
                    "${(membership.progressPercent * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyMid,
                    letterSpacing = 0.33.sp,
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ProgressTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(membership.progressPercent)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(TealAccent, Color(0xFF14B8A6)),
                            )
                        ),
                )
            }

            // Due date + Pay button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                membership.nextDueDate?.let { due ->
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Due: ") }
                            withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append(due) }
                        },
                    )
                }
                Row(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealAccent)
                        .clickable { onPayNow() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(
                        "Pay ${membership.monthlyContribution}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 0.24.sp,
                    )
                }
            }
        }
    }
}

// ── Completed membership card ─────────────────────────────────────────────────

@Composable
private fun CompletedMembershipCard(
    membership: Membership,
    onDownloadCertificate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.9f)
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(membership.groupName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Chit Value: ") }
                        withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append(membership.chitValue) }
                    },
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceBlue)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Completed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CompletedGray, letterSpacing = 0.33.sp)
            }
        }

        // Status details box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceBlue)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Cycle Status", fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
                Text(
                    "Completed ${membership.installmentsTotal}/${membership.installmentsTotal} months",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                )
                Text(
                    membership.completedDetails ?: "Prize successfully settled",
                    fontSize = 12.sp,
                    color = SlateBody,
                    lineHeight = 16.sp,
                )
            }
            Text(
                "Download Completion\nCertificate",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CertBlue,
                letterSpacing = 0.24.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .clickable { onDownloadCertificate() }
                    .padding(start = 8.dp),
            )
        }

        // Rotational savings badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFF0B3D7A), Color(0xFF1769AA)))
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = BadgeLight, modifier = Modifier.size(14.dp))
            Text(
                "ROTATIONAL SAVINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = BadgeLight,
                letterSpacing = 0.55.sp,
            )
            Text("•", color = BadgeLight.copy(alpha = 0.6f))
            Text(
                "This cycle successfully completed",
                fontSize = 11.sp,
                color = BadgeLight.copy(alpha = 0.8f),
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveMembershipsTopBar(onBack: () -> Unit, onNotifications: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg.copy(alpha = 0.8f)),
        title = {
            Column {
                Text("My Chits", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Active Circles & Memberships", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
            }
        },
        actions = {
            // Language toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(SurfaceBlue)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            ) {
                Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(NavyMid).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("EN", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("हिंदी", fontSize = 10.sp, color = SlateBody)
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onNotifications) {
                Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications", tint = NavyDeep)
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
                Text("P", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
    )
}

// ── Bottom nav ────────────────────────────────────────────────────────────────

@Composable
private fun ActiveMembershipsBottomNav() {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        val items = listOf(Icons.Outlined.Home to "Home", Icons.Outlined.AccountBalance to "My Chits", Icons.Outlined.Payments to "Payments", Icons.Outlined.SmartToy to "Assistant", Icons.Outlined.Person to "Profile")
        items.forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = index == 1,
                onClick = {},
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyMid, selectedTextColor = NavyMid,
                    indicatorColor = SurfaceBlue, unselectedIconColor = SlateBody, unselectedTextColor = SlateBody,
                ),
            )
        }
    }
}

// ── Sample data matching Figma exactly ───────────────────────────────────────

val sampleMemberships = listOf(
    Membership(
        groupName = "Family Savings 50K",
        chitValue = "₹50,000",
        monthlyContribution = "₹2,500",
        installmentsPaid = 6,
        installmentsTotal = 24,
        progressPercent = 0.25f,
        nextDueDate = "15/10/2026",
        status = MembershipStatus.ACTIVE,
        payoutReceived = false,
        payoutAmount = null,
        payoutDate = null,
    ),
    Membership(
        groupName = "Community Chit 1L",
        chitValue = "₹1,00,000",
        monthlyContribution = "₹5,000",
        installmentsPaid = 14,
        installmentsTotal = 20,
        progressPercent = 0.70f,
        nextDueDate = "20/10/2026",
        status = MembershipStatus.ACTIVE,
        payoutReceived = true,
        payoutAmount = "₹82,500",
        payoutDate = "12/04/2026",
    ),
    Membership(
        groupName = "Monthly Circle 25K",
        chitValue = "₹25,000",
        monthlyContribution = "₹1,250",
        installmentsPaid = 20,
        installmentsTotal = 20,
        progressPercent = 1.0f,
        nextDueDate = null,
        status = MembershipStatus.COMPLETED,
        payoutReceived = true,
        payoutAmount = "₹23,750",
        payoutDate = "18/08/2026",
        completedDetails = "Prize successfully settled",
    ),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Active Memberships")
@Composable
fun ActiveMembershipsScreenPreview() {
    MaterialTheme { ActiveMembershipsScreen() }
}
