package com.chitsmart.ui.notifications

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens from Figma (node 1:1965) ───────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyMid       = Color(0xFF002141)
private val BlueAccent    = Color(0xFF0261A2)
private val TealAccent    = Color(0xFF00B3A1)
private val TealBg        = Color(0xFFE6FAF3)
private val SlateBody     = Color(0xFF43474F)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val InputPill     = Color(0xFFE8EDFF)
private val AmberWarn     = Color(0xFFF59E0B)
private val AmberBg       = Color(0xFFFFF8E1)
private val GreenPaid     = Color(0xFF1B6E55)
private val BlueSys       = Color(0xFF0261A2)
private val BlueSysBg     = Color(0xFFF1F3FF)
private val UnreadDot     = Color(0xFF0261A2)

enum class NotificationType { PAYMENT_DUE, PAYMENT_CONFIRMED, INSTALLMENT_REMINDER, PAYOUT_CREDITED, SYSTEM_ALERT }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val time: String,
    val isUnread: Boolean,
    val actionLabel: String? = null,
)

data class NotificationGroup(val label: String, val items: List<AppNotification>)

@Composable
fun NotificationsScreen(
    groups: List<NotificationGroup> = sampleNotifications,
    onMarkAllRead: () -> Unit = {},
    onNotificationTap: (AppNotification) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val unreadCount = groups.sumOf { g -> g.items.count { it.isUnread } }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            NotificationsTopBar(unreadCount = unreadCount, onBack = onBack, onMarkAllRead = onMarkAllRead)
        },
        bottomBar = { NotificationsBottomNav() },
    ) { padding ->
        if (groups.all { it.items.isEmpty() }) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = SlateBody, modifier = Modifier.size(48.dp))
                    Text("You're all caught up!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text("No new notifications", fontSize = 14.sp, color = SlateBody)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            groups.forEach { group ->
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(group.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.3.sp)
                    Spacer(Modifier.height(8.dp))
                }
                items(group.items.size) { i ->
                    if (i > 0) Spacer(Modifier.height(8.dp))
                    NotificationCard(notification = group.items[i], onTap = { onNotificationTap(group.items[i]) })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification, onTap: () -> Unit) {
    val (iconColor, iconBg, icon) = when (notification.type) {
        NotificationType.PAYMENT_DUE -> Triple(AmberWarn, AmberBg, Icons.Outlined.NotificationImportant)
        NotificationType.PAYMENT_CONFIRMED -> Triple(GreenPaid, TealBg, Icons.Outlined.CheckCircle)
        NotificationType.INSTALLMENT_REMINDER -> Triple(BlueAccent, SurfaceBlue, Icons.Outlined.CalendarToday)
        NotificationType.PAYOUT_CREDITED -> Triple(GreenPaid, TealBg, Icons.Outlined.AccountBalanceWallet)
        NotificationType.SYSTEM_ALERT -> Triple(SlateBody, Color(0xFFF5F5F5), Icons.Outlined.Info)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (notification.isUnread) Color.White else Color.White.copy(0.95f))
            .clickable { onTap() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Icon
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, fontSize = 14.sp, fontWeight = if (notification.isUnread) FontWeight.SemiBold else FontWeight.Medium, color = NavyDeep)
            Spacer(Modifier.height(2.dp))
            Text(notification.body, fontSize = 12.sp, color = SlateBody, lineHeight = 16.sp, maxLines = 2)
            notification.actionLabel?.let { label ->
                Spacer(Modifier.height(6.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TealAccent, modifier = Modifier.clickable { onTap() })
            }
        }

        // Time + unread dot
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(notification.time, fontSize = 11.sp, color = SlateBody)
            if (notification.isUnread) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(UnreadDot))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTopBar(unreadCount: Int, onBack: () -> Unit, onMarkAllRead: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
        title = {
            Column {
                Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                if (unreadCount > 0) Text("$unreadCount unread", fontSize = 11.sp, color = BlueAccent, letterSpacing = 0.33.sp)
            }
        },
        actions = {
            TextButton(onClick = onMarkAllRead) { Text("Mark All Read", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold) }
        },
    )
}

@Composable
private fun NotificationsBottomNav() {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        listOf(Icons.Outlined.Home to "Home", Icons.Outlined.AccountBalance to "My Chits", Icons.Outlined.Payments to "Payments", Icons.Outlined.SmartToy to "Assistant", Icons.Outlined.Person to "Profile").forEachIndexed { index, (icon, label) ->
            NavigationBarItem(selected = index == 0, onClick = {}, icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) }, label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = NavyMid, selectedTextColor = NavyMid, indicatorColor = SurfaceBlue, unselectedIconColor = SlateBody, unselectedTextColor = SlateBody))
        }
    }
}

private val sampleNotifications = listOf(
    NotificationGroup("TODAY", listOf(
        AppNotification("1", NotificationType.PAYMENT_DUE, "Installment Due Tomorrow", "₹2,500 due for Family Savings 50K on 15/10/2026. Pay early to avoid late fees.", "10:30 AM", isUnread = true, actionLabel = "Pay Now →"),
        AppNotification("2", NotificationType.PAYMENT_CONFIRMED, "Payment Confirmed", "₹5,000 successfully paid for Community Chit 1L. Receipt #RCP-0892.", "8:15 AM", isUnread = false),
    )),
    NotificationGroup("THIS WEEK", listOf(
        AppNotification("3", NotificationType.INSTALLMENT_REMINDER, "Upcoming Cycle Reminder", "Family Savings 50K completes 25% of its cycle. 6 of 24 installments paid.", "12 Oct", isUnread = false),
        AppNotification("4", NotificationType.PAYOUT_CREDITED, "Payout Credited — ₹82,500", "Your payout for Community Chit 1L has been credited to HDFC Bank ••••4092.", "10 Oct", isUnread = false),
    )),
    NotificationGroup("EARLIER", listOf(
        AppNotification("5", NotificationType.SYSTEM_ALERT, "Auto-Debit Mandate Active", "Your auto-debit mandate for Family Savings 50K is now active. Monthly installments will be auto-collected.", "5 Oct", isUnread = false),
    )),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Notifications")
@Composable
fun NotificationsScreenPreview() { MaterialTheme { NotificationsScreen() } }
