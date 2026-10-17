package com.chitsmart.ui.chits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens from Figma (node 1:1737) ───────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val BlueValue = Color(0xFF00487A)
private val BlueAccent = Color(0xFF0261A2)
private val TealAccent = Color(0xFF00B3A1)
private val SlateBody = Color(0xFF43474F)
private val SlateHint = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val InputPill = Color(0xFFE8EDFF)
private val ErrorRed = Color(0xFFBA1A1A)
private val FullBadgeBg = Color(0xFFFFDAD6)

data class ChitGroup(
    val name: String,
    val chitValue: String,
    val monthlyInstallment: String,
    val duration: String,
    val enrolledMembers: Int,
    val totalMembers: Int,
    val enrollmentPct: Float,
    val nextDueDate: String,
    val isFull: Boolean = false,
    val tags: List<String> = emptyList(),
)

@Composable
fun BrowseGroupsScreen(
    groups: List<ChitGroup> = sampleGroups,
    onEnroll: (ChitGroup) -> Unit = {},
    onKnowMore: (ChitGroup) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var search by remember { mutableStateOf("") }
    val filters = listOf("All", "₹25K", "₹50K", "₹1L", "₹5L", "6M", "12M", "24M")
    var selectedFilter by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            BrowseGroupsTopBar(
                search = search,
                onSearchChange = { search = it },
                onBack = onBack,
            )
        },
        bottomBar = { BrowseBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Header
            item {
                Spacer(Modifier.height(8.dp))
                Column {
                    Text(
                        "Explore Chit Groups",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDeep,
                        letterSpacing = (-0.6).sp,
                        lineHeight = 32.sp,
                    )
                    Text("Find the right circle for your savings goal", fontSize = 14.sp, color = SlateBody, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                    items(filters.size) { i ->
                        val selected = selectedFilter == i
                        Box(
                            modifier =
                                Modifier
                                    .shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(100.dp))
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (selected) NavyMid else InputPill)
                                    .clickable { selectedFilter = i }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
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
                Spacer(Modifier.height(16.dp))
            }

            // Referral promo card (teal gradient from Figma)
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(colors = listOf(Color(0xFF4FDBC8), Color(0xFF00B3A1), Color(0xFF003E37))))
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Refer & Earn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Invite a friend and earn ₹500 credit", fontSize = 12.sp, color = Color.White.copy(0.85f))
                    }
                    Row(
                        modifier =
                            Modifier.clip(
                                RoundedCornerShape(8.dp),
                            ).background(Color.White.copy(0.2f)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Group cards
            items(groups.size) { i ->
                if (i > 0) Spacer(Modifier.height(16.dp))
                ChitGroupCard(group = groups[i], onEnroll = { onEnroll(groups[i]) }, onKnowMore = { onKnowMore(groups[i]) })
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ChitGroupCard(
    group: ChitGroup,
    onEnroll: () -> Unit,
    onKnowMore: () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth().shadow(
                1.dp,
                RoundedCornerShape(12.dp),
            ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, lineHeight = 22.sp)
                Spacer(Modifier.height(2.dp))
                Text("Chit Value", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.33.sp)
            }
            if (group.isFull) {
                Box(
                    modifier =
                        Modifier.clip(
                            RoundedCornerShape(20.dp),
                        ).background(FullBadgeBg).padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("FULL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed, letterSpacing = 0.55.sp)
                }
            } else {
                Box(
                    modifier =
                        Modifier.clip(
                            RoundedCornerShape(20.dp),
                        ).background(SurfaceBlue).padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Open", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.33.sp)
                }
            }
        }

        // Stats grid: chit value + monthly + duration + members
        Row(modifier = Modifier.fillMaxWidth()) {
            StatBox("Chit Value", group.chitValue, modifier = Modifier.weight(1f), valueColor = NavyMid, valueFontSize = 20.sp)
            StatBox("Monthly", group.monthlyInstallment, modifier = Modifier.weight(1f))
            StatBox("Duration", group.duration, modifier = Modifier.weight(0.7f))
        }

        // Next due date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Next installment due: ") }
                    withStyle(SpanStyle(color = NavyDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append(group.nextDueDate) }
                },
            )
        }

        // Enrollment tracker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Seats Filled:", fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = NavyMid, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)) {
                            append("${group.enrolledMembers}/${group.totalMembers}")
                        }
                        withStyle(SpanStyle(color = SlateBody, fontSize = 11.sp)) { append(" (${(group.enrollmentPct * 100).toInt()}%)") }
                    },
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE0E8FF))) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth(
                            group.enrollmentPct,
                        ).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(if (group.isFull) ErrorRed else TealAccent),
                )
            }
        }

        // CTA buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onKnowMore,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent),
            ) {
                Text("Know More", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.14.sp)
            }
            Button(
                onClick = onEnroll,
                enabled = !group.isFull,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (group.isFull) Color(0xFFE0E8FF) else NavyMid,
                        disabledContainerColor = Color(0xFFE0E8FF),
                    ),
            ) {
                Text(
                    if (group.isFull) "Group Full" else "Enroll Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (group.isFull) SlateBody else Color.White,
                    letterSpacing = 0.14.sp,
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF041B3C),
    valueFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.33.sp)
        Text(value, fontSize = valueFontSize, fontWeight = FontWeight.Bold, color = valueColor, lineHeight = 24.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseGroupsTopBar(
    search: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
        title = {
            Row(
                modifier =
                    Modifier.fillMaxWidth().height(
                        40.dp,
                    ).clip(
                        RoundedCornerShape(100.dp),
                    ).background(Color.White).border(1.dp, Color(0xFFE0E8FF), RoundedCornerShape(100.dp)).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = SlateHint, modifier = Modifier.size(18.dp))
                BasicTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    modifier =
                        Modifier.weight(
                            1f,
                        ),
                    textStyle = TextStyle(fontSize = 14.sp, color = NavyDeep),
                    cursorBrush = SolidColor(TealAccent),
                    decorationBox = {
                            inner ->
                        if (search.isEmpty()) Text("Search chit groups...", fontSize = 14.sp, color = SlateHint)
                        inner()
                    },
                )
            }
        },
        actions = {
            Box(
                modifier =
                    Modifier.size(
                        48.dp,
                    ).shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = NavyDeep, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
        },
    )
}

@Composable
private fun BrowseBottomNav() {
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

private val sampleGroups =
    listOf(
        ChitGroup("Family Savings 50K", "₹50,000", "₹2,500", "24M", 18, 20, 0.90f, "15/10/2026"),
        ChitGroup("Community Chit 1L", "₹1,00,000", "₹5,000", "20M", 15, 17, 0.88f, "20/10/2026", tags = listOf("Popular")),
        ChitGroup("Monthly Circle 25K", "₹25,000", "₹1,250", "24M", 20, 20, 1.0f, "N/A", isFull = true),
    )

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Browse Groups")
@Composable
fun BrowseGroupsPreview() {
    MaterialTheme { BrowseGroupsScreen() }
}
