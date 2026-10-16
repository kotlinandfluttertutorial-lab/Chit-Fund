package com.chitsmart.ui.chits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Colour tokens (consistent with rest of project) ──────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val BlueAccent = Color(0xFF0261A2)
private val SlateBody = Color(0xFF43474F)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val InputPill = Color(0xFFE8EDFF)
private val ProgressTrack = Color(0xFFE0E8FF)
private val PaidGreen = Color(0xFF1B6E55)
private val PaidGreenBg = Color(0xFFE6FAF3)

// ── Data models ───────────────────────────────────────────────────────────────

data class GroupDetailData(
    val groupName: String,
    val chitValue: String,
    val monthlyInstallment: String,
    val durationMonths: Int,
    val maxMembers: Int,
    val enrolledMembers: Int,
    val foremanCommissionPct: String,
    val startDate: String,
    val groupManagerName: String,
    val description: String,
    val termsDocumentUrl: String?,
    val installmentSchedule: List<ScheduleRow>,
    val members: List<GroupMember>,
    val faqs: List<FAQ>,
    val isEnrolled: Boolean = false,
)

data class ScheduleRow(
    val monthNumber: Int,
    val dueDate: String,
    val amount: String,
    // "Upcoming" | "Due" | "Paid" | "Overdue"
    val status: String,
)

data class GroupMember(val memberCode: String, val isCurrentUser: Boolean = false)

data class FAQ(val question: String, val answer: String)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun GroupDetailsScreen(
    group: GroupDetailData = sampleGroupDetail,
    onBack: () -> Unit = {},
    onEnroll: () -> Unit = {},
) {
    val tabs = listOf("Overview", "Schedule", "Members", "FAQs")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 72.dp),
        ) {
            // ── Top bar ──
            item {
                GroupDetailsTopBar(groupName = group.groupName, onBack = onBack)
            }

            // ── Hero card ──
            item {
                HeroCard(group = group)
            }

            // ── Sticky tabs ──
            item {
                Column(modifier = Modifier.background(PageBg)) {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = PageBg,
                        contentColor = NavyMid,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = TealAccent,
                            )
                        },
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = pagerState.currentPage == i,
                                onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 14.sp,
                                        fontWeight = if (pagerState.currentPage == i) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                },
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE0E8FF))
                }
            }

            // ── Tab content ──
            item {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    when (page) {
                        0 -> OverviewTab(group = group)
                        1 -> ScheduleTab(schedule = group.installmentSchedule)
                        2 -> MembersTab(members = group.members, enrolled = group.enrolledMembers, max = group.maxMembers)
                        3 -> FAQsTab(faqs = group.faqs)
                    }
                }
            }
        }

        // ── Sticky Enroll Now CTA ──
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                onClick = onEnroll,
                enabled = !group.isEnrolled,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        disabledContainerColor = Color(0xFFB2DFDB),
                    ),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Text(
                    if (group.isEnrolled) "Already Enrolled" else "Enroll Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                if (!group.isEnrolled) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsTopBar(
    groupName: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep)
            }
        },
        title = {
            Text(groupName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, maxLines = 1)
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = NavyDeep)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = NavyDeep)
            }
        },
    )
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(group: GroupDetailData) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(colors = listOf(Color(0xFF0B3D7A), Color(0xFF1769AA))),
                )
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Group name + chit value
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(group.groupName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Chit Value", fontSize = 11.sp, color = Color(0xB3FFFFFF), letterSpacing = 0.33.sp)
                Text(group.chitValue, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF71F8E4), letterSpacing = (-0.5).sp)
            }
            Box(
                modifier =
                    Modifier.clip(
                        RoundedCornerShape(20.dp),
                    ).background(Color(0x26FFFFFF)).padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF71F8E4)))
                    Text("Active", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF71F8E4))
                }
            }
        }

        HorizontalDivider(color = Color(0x33FFFFFF))

        // Key stats grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            HeroStat("Monthly", group.monthlyInstallment)
            VerticalDivider(color = Color(0x33FFFFFF), modifier = Modifier.height(40.dp))
            HeroStat("Duration", "${group.durationMonths} months")
            VerticalDivider(color = Color(0x33FFFFFF), modifier = Modifier.height(40.dp))
            HeroStat("Commission", group.foremanCommissionPct)
        }

        // Enrollment progress
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${group.enrolledMembers} of ${group.maxMembers} seats filled",
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF),
                )
                Text(
                    "${((group.enrolledMembers.toFloat() / group.maxMembers) * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF71F8E4),
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x33FFFFFF)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(group.enrolledMembers.toFloat() / group.maxMembers)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF71F8E4)),
                )
            }
        }

        // Start date + manager
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color(0x99FFFFFF), modifier = Modifier.size(12.dp))
                Text("Started ${group.startDate}", fontSize = 11.sp, color = Color(0x99FFFFFF))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0x99FFFFFF), modifier = Modifier.size(12.dp))
                Text("Foreman: ${group.groupManagerName}", fontSize = 11.sp, color = Color(0x99FFFFFF))
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        Text(label, fontSize = 11.sp, color = Color(0xB3FFFFFF), letterSpacing = 0.33.sp, textAlign = TextAlign.Center)
    }
}

