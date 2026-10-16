package com.chitsmart.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val BlueAccent = Color(0xFF0261A2)
private val SlateBody = Color(0xFF43474F)
private val SlateHint = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)

// ── Language data ─────────────────────────────────────────────────────────────

private data class Language(val code: String, val label: String, val nativeName: String)

private val supportedLanguages =
    listOf(
        Language("en", "English", "English"),
        Language("hi", "Hindi", "हिंदी"),
        Language("ta", "Tamil", "தமிழ்"),
        Language("te", "Telugu", "తెలుగు"),
        Language("kn", "Kannada", "ಕನ್ನಡ"),
        Language("ml", "Malayalam", "മലയാളം"),
    )

// ── Notification data ─────────────────────────────────────────────────────────

private data class NotificationCategory(
    val label: String,
    val smsEnabled: Boolean,
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
)

// ── Statement format ──────────────────────────────────────────────────────────

private enum class StatementFormat { PDF, EXCEL }

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    initialLanguage: String = "en",
    onBack: () -> Unit = {},
    onSave: (language: String) -> Unit = {},
) {
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var statementFormat by remember { mutableStateOf(StatementFormat.PDF) }
    val notifications =
        remember {
            mutableStateListOf(
                NotificationCategory("Payment Due", smsEnabled = true, pushEnabled = true, emailEnabled = true),
                NotificationCategory("Payment Received", smsEnabled = true, pushEnabled = true, emailEnabled = false),
                NotificationCategory("Payout Credit", smsEnabled = true, pushEnabled = true, emailEnabled = true),
                NotificationCategory("Account Updates", smsEnabled = false, pushEnabled = true, emailEnabled = false),
            )
        }

    val hasChanges = selectedLanguage != initialLanguage

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
                        Text("Language & Preferences", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("Language, notifications & statements", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = PageBg) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = { onSave(selectedLanguage) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Preferences", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
        ) {
            // ── Language selector ────────────────────────────────────────────
            item {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().shadow(
                            1.dp,
                            RoundedCornerShape(12.dp),
                        ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Language, contentDescription = null, tint = NavyMid, modifier = Modifier.size(20.dp))
                        Text("App Language", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    }
                    Text("Changes take effect after restarting the app.", fontSize = 12.sp, color = SlateBody)
                    // Wrap into rows of 3
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        supportedLanguages.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { lang ->
                                    val selected = selectedLanguage == lang.code
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedLanguage = lang.code },
                                        label = {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    lang.nativeName,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                                )
                                                Text(
                                                    lang.label,
                                                    fontSize = 10.sp,
                                                    color = if (selected) Color.White.copy(0.8f) else SlateHint,
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                            FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TealAccent,
                                                selectedLabelColor = Color.White,
                                            ),
                                        border =
                                            FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selected,
                                                borderColor = Color(0xFFE2E8F0),
                                                selectedBorderColor = TealAccent,
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Notification preferences ─────────────────────────────────────
            item {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().shadow(
                            1.dp,
                            RoundedCornerShape(12.dp),
                        ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = NavyMid, modifier = Modifier.size(20.dp))
                        Text("Notification Preferences", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    }

                    // Header row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Category",
                            modifier = Modifier.weight(1f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateHint,
                        )
                        listOf("SMS", "Push", "Email").forEach { channel ->
                            Text(
                                channel,
                                modifier = Modifier.width(48.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateHint,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F3FF))

                    notifications.forEachIndexed { idx, notif ->
                        if (idx > 0) HorizontalDivider(color = Color(0xFFF1F3FF))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(notif.label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = NavyDeep)
                            // SMS
                            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                                Switch(
                                    checked = notif.smsEnabled,
                                    onCheckedChange = { v -> notifications[idx] = notif.copy(smsEnabled = v) },
                                    modifier = Modifier.size(width = 36.dp, height = 20.dp),
                                    colors = SwitchDefaults.colors(checkedTrackColor = TealAccent),
                                )
                            }
                            // Push
                            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                                Switch(
                                    checked = notif.pushEnabled,
                                    onCheckedChange = { v -> notifications[idx] = notif.copy(pushEnabled = v) },
                                    modifier = Modifier.size(width = 36.dp, height = 20.dp),
                                    colors = SwitchDefaults.colors(checkedTrackColor = TealAccent),
                                )
                            }
                            // Email
                            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                                Switch(
                                    checked = notif.emailEnabled,
                                    onCheckedChange = { v -> notifications[idx] = notif.copy(emailEnabled = v) },
                                    modifier = Modifier.size(width = 36.dp, height = 20.dp),
                                    colors = SwitchDefaults.colors(checkedTrackColor = TealAccent),
                                )
                            }
                        }
                    }
                }
            }

            // ── Statement format ─────────────────────────────────────────────
            item {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().shadow(
                            1.dp,
                            RoundedCornerShape(12.dp),
                        ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = NavyMid, modifier = Modifier.size(20.dp))
                        Text("Account Statement Format", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatementFormat.values().forEach { format ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                RadioButton(
                                    selected = statementFormat == format,
                                    onClick = { statementFormat = format },
                                    colors = RadioButtonDefaults.colors(selectedColor = TealAccent),
                                )
                                Text(format.name, fontSize = 14.sp, color = NavyDeep)
                            }
                        }
                    }
                    Text(
                        if (statementFormat == StatementFormat.PDF) {
                            "PDF statements are formatted for printing and archiving."
                        } else {
                            "Excel statements are ideal for data analysis and accounting software."
                        },
                        fontSize = 12.sp,
                        color = SlateBody,
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Preferences")
@Composable
fun PreferencesScreenPreview() {
    MaterialTheme { PreferencesScreen() }
}
