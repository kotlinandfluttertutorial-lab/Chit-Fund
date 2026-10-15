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

// ── Colour tokens from Figma (node 1:2165) ───────────────────────────────────
private val PageBg       = Color(0xFFF9F9FF)
private val NavyDeep     = Color(0xFF041B3C)
private val NavyMid      = Color(0xFF002141)
private val TealAccent   = Color(0xFF00B3A1)
private val BlueAccent   = Color(0xFF0261A2)
private val SlateBody    = Color(0xFF43474F)
private val SurfaceBlue  = Color(0xFFF1F3FF)
private val InputPill    = Color(0xFFE8EDFF)
private val GreenPaid    = Color(0xFF1B6E55)
private val GreenBg      = Color(0xFFE6FAF3)
private val ErrorRed     = Color(0xFFBA1A1A)
private val ErrorBg      = Color(0xFFFFDAD6)
private val AmberWarn    = Color(0xFFF59E0B)
private val AmberBg      = Color(0xFFFFF8E1)
private val AmberText    = Color(0xFF795500)

enum class TxStatus { PAID, FAILED, PENDING }

data class TransactionRecord(
    val id: String,
    val groupName: String,
    val installmentMonth: String,
    val date: String,
    val amount: String,
    val method: String,
    val status: TxStatus,
    val receiptAvailable: Boolean,
    val note: String? = null,
)

