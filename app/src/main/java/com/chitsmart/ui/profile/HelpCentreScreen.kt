package com.chitsmart.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
private val PageBg      = Color(0xFFF9F9FF)
private val NavyDeep    = Color(0xFF041B3C)
private val NavyMid     = Color(0xFF002141)
private val TealAccent  = Color(0xFF00B3A1)
private val BlueAccent  = Color(0xFF0261A2)
private val SlateBody   = Color(0xFF43474F)
private val SlateHint   = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)

// ── Data models ───────────────────────────────────────────────────────────────

private data class FAQ(val question: String, val answer: String)

private data class FAQCategory(val label: String, val faqs: List<FAQ>)

private val faqCategories = listOf(
    FAQCategory("Payments", listOf(
        FAQ("How do I pay my monthly installment?", "Go to the Payments tab and tap 'Pay Now' next to your due installment. We support UPI, Net Banking, Debit Card, NEFT, and RTGS."),
        FAQ("What happens if I miss an installment?", "A late fee is applied after the due date as per your chit group's terms. Please refer to your enrollment agreement for the exact fee structure."),
        FAQ("How long does payment take to reflect?", "UPI payments reflect instantly. NEFT/RTGS may take up to 2 business hours. Net Banking and Debit Card transactions reflect within 30 minutes."),
        FAQ("Can I pay in advance?", "Yes, you can pay up to 3 months in advance. Contact your branch or use the AI assistant for more information."),
    )),
    FAQCategory("Account", listOf(
        FAQ("How do I update my personal information?", "Go to Profile → Personal Information. You can update your name, email, and alternate mobile number. Primary mobile and DOB require contacting support."),
        FAQ("How do I change my MPIN?", "Go to Profile → Security Settings → Change MPIN. You will need to verify your current MPIN first."),
        FAQ("How do I add a bank account?", "Go to Profile → Bank Accounts and tap 'Add Bank Account'. You will need your account number and IFSC code for penny-drop verification."),
        FAQ("How do I view my payment history?", "Go to Payments → Payment History to see all your transactions, receipts, and account statements."),
    )),
    FAQCategory("Technical", listOf(
        FAQ("The app is not loading. What should I do?", "Try force-closing the app and reopening it. If the issue persists, check your internet connection and ensure you have the latest version of ChitSmart installed."),
        FAQ("I'm not receiving OTPs. What should I do?", "Ensure your mobile number has a stable signal. OTPs expire in 10 minutes. You can request a new OTP after 30 seconds. Contact support if the issue persists."),
        FAQ("How do I enable biometric login?", "Go to Profile → Security Settings → Biometric Login and toggle it on. Your device must support fingerprint or face recognition."),
        FAQ("I forgot my MPIN. How do I reset it?", "On the login screen, tap 'Forgot MPIN'. An OTP will be sent to your registered mobile number to verify your identity and set a new MPIN."),
    )),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCentreScreen(
    onBack: () -> Unit = {},
    onAskAI: () -> Unit = {},
    onHumanSupport: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Payments") }

    val filteredFAQs: List<FAQ> = if (searchQuery.isBlank()) {
        faqCategories.firstOrNull { it.label == selectedCategory }?.faqs ?: emptyList()
    } else {
        faqCategories.flatMap { cat ->
            cat.faqs.filter {
                it.question.contains(searchQuery, ignoreCase = true) ||
                it.answer.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
                        Text("Help Centre", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("FAQs, guides & support", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = PageBg) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onAskAI,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ask ChitSmart AI", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onHumanSupport,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent),
                    ) {
                        Icon(Icons.Outlined.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Talk to Human", fontSize = 13.sp)
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search FAQs…", color = SlateHint) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = SlateHint) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = SlateHint)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                )
            }

            // Category chips (only when not searching)
            if (searchQuery.isBlank()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        faqCategories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category.label,
                                onClick = { selectedCategory = category.label },
                                label = { Text(category.label, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealAccent,
                                    selectedLabelColor = Color.White,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == category.label,
                                    borderColor = Color(0xFFE2E8F0),
                                    selectedBorderColor = TealAccent,
                                ),
                            )
                        }
                    }
                }
            }

            // FAQ accordions
            items(filteredFAQs) { faq ->
                FAQAccordion(faq = faq)
            }

            if (filteredFAQs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = SlateHint, modifier = Modifier.size(40.dp))
                            Text("No FAQs found for '$searchQuery'", fontSize = 14.sp, color = SlateBody)
                            Text("Try asking the AI assistant for personalised help.", fontSize = 12.sp, color = SlateHint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FAQAccordion(faq: FAQ) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(faq.question, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NavyDeep, lineHeight = 20.sp)
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = SlateBody,
                modifier = Modifier.size(20.dp),
            )
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3FF))
                Text(faq.answer, modifier = Modifier.padding(16.dp), fontSize = 13.sp, color = SlateBody, lineHeight = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Help Centre")
@Composable
fun HelpCentreScreenPreview() { MaterialTheme { HelpCentreScreen() } }