// ── Tab: Overview ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(group: GroupDetailData) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Description
        Column(
            modifier =
                Modifier.fillMaxWidth().shadow(
                    1.dp,
                    RoundedCornerShape(12.dp),
                ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("About This Group", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            Text(group.description, fontSize = 14.sp, color = SlateBody, lineHeight = 22.sp)
        }

        // Key stats detail
        Column(
            modifier =
                Modifier.fillMaxWidth().shadow(
                    1.dp,
                    RoundedCornerShape(12.dp),
                ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Key Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
            KeyDetailRow("Chit Value", group.chitValue)
            KeyDetailRow("Monthly Installment", group.monthlyInstallment)
            KeyDetailRow("Duration", "${group.durationMonths} months")
            KeyDetailRow("Max Members", group.maxMembers.toString())
            KeyDetailRow("Foreman Commission", group.foremanCommissionPct)
            KeyDetailRow("Start Date", group.startDate)
            KeyDetailRow("Foreman Name", group.groupManagerName)
        }

        // Terms document link
        group.termsDocumentUrl?.let {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp).clickable {},
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                Text(
                    "View Group Terms & Agreement",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueAccent,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun KeyDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = SlateBody)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
    }
}

// ── Tab: Schedule ─────────────────────────────────────────────────────────────

@Composable
private fun ScheduleTab(schedule: List<ScheduleRow>) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier =
                Modifier.fillMaxWidth().clip(
                    RoundedCornerShape(8.dp),
                ).background(SurfaceBlue).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Month", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, modifier = Modifier.width(50.dp))
            Text("Due Date", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, modifier = Modifier.weight(1f))
            Text(
                "Amount",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End,
            )
            Text(
                "Status",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep,
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End,
            )
        }
        schedule.forEach { row ->
            val (statusColor, statusBg) =
                when (row.status) {
                    "Paid" -> Pair(PaidGreen, PaidGreenBg)
                    "Due" -> Pair(Color(0xFF795500), Color(0xFFFFF8E1))
                    "Overdue" -> Pair(Color(0xFFBA1A1A), Color(0xFFFFDAD6))
                    else -> Pair(SlateBody, SurfaceBlue)
                }
            Row(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(8.dp),
                    ).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    row.monthNumber.toString().padStart(2, '0'),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    modifier = Modifier.width(50.dp),
                )
                Text(row.dueDate, fontSize = 12.sp, color = SlateBody, modifier = Modifier.weight(1f))
                Text(
                    row.amount,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End,
                )
                Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.CenterEnd) {
                    Box(
                        modifier =
                            Modifier.clip(
                                RoundedCornerShape(20.dp),
                            ).background(statusBg).padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(row.status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
            Text(
                "Schedule shows installment dates only. Auction dates are not applicable in this release.",
                fontSize = 11.sp,
                color = SlateBody,
                lineHeight = 15.sp,
            )
        }
    }
}

// ── Tab: Members ──────────────────────────────────────────────────────────────

