package com.chitsmart.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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
private val InputPill = Color(0xFFE8EDFF)
private val PolicyBg = Color(0xFFE8EDFF)
private val PolicyText = Color(0xFF005048)

// ── Data models ───────────────────────────────────────────────────────────────

private data class Language(val code: String, val nativeName: String)

private data class SuggestedQuestion(val text: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private data class RecentConversation(
    val id: String,
    val preview: String,
    val date: String,
    val messageCount: Int,
)

private val languages =
    listOf(
        Language("en", "English"),
        Language("hi", "हिंदी"),
        Language("ta", "தமிழ்"),
        Language("te", "తెలుగు"),
        Language("kn", "ಕನ್ನಡ"),
        Language("ml", "മലയാളം"),
    )

private val suggestedQuestions =
    listOf(
        SuggestedQuestion("What is my outstanding balance?", Icons.Outlined.AccountBalance),
        SuggestedQuestion("When is my next installment due?", Icons.Outlined.CalendarToday),
        SuggestedQuestion("How does foreman commission work?", Icons.Outlined.Gavel),
    )

private val recentConversations =
    listOf(
        RecentConversation("1", "What is my total due this month?", "Today, 10:30 AM", 4),
        RecentConversation("2", "Explain the chit fund process", "Yesterday, 3:45 PM", 6),
        RecentConversation("3", "How to update my bank account?", "13 Oct 2026", 3),
    )

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantHomeScreen(
    initialLanguage: String = "en",
    onStartChat: (message: String) -> Unit = {},
    onResumeConversation: (id: String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var inputText by remember { mutableStateOf("") }

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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBlue),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("ChitSmart AI", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyMid)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                Text("Online", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = PageBg) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier =
                            Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(Color.White)
                                .shadow(1.dp, RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 14.sp, color = NavyDeep),
                            cursorBrush = SolidColor(TealAccent),
                            decorationBox = { inner ->
                                if (inputText.isEmpty()) Text("Ask me anything…", fontSize = 14.sp, color = SlateHint)
                                inner()
                            },
                        )
                        Icon(Icons.Outlined.Mic, contentDescription = "Voice input", tint = SlateHint, modifier = Modifier.size(18.dp))
                    }
                    Box(
                        modifier =
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(if (inputText.isNotBlank()) TealAccent else Color(0xFFE0E8FF))
                                .clickable(enabled = inputText.isNotBlank()) {
                                    val msg = inputText
                                    inputText = ""
                                    onStartChat(msg)
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else SlateHint,
                            modifier = Modifier.size(18.dp),
                        )
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
            // ── Language chips ───────────────────────────────────────────────
            item {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().shadow(
                            1.dp,
                            RoundedCornerShape(12.dp),
                        ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "RESPONSE LANGUAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueAccent,
                        letterSpacing = 0.55.sp,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(languages) { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang.code,
                                onClick = { selectedLanguage = lang.code },
                                label = { Text(lang.nativeName, fontSize = 13.sp) },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealAccent,
                                        selectedLabelColor = NavyDeep,
                                    ),
                                border =
                                    FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedLanguage == lang.code,
                                        borderColor = Color(0xFFE2E8F0),
                                        selectedBorderColor = TealAccent,
                                    ),
                            )
                        }
                    }
                }
            }

            // ── Suggested questions ──────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = SlateBody, modifier = Modifier.size(15.dp))
                        Text(
                            "SUGGESTED QUESTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateBody,
                            letterSpacing = 0.275.sp,
                        )
                    }
                    suggestedQuestions.forEach { q ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth().shadow(
                                    1.dp,
                                    RoundedCornerShape(12.dp),
                                ).clip(RoundedCornerShape(12.dp)).background(Color.White)
                                    .clickable { onStartChat(q.text) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PolicyBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(q.icon, contentDescription = null, tint = PolicyText, modifier = Modifier.size(18.dp))
                            }
                            Text(q.text, modifier = Modifier.weight(1f), fontSize = 14.sp, color = NavyDeep)
                            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Recent conversations ─────────────────────────────────────────
            if (recentConversations.isNotEmpty()) {
                item {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth().shadow(
                                1.dp,
                                RoundedCornerShape(12.dp),
                            ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            "RECENT CONVERSATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BlueAccent,
                            letterSpacing = 0.55.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        recentConversations.forEachIndexed { i, conv ->
                            if (i > 0) HorizontalDivider(color = Color(0xFFF1F3FF))
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onResumeConversation(conv.id) }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBlue),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Outlined.Chat, contentDescription = null, tint = NavyMid, modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conv.preview, fontSize = 13.sp, color = NavyDeep, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text("${conv.date}  ·  ${conv.messageCount} messages", fontSize = 11.sp, color = SlateHint)
                                }
                                Icon(
                                    Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = SlateHint,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ── Info footer ──────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(InputPill).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = SlateBody, modifier = Modifier.size(14.dp))
                        Text("About ChitSmart AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateBody)
                    }
                    Text(
                        "I can access your account information and approved chit fund policy documents. " +
                            "I cannot initiate transactions or access other members' data.",
                        fontSize = 12.sp,
                        color = SlateHint,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Assistant Home")
@Composable
fun AssistantHomeScreenPreview() {
    MaterialTheme { AssistantHomeScreen() }
}
