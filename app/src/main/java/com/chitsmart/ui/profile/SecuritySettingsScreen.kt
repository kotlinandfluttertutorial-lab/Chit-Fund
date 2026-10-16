package com.chitsmart.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

// ── Data models ───────────────────────────────────────────────────────────────

private data class ActiveSession(
    val id: String,
    val deviceName: String,
    val lastActive: String,
    val isCurrent: Boolean = false,
)

private data class LoginHistoryEntry(
    val id: String,
    val date: String,
    val deviceName: String,
    val status: String,
    val isSuccess: Boolean,
)

private val sampleSessions = listOf(
    ActiveSession("1", "Samsung Galaxy S23 Ultra", "Active now", isCurrent = true),
    ActiveSession("2", "iPhone 14 Pro", "2 hours ago"),
    ActiveSession("3", "Pixel 7", "Yesterday, 6:30 PM"),
)

private val sampleHistory = listOf(
    LoginHistoryEntry("1", "15 Oct 2026, 10:30 AM", "Samsung Galaxy S23 Ultra", "Successful", true),
    LoginHistoryEntry("2", "14 Oct 2026, 8:10 PM", "iPhone 14 Pro", "Successful", true),
    LoginHistoryEntry("3", "13 Oct 2026, 3:45 PM", "Unknown device", "Failed", false),
    LoginHistoryEntry("4", "12 Oct 2026, 9:00 AM", "Samsung Galaxy S23 Ultra", "Successful", true),
    LoginHistoryEntry("5", "11 Oct 2026, 7:30 AM", "Pixel 7", "Successful", true),
)

// ── MPIN change step ──────────────────────────────────────────────────────────

private enum class MPINStep { IDLE, CURRENT, NEW, CONFIRM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit = {},
) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var mpinStep by remember { mutableStateOf(MPINStep.IDLE) }
    var currentMPIN by remember { mutableStateOf("") }
    var newMPIN by remember { mutableStateOf("") }
    var confirmMPIN by remember { mutableStateOf("") }
    var mpinError by remember { mutableStateOf<String?>(null) }
    var mpinSuccess by remember { mutableStateOf(false) }
    val sessions = remember { mutableStateListOf(*sampleSessions.toTypedArray()) }

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
                        Text("Security Settings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("MPIN, biometric & active sessions", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        ) {

            // ── Change MPIN card ─────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Pin, contentDescription = null, tint = NavyMid, modifier = Modifier.size(20.dp))
                            Text("Change MPIN", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        }
                        if (mpinStep == MPINStep.IDLE) {
                            TextButton(onClick = { mpinStep = MPINStep.CURRENT; mpinError = null; mpinSuccess = false }) {
                                Text("Change", color = TealAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (mpinSuccess) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFE6FAF3)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                            Text("MPIN changed successfully!", fontSize = 13.sp, color = TealAccent, fontWeight = FontWeight.Medium)
                        }
                    }

                    when (mpinStep) {
                        MPINStep.CURRENT -> {
                            Text("Step 1 of 3 — Enter current MPIN", fontSize = 12.sp, color = SlateBody)
                            MPINField(
                                label = "Current MPIN (6 digits)",
                                value = currentMPIN,
                                onValueChange = { if (it.length <= 6) { currentMPIN = it; mpinError = null } },
                                error = mpinError,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { mpinStep = MPINStep.IDLE; currentMPIN = "" }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                                Button(
                                    onClick = {
                                        if (currentMPIN.length < 6) mpinError = "MPIN must be 6 digits"
                                        else { mpinError = null; mpinStep = MPINStep.NEW }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                ) { Text("Next") }
                            }
                        }
                        MPINStep.NEW -> {
                            Text("Step 2 of 3 — Enter new MPIN", fontSize = 12.sp, color = SlateBody)
                            MPINField(
                                label = "New MPIN (6 digits)",
                                value = newMPIN,
                                onValueChange = { if (it.length <= 6) { newMPIN = it; mpinError = null } },
                                error = mpinError,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { mpinStep = MPINStep.CURRENT; newMPIN = "" }, modifier = Modifier.weight(1f)) { Text("Back") }
                                Button(
                                    onClick = {
                                        if (newMPIN.length < 6) mpinError = "MPIN must be 6 digits"
                                        else if (newMPIN == currentMPIN) mpinError = "New MPIN must differ from current"
                                        else { mpinError = null; mpinStep = MPINStep.CONFIRM }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                ) { Text("Next") }
                            }
                        }
                        MPINStep.CONFIRM -> {
                            Text("Step 3 of 3 — Confirm new MPIN", fontSize = 12.sp, color = SlateBody)
                            MPINField(
                                label = "Confirm MPIN",
                                value = confirmMPIN,
                                onValueChange = { if (it.length <= 6) { confirmMPIN = it; mpinError = null } },
                                error = mpinError,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { mpinStep = MPINStep.NEW; confirmMPIN = "" }, modifier = Modifier.weight(1f)) { Text("Back") }
                                Button(
                                    onClick = {
                                        if (confirmMPIN != newMPIN) mpinError = "MPINs do not match"
                                        else {
                                            // API call would go here
                                            mpinStep = MPINStep.IDLE
                                            currentMPIN = ""; newMPIN = ""; confirmMPIN = ""
                                            mpinSuccess = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                ) { Text("Confirm") }
                            }
                        }
                        else -> { /* IDLE — nothing extra */ }
                    }
                }
            }

            // ── Biometric toggle ─────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = NavyMid, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Biometric Login", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                            Text("Use fingerprint or face unlock", fontSize = 12.sp, color = SlateBody)
                        }
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = TealAccent),
                    )
                }
            }

            // ── Active sessions ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("ACTIVE SESSIONS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.55.sp)
                    sessions.forEachIndexed { i, session ->
                        if (i > 0) HorizontalDivider(color = Color(0xFFF1F3FF))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = NavyMid, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(session.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                                    if (session.isCurrent) {
                                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(TealAccent.copy(0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text("This device", fontSize = 10.sp, color = TealAccent, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                Text(session.lastActive, fontSize = 11.sp, color = SlateHint)
                            }
                            if (!session.isCurrent) {
                                TextButton(onClick = { sessions.removeIf { it.id == session.id } }) {
                                    Text("Revoke", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Login history ────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("LOGIN HISTORY", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.55.sp)
                    Text("Last 10 login attempts", fontSize = 12.sp, color = SlateBody)
                    sampleHistory.take(10).forEachIndexed { i, entry ->
                        if (i > 0) HorizontalDivider(color = Color(0xFFF1F3FF))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (entry.isSuccess) Color(0xFFE6FAF3) else Color(0xFFFFEDED)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (entry.isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Error,
                                    contentDescription = null,
                                    tint = if (entry.isSuccess) TealAccent else ErrorRed,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                                Text(entry.date, fontSize = 11.sp, color = SlateHint)
                            }
                            Text(
                                entry.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (entry.isSuccess) TealAccent else ErrorRed,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MPINField(label: String, value: String, onValueChange: (String) -> Unit, error: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = ErrorRed) } },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                errorBorderColor = ErrorRed,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Security Settings")
@Composable
fun SecuritySettingsScreenPreview() { MaterialTheme { SecuritySettingsScreen() } }
