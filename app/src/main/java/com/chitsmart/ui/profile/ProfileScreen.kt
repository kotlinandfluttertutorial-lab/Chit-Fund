package com.chitsmart.ui.profile

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

// ── Colour tokens from Figma (node 1:2753) ───────────────────────────────────
private val PageBg       = Color(0xFFF9F9FF)
private val NavyDeep     = Color(0xFF041B3C)
private val NavyMid      = Color(0xFF002141)
private val TealAccent   = Color(0xFF00B3A1)
private val BlueAccent   = Color(0xFF0261A2)
private val ErrorRed     = Color(0xFFBA1A1A)
private val SlateBody    = Color(0xFF43474F)
private val SlateHint    = Color(0xFF737780)
private val SurfaceBlue  = Color(0xFFF1F3FF)

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badge: String? = null,
    val badgeColor: Color = TealAccent,
    val isDestructive: Boolean = false,
    val route: String = "",
)

data class ProfileSection(val sectionTitle: String, val items: List<ProfileMenuItem>)

@Composable
fun ProfileScreen(
    memberName: String = "Priya Sharma",
    memberId: String = "CS-8402",
    mobile: String = "+91 98765 43210",
    email: String = "priya.sharma@example.com",
    activeCircles: Int = 3,
    totalInvested: String = "₹1,20,000",
    memberSince: String = "Mar 2026",
    onMenuItemTap: (ProfileMenuItem) -> Unit = {},
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val sections = listOf(
        ProfileSection("ACCOUNT & BANKING", listOf(
            ProfileMenuItem(Icons.Outlined.Person, "Personal Information", "Name, DOB, gender", route = "personal_info"),
            ProfileMenuItem(Icons.Outlined.AccountBalance, "Bank Accounts", "HDFC Bank ••••4092", badge = "Primary", badgeColor = BlueAccent, route = "bank_accounts"),
            ProfileMenuItem(Icons.Outlined.People, "Nominees", "Update beneficiary details", route = "nominees"),
        )),
        ProfileSection("SECURITY & PREFERENCES", listOf(
            ProfileMenuItem(Icons.Outlined.Lock, "Security Settings", "MPIN, biometric, sessions", badge = "Active", badgeColor = TealAccent, route = "security_settings"),
            ProfileMenuItem(Icons.Outlined.Language, "Language & Preferences", "English (EN)", badge = "EN", route = "preferences"),
            ProfileMenuItem(Icons.Outlined.Notifications, "Notification Preferences", "SMS, Push, Email", route = "preferences"),
        )),
        ProfileSection("SUPPORT & REGULATORY", listOf(
            ProfileMenuItem(Icons.Outlined.HelpOutline, "Help Centre", "FAQs, guides, tutorials", route = "help_centre"),
            ProfileMenuItem(Icons.Outlined.Gavel, "Legal & Compliance", "Chit Funds Act, 1982 — TN-RO-2024"),
            ProfileMenuItem(Icons.Outlined.Info, "About ChitSmart", "v2.4.0 (Build 8402)"),
        )),
    )

    Scaffold(
        containerColor = PageBg,
        topBar = { ProfileTopBar(onBack = onBack) },
        bottomBar = { ProfileBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        ) {
            // ── Profile showcase card ──
            item {
                Spacer(Modifier.height(8.dp))
                ProfileShowcaseCard(
                    memberName = memberName,
                    memberId = memberId,
                    mobile = mobile,
                    email = email,
                    activeCircles = activeCircles,
                    totalInvested = totalInvested,
                    memberSince = memberSince,
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Menu sections ──
            sections.forEach { section ->
                item {
                    Text(section.sectionTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.55.sp)
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White),
                    ) {
                        section.items.forEachIndexed { i, item ->
                            if (i > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3FF))
                            ProfileMenuRow(item = item, onTap = { onMenuItemTap(item) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Logout ──
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { showLogoutDialog = true }.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed, letterSpacing = 0.14.sp, textAlign = TextAlign.Center)
                    Text("End current session securely", fontSize = 11.sp, color = SlateHint, letterSpacing = 0.33.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to log out of your ChitSmart account?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Log Out", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = BlueAccent)
                }
            },
        )
    }
}

@Composable
private fun ProfileShowcaseCard(
    memberName: String,
    memberId: String,
    mobile: String,
    email: String,
    activeCircles: Int,
    totalInvested: String,
    memberSince: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Avatar
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF0B3D7A), Color(0xFF1769AA))))
                    .border(2.dp, Color(0xFFD7E2FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(memberName.first().toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(memberName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("Member ID: ") }
                    withStyle(SpanStyle(color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) { append("#$memberId") }
                })
                Text(mobile, fontSize = 12.sp, color = SlateBody)
                Text(email, fontSize = 12.sp, color = SlateBody, maxLines = 1)
            }
        }

        HorizontalDivider(color = Color(0xFFF1F3FF))

        // Stats row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            ProfileStat(label = "Active Circles", value = "$activeCircles")
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF1F3FF)))
            ProfileStat(label = "Total Invested", value = totalInvested)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF1F3FF)))
            ProfileStat(label = "Member Since", value = memberSince)
        }

        // Active status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE6FAF3)).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TealAccent))
                Text("Account Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealAccent, letterSpacing = 0.33.sp)
            }
            Text("Edit Profile →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, modifier = Modifier.clickable {})
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyMid, textAlign = TextAlign.Center)
        Text(label, fontSize = 11.sp, color = SlateBody, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem, onTap: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onTap() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBlue), contentAlignment = Alignment.Center) {
            Icon(item.icon, contentDescription = null, tint = NavyMid, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (item.isDestructive) ErrorRed else NavyDeep, letterSpacing = 0.14.sp)
                item.badge?.let { badge ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(item.badgeColor.copy(0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = item.badgeColor)
                    }
                }
            }
            Text(item.subtitle, fontSize = 12.sp, color = SlateBody)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        title = {
            Column {
                Text("My Profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Account settings & preferences", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
            }
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
    )
}

@Composable
private fun ProfileBottomNav() {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        listOf(Icons.Outlined.Home to "Home", Icons.Outlined.AccountBalance to "My Chits", Icons.Outlined.Payments to "Payments", Icons.Outlined.SmartToy to "Assistant", Icons.Outlined.Person to "Profile").forEachIndexed { index, (icon, label) ->
            NavigationBarItem(selected = index == 4, onClick = {}, icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) }, label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = NavyMid, selectedTextColor = NavyMid, indicatorColor = SurfaceBlue, unselectedIconColor = SlateBody, unselectedTextColor = SlateBody))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Profile")
@Composable
fun ProfileScreenPreview() { MaterialTheme { ProfileScreen() } }