@Composable
fun PaymentHistoryScreen(
    transactions: List<TransactionRecord> = sampleTransactions,
    outstandingBalance: String = "₹0",
    hasOutstanding: Boolean = false,
    onReceiptTap: (TransactionRecord) -> Unit = {},
    onDownloadStatement: () -> Unit = {},
    onBack: () -> Unit = {},
    onPayNow: () -> Unit = {},
) {
    val filters = listOf("All", "Paid", "Failed", "Pending")
    var selectedFilter by remember { mutableIntStateOf(0) }
    var selectedGroup by remember { mutableStateOf("All Groups") }
    var selectedDateRange by remember { mutableStateOf("All Time") }

    val filtered = when (selectedFilter) {
        1 -> transactions.filter { it.status == TxStatus.PAID }
        2 -> transactions.filter { it.status == TxStatus.FAILED }
        3 -> transactions.filter { it.status == TxStatus.PENDING }
        else -> transactions
    }

    Scaffold(
        containerColor = PageBg,
        topBar = { PaymentHistoryTopBar(onBack = onBack, onDownloadStatement = onDownloadStatement) },
        bottomBar = { PaymentHistoryBottomNav() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Outstanding balance card ──
            item {
                Spacer(Modifier.height(8.dp))
                if (hasOutstanding) {
                    Row(
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Outstanding Balance", fontSize = 12.sp, color = SlateBody, fontWeight = FontWeight.Medium)
                            Text(outstandingBalance, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            Text("Includes late fees", fontSize = 11.sp, color = ErrorRed)
                        }
                        Button(onClick = onPayNow, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = TealAccent)) {
                            Text("Pay Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(GreenBg).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenPaid, modifier = Modifier.size(20.dp))
                            Column {
                                Text("All Paid Up ✓", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenPaid)
                                Text("Outstanding Balance: ₹0", fontSize = 12.sp, color = GreenPaid)
                            }
                        }
                        Text("View Payouts →", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Download statement bar ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceBlue).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Download Statement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, modifier = Modifier.clickable { onDownloadStatement() })
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Filters ──
            item {
                // Date range + group selectors
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown(label = selectedDateRange, icon = Icons.Outlined.CalendarToday, modifier = Modifier.weight(1f), onClick = {})
                    FilterDropdown(label = selectedGroup, icon = Icons.Outlined.AccountBalance, modifier = Modifier.weight(1f), onClick = {})
                }
                Spacer(Modifier.height(8.dp))
                // Status filter chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters.size) { i ->
                        val selected = selectedFilter == i
                        Box(
                            modifier = Modifier.shadow(if (selected) 1.dp else 0.dp, RoundedCornerShape(100.dp)).clip(RoundedCornerShape(100.dp)).background(if (selected) NavyMid else InputPill).clickable { selectedFilter = i }.padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(filters[i], fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) Color.White else SlateBody, letterSpacing = 0.24.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Transaction records header ──
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Transaction Records", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text("${filtered.size} transactions", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Transaction items ──
            items(filtered.size) { i ->
                if (i > 0) Spacer(Modifier.height(8.dp))
                TransactionCard(transaction = filtered[i], onReceiptTap = { onReceiptTap(filtered[i]) })
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FilterDropdown(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.height(44.dp).shadow(1.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(Color.White).clickable { onClick() }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = SlateBody, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 12.sp, color = NavyDeep, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = SlateBody, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun TransactionCard(transaction: TransactionRecord, onReceiptTap: () -> Unit) {
    val (statusColor, statusBg, statusLabel) = when (transaction.status) {
        TxStatus.PAID -> Triple(GreenPaid, GreenBg, "Paid")
        TxStatus.FAILED -> Triple(ErrorRed, ErrorBg, "Failed")
        TxStatus.PENDING -> Triple(AmberText, AmberBg, "Pending")
    }

    Column(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.groupName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(transaction.installmentMonth, fontSize = 12.sp, color = SlateBody)
                Text(transaction.date, fontSize = 11.sp, color = SlateBody)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(transaction.amount, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Payment, contentDescription = null, tint = SlateBody, modifier = Modifier.size(12.dp))
                Text(transaction.method, fontSize = 11.sp, color = SlateBody)
            }
            if (transaction.receiptAvailable) {
                Row(
                    modifier = Modifier.clickable { onReceiptTap() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.Receipt, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
                    Text("Receipt", fontSize = 11.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        transaction.note?.let { note ->
            Text(note, fontSize = 11.sp, color = SlateBody)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentHistoryTopBar(onBack: () -> Unit, onDownloadStatement: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
        title = {
            Column {
                Text("Payment History", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text("Transaction records & statements", fontSize = 11.sp, color = SlateBody)
            }
        },
        actions = {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceBlue).clickable { onDownloadStatement() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Share, contentDescription = "Export", tint = NavyDeep, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        },
    )
}

@Composable
private fun PaymentHistoryBottomNav() {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        listOf(Icons.Outlined.Home to "Home", Icons.Outlined.AccountBalance to "My Chits", Icons.Outlined.Payments to "Payments", Icons.Outlined.SmartToy to "Assistant", Icons.Outlined.Person to "Profile").forEachIndexed { index, (icon, label) ->
            NavigationBarItem(selected = index == 2, onClick = {}, icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) }, label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = NavyMid, selectedTextColor = NavyMid, indicatorColor = SurfaceBlue, unselectedIconColor = SlateBody, unselectedTextColor = SlateBody))
        }
    }
}

private val sampleTransactions = listOf(
    TransactionRecord("1", "Family Savings 50K", "October 2026 • Month 7", "12/10/2026", "₹2,500", "UPI — priya@ybl", TxStatus.PAID, true),
    TransactionRecord("2", "Community Chit 1L", "October 2026 • Month 15", "10/10/2026", "₹5,000", "Auto-Debit — HDFC ••••4092", TxStatus.PAID, true, "Auto-collected via mandate"),
    TransactionRecord("3", "Monthly Circle 25K", "August 2026 • Month 20", "18/08/2026", "₹1,250", "UPI — priya98765@okicici", TxStatus.PAID, true, "Final cycle payment"),
    TransactionRecord("4", "Family Savings 50K", "September 2026 • Month 6", "13/09/2026", "₹2,500", "Net Banking — HDFC", TxStatus.FAILED, false, "Bank declined — insufficient funds"),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Payment History")
@Composable
fun PaymentHistoryScreenPreview() { MaterialTheme { PaymentHistoryScreen() } }