@Composable
private fun MembersTab(
    members: List<GroupMember>,
    enrolled: Int,
    max: Int,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier =
                Modifier.fillMaxWidth().shadow(
                    1.dp,
                    RoundedCornerShape(12.dp),
                ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("$enrolled members enrolled", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("$max total seats in this group", fontSize = 12.sp, color = SlateBody)
            }
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(SurfaceBlue),
                contentAlignment = Alignment.Center,
            ) {
                Text("$enrolled/$max", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyMid, textAlign = TextAlign.Center)
            }
        }
        members.forEach { member ->
            Row(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(10.dp),
                    ).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier.size(
                            36.dp,
                        ).clip(CircleShape).background(if (member.isCurrentUser) TealAccent.copy(0.2f) else SurfaceBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = if (member.isCurrentUser) TealAccent else SlateBody,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    if (member.isCurrentUser) "${member.memberCode} (You)" else member.memberCode,
                    fontSize = 14.sp,
                    fontWeight = if (member.isCurrentUser) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (member.isCurrentUser) TealAccent else NavyDeep,
                )
                if (member.isCurrentUser) {
                    Box(
                        modifier =
                            Modifier.clip(
                                RoundedCornerShape(20.dp),
                            ).background(TealAccent.copy(0.1f)).padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("You", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TealAccent)
                    }
                }
            }
        }
    }
}

// ── Tab: FAQs ─────────────────────────────────────────────────────────────────

@Composable
private fun FAQsTab(faqs: List<FAQ>) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        faqs.forEach { faq ->
            var expanded by remember { mutableStateOf(false) }
            Column(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(10.dp),
                    ).clip(RoundedCornerShape(10.dp)).background(Color.White).clickable {
                        expanded = !expanded
                    },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        faq.question,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDeep,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp,
                    )
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = SlateBody,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (expanded) {
                    HorizontalDivider(color = Color(0xFFE0E8FF), modifier = Modifier.padding(horizontal = 14.dp))
                    Text(faq.answer, fontSize = 13.sp, color = SlateBody, lineHeight = 20.sp, modifier = Modifier.padding(14.dp))
                }
            }
        }
    }
}

// ── Sample data ───────────────────────────────────────────────────────────────

val sampleGroupDetail =
    GroupDetailData(
        groupName = "Family Savings 50K",
        chitValue = "₹50,000",
        monthlyInstallment = "₹2,500",
        durationMonths = 24,
        maxMembers = 20,
        enrolledMembers = 18,
        foremanCommissionPct = "5%",
        startDate = "01/04/2026",
        groupManagerName = "Ramesh Foreman",
        description =
            "A trusted rotational savings circle for families. Each member contributes ₹2,500 monthly " +
                "and gets the full chit value of ₹50,000 when it's their turn, minus the foreman " +
                "commission and any declared discount.",
        termsDocumentUrl = "https://chitsmart.in/terms/family-savings-50k",
        installmentSchedule =
            (1..8).map { month ->
                ScheduleRow(
                    monthNumber = month,
                    dueDate = "${15}/${month.toString().padStart(2, '0')}/2026",
                    amount = "₹2,500",
                    status =
                        when {
                            month <= 6 -> "Paid"
                            month == 7 -> "Overdue"
                            else -> "Upcoming"
                        },
                )
            },
        members =
            (1..18).map { i ->
                GroupMember(memberCode = "Member #${i.toString().padStart(2, '0')}", isCurrentUser = i == 7)
            },
        faqs =
            listOf(
                FAQ(
                    "What happens if I miss an installment?",
                    "A late fee of ₹50 per day applies after the grace period of 3 days. Continued " +
                        "non-payment may result in suspension per Chit Funds Act, 1982.",
                ),
                FAQ(
                    "How is the payout amount calculated?",
                    "Payout = Chit Value − Foreman Commission (5%) − Declared Discount (if applicable). " +
                        "Your net payout is shown before you confirm enrollment.",
                ),
                FAQ(
                    "When will I receive my payout?",
                    "Payouts are disbursed via NEFT/IMPS to your registered bank account within 3 " +
                        "working days of a completed payout cycle.",
                ),
                FAQ(
                    "Can I exit the group before it ends?",
                    "Early exit is subject to foreman approval and applicable penalties as per the " +
                        "group agreement. Contact your foreman for details.",
                ),
            ),
    )

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Group Details")
@Composable
fun GroupDetailsScreenPreview() {
    MaterialTheme { GroupDetailsScreen() }
}
