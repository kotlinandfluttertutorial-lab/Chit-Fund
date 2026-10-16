package com.chitsmart.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg      = Color(0xFFF9F9FF)
private val NavyDeep    = Color(0xFF041B3C)
private val NavyMid     = Color(0xFF002141)
private val TealAccent  = Color(0xFF00B3A1)
private val BlueAccent  = Color(0xFF0261A2)
private val ErrorRed    = Color(0xFFBA1A1A)
private val SlateBody   = Color(0xFF43474F)
private val SlateHint   = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)

// ── Data model ────────────────────────────────────────────────────────────────

data class BankAccount(
    val id: String,
    val bankName: String,
    val accountType: String,       // Savings / Current
    val maskedNumber: String,      // e.g. "•••• 4092"
    val ifscCode: String,
    val upiIds: List<String> = emptyList(),
    val isPrimary: Boolean = false,
)

private val sampleAccounts = listOf(
    BankAccount("1", "HDFC Bank", "Savings", "•••• 4092", "HDFC0001234", listOf("priya@hdfcbank"), isPrimary = true),
    BankAccount("2", "SBI Bank", "Savings", "•••• 8817", "SBIN0002345", listOf("priya.sharma@oksbi")),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    onBack: () -> Unit = {},
) {
    val accounts = remember { mutableStateListOf(*sampleAccounts.toTypedArray()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<BankAccount?>(null) }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep)
                    }
                },
                title = {
                    Column {
                        Text("Bank Accounts", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("Manage linked accounts & UPI", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = TealAccent,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add Bank Account", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        ) {
            items(accounts, key = { it.id }) { account ->
                BankAccountCard(
                    account = account,
                    onSetPrimary = {
                        val idx = accounts.indexOfFirst { it.id == account.id }
                        if (idx >= 0) {
                            val updated = accounts.map { it.copy(isPrimary = it.id == account.id) }
                            accounts.clear()
                            accounts.addAll(updated)
                        }
                    },
                    onRemove = { removeTarget = account },
                )
            }
        }
    }

    // Remove confirmation dialog
    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Remove Account?", fontWeight = FontWeight.SemiBold) },
            text = { Text("Remove ${target.bankName} ${target.maskedNumber}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    accounts.removeIf { it.id == target.id }
                    removeTarget = null
                }) {
                    Text("Remove", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) {
                    Text("Cancel", color = BlueAccent)
                }
            },
        )
    }

    // Add bank account bottom sheet
    if (showAddSheet) {
        AddBankAccountSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { newAccount ->
                accounts.add(newAccount)
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun BankAccountCard(
    account: BankAccount,
    onSetPrimary: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White),
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = NavyMid, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(account.bankName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    if (account.isPrimary) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(TealAccent.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = TealAccent, modifier = Modifier.size(10.dp))
                            Text("Primary", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TealAccent)
                        }
                    }
                }
                Text("${account.accountType}  ${account.maskedNumber}", fontSize = 13.sp, color = SlateBody)
                Text(account.ifscCode, fontSize = 11.sp, color = SlateHint)
            }
        }

        // UPI IDs
        if (account.upiIds.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3FF))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("UPI IDs", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateHint, letterSpacing = 0.33.sp)
                account.upiIds.forEach { upiId ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TealAccent))
                        Text(upiId, fontSize = 13.sp, color = SlateBody)
                    }
                }
            }
        }

        // Actions
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3FF))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
            if (!account.isPrimary) {
                TextButton(onClick = onSetPrimary) {
                    Text("Set as Primary", fontSize = 13.sp, color = BlueAccent, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRemove) {
                Text("Remove", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Add bank account bottom sheet ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBankAccountSheet(
    onDismiss: () -> Unit,
    onAdd: (BankAccount) -> Unit,
) {
    // Step: 0 = form, 1 = verifying, 2 = confirm
    var step by remember { mutableStateOf(0) }
    var accountNumber by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var accountNumberError by remember { mutableStateOf<String?>(null) }
    var ifscError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Add Bank Account", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)

            when (step) {
                0 -> {
                    // Input form
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it; accountNumberError = null },
                        label = { Text("Account Number") },
                        isError = accountNumberError != null,
                        supportingText = accountNumberError?.let { { Text(it, color = ErrorRed) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                        ),
                    )
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase(); ifscError = null },
                        label = { Text("IFSC Code") },
                        isError = ifscError != null,
                        supportingText = ifscError?.let { { Text(it, color = ErrorRed) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                        ),
                    )
                    Button(
                        onClick = {
                            accountNumberError = if (accountNumber.length < 8) "Enter a valid account number" else null
                            ifscError = if (!ifsc.matches(Regex("^[A-Z]{4}0[A-Z0-9]{6}\$"))) "Enter a valid IFSC code" else null
                            if (accountNumberError == null && ifscError == null) step = 1
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Verify Account", fontWeight = FontWeight.SemiBold)
                    }
                }
                1 -> {
                    // Verifying animation
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(color = TealAccent)
                            Text("Verifying account via penny-drop…", fontSize = 13.sp, color = SlateBody)
                        }
                    }
                    // Auto-advance to confirmation after short delay (in real app, wait for API)
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1500)
                        step = 2
                    }
                }
                2 -> {
                    // Confirm details
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SurfaceBlue).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Account Verified ✓", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TealAccent)
                        Text("Account: •••• ${accountNumber.takeLast(4)}", fontSize = 13.sp, color = SlateBody)
                        Text("IFSC: $ifsc", fontSize = 13.sp, color = SlateBody)
                        Text("Bank: Sample Bank (verify label from bank API)", fontSize = 12.sp, color = SlateHint)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { step = 0 },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Edit", color = BlueAccent) }
                        Button(
                            onClick = {
                                onAdd(
                                    BankAccount(
                                        id = System.currentTimeMillis().toString(),
                                        bankName = "Sample Bank",
                                        accountType = "Savings",
                                        maskedNumber = "•••• ${accountNumber.takeLast(4)}",
                                        ifscCode = ifsc,
                                        upiIds = emptyList(),
                                        isPrimary = false,
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("Confirm", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Bank Accounts")
@Composable
fun BankAccountsScreenPreview() { MaterialTheme { BankAccountsScreen() } }
