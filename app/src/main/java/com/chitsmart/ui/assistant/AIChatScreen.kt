package com.chitsmart.ui.assistant

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Colour tokens from Figma (node 1:1220) ───────────────────────────────────
private val PageBg        = Color(0xFFF9F9FF)
private val NavyDeep      = Color(0xFF041B3C)
private val NavyMid       = Color(0xFF002141)
private val TealAccent    = Color(0xFF4FDBC8)
private val TealBorder    = Color(0xFF4FDBC8)
private val BlueAccent    = Color(0xFF0261A2)
private val SlateBody     = Color(0xFF43474F)
private val SlateHint     = Color(0xFF737780)
private val SurfaceBlue   = Color(0xFFF1F3FF)
private val InputPill     = Color(0xFFE8EDFF)
private val PolicyBg      = Color(0xFFE8EDFF)
private val PolicyText    = Color(0xFF005048)
private val TimePillBg    = Color(0xFFE0E8FF)
private val UserBubble    = Color(0xFF073763)
private val AiBubbleBg   = Color.White
private val ActionBar     = Color(0xFFC3C6D0)

// ── Data models ───────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val policyChips: List<PolicyChip> = emptyList(),
    val embeddedCard: EmbeddedCard? = null,
    val timestamp: String = "",
    val isTyping: Boolean = false,
)

data class PolicyChip(val label: String, val reference: String)

data class EmbeddedCard(
    val title: String,
    val bodyLines: List<String>,
    val totalAmount: String?,
    val ctaLabel: String?,
)

data class SuggestedQuery(val text: String)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun AIChatScreen(
    onBack: () -> Unit = {},
    onPolicyChipTap: (PolicyChip) -> Unit = {},
    onEmbeddedCTA: () -> Unit = {},
    onHumanSupport: () -> Unit = {},
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val messages = remember { mutableStateListOf(*sampleMessages.toTypedArray()) }
    val suggestedQueries = listOf(
        SuggestedQuery("What is my outstanding balance?"),
        SuggestedQuery("When is my next installment due?"),
        SuggestedQuery("How does foreman commission work?"),
    )

    Scaffold(
        containerColor = PageBg,
        topBar = { AIChatTopBar(onBack = onBack, onHumanSupport = onHumanSupport) },
        bottomBar = {
            AIChatInputBar(
                input = input,
                onInputChange = { input = it },
                onSend = {
                    if (input.isNotBlank()) {
                        messages.add(ChatMessage(id = "${messages.size}", isUser = true, text = input, timestamp = "Now"))
                        input = ""
                        // Add typing indicator
                        messages.add(ChatMessage(id = "typing", isUser = false, text = "", isTyping = true))
                        scope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                            kotlinx.coroutines.delay(2000)
                            messages.removeIf { it.isTyping }
                            messages.add(ChatMessage(id = "${messages.size}", isUser = false, text = "I'm processing your request. Please note that I can only access your own account information and approved policy documents.", timestamp = "Now"))
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Time separator
            item {
                Spacer(Modifier.height(8.dp))
                TimeSeparator("Today, 10:30 AM")
            }

            // Messages
            items(messages, key = { it.id }) { msg ->
                if (msg.isTyping) {
                    TypingIndicator()
                } else if (msg.isUser) {
                    UserMessage(message = msg)
                } else {
                    AIMessage(
                        message = msg,
                        onPolicyChipTap = onPolicyChipTap,
                        onEmbeddedCTA = onEmbeddedCTA,
                    )
                }
            }

            // Suggested queries
            item {
                SuggestedQueriesSection(
                    queries = suggestedQueries,
                    onQueryTap = { query ->
                        messages.add(ChatMessage(id = "${messages.size}", isUser = true, text = query.text, timestamp = "Now"))
                        scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                    },
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIChatTopBar(onBack: () -> Unit, onHumanSupport: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceBlue), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("ChitSmart AI", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyMid)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                        Text("Active", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                }
            }
        },
        actions = {
            Row(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(InputPill).padding(horizontal = 2.dp, vertical = 2.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(NavyMid).padding(horizontal = 8.dp, vertical = 3.dp)) { Text("EN", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold) }
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) { Text("हिंदी", fontSize = 10.sp, color = SlateBody) }
            }
            Spacer(Modifier.width(4.dp))
            TextButton(onClick = onHumanSupport) {
                Text("Support", fontSize = 12.sp, color = BlueAccent)
            }
        },
    )
}

// ── Chat bubbles ──────────────────────────────────────────────────────────────

@Composable
private fun TimeSeparator(label: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.shadow(1.dp, RoundedCornerShape(100.dp)).clip(RoundedCornerShape(100.dp)).background(TimePillBg).padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.33.sp)
        }
    }
}

@Composable
private fun UserMessage(message: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(UserBubble)
                .shadow(4.dp, RoundedCornerShape(topStart = 12.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(message.text, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(message.timestamp, fontSize = 11.sp, color = SlateHint, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun AIMessage(
    message: ChatMessage,
    onPolicyChipTap: (PolicyChip) -> Unit,
    onEmbeddedCTA: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // AI bubble with teal left border
        Box(modifier = Modifier.widthIn(max = 340.dp)) {
            Column(
                modifier = Modifier
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(AiBubbleBg)
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // AI header
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = TealAccent, modifier = Modifier.size(13.dp))
                    Text("ChitSmart AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyMid, letterSpacing = 0.24.sp)
                }

                // Message text
                if (message.text.isNotBlank()) {
                    Text(message.text, fontSize = 14.sp, color = NavyDeep, lineHeight = 22.sp)
                }

                // Embedded card
                message.embeddedCard?.let { card ->
                    EmbeddedPaymentCard(card = card, onCTA = onEmbeddedCTA)
                }

                // Policy chips
                if (message.policyChips.isNotEmpty()) {
                    message.policyChips.forEach { chip ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PolicyBg)
                                .clickable { onPolicyChipTap(chip) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.Gavel, contentDescription = null, tint = PolicyText, modifier = Modifier.size(12.dp))
                            Text(chip.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PolicyText, letterSpacing = 0.24.sp)
                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = PolicyText, modifier = Modifier.size(10.dp))
                        }
                    }
                }

                // Action bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(message.timestamp, fontSize = 11.sp, color = SlateHint, fontWeight = FontWeight.Medium, letterSpacing = 0.33.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = SlateBody, modifier = Modifier.size(13.dp)) }
                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.Share, contentDescription = "Share", tint = SlateBody, modifier = Modifier.size(13.dp)) }
                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(ActionBar))
                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.ThumbUp, contentDescription = "Helpful", tint = SlateBody, modifier = Modifier.size(13.dp)) }
                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.ThumbDown, contentDescription = "Not helpful", tint = SlateBody, modifier = Modifier.size(13.dp)) }
                    }
                }
            }
            // Teal accent left border
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(TealBorder).align(Alignment.CenterStart).clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
        }
    }
}

@Composable
private fun EmbeddedPaymentCard(card: EmbeddedCard, onCTA: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E8FF), RoundedCornerShape(8.dp)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(SurfaceBlue).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyMid, letterSpacing = 0.24.sp)
            card.bodyLines.forEach { line -> Text(line, fontSize = 14.sp, color = NavyDeep, lineHeight = 22.sp) }
            card.totalAmount?.let { total ->
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 14.sp)) { append("Total Due: ") }
                        withStyle(SpanStyle(color = NavyMid, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)) { append(total) }
                    },
                )
            }
        }
        card.ctaLabel?.let { label ->
            Row(
                modifier = Modifier.fillMaxWidth().background(NavyMid).clickable { onCTA() }.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, 0), RepeatMode.Reverse), label = "d1")
    val dot2 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, 200), RepeatMode.Reverse), label = "d2")
    val dot3 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, 400), RepeatMode.Reverse), label = "d3")

    Box(modifier = Modifier.shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(AiBubbleBg).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(dot1, dot2, dot3).forEach { alpha ->
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealBorder.copy(alpha = 0.4f + alpha * 0.6f)))
            }
        }
    }
}

// ── Suggested queries ─────────────────────────────────────────────────────────

@Composable
private fun SuggestedQueriesSection(queries: List<SuggestedQuery>, onQueryTap: (SuggestedQuery) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = SlateBody, modifier = Modifier.size(15.dp))
            Text("SUGGESTED QUERIES", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.275.sp)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(queries) { query ->
                Row(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { onQueryTap(query) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(query.text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyDeep)
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = TealAccent, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

// ── Input bar ─────────────────────────────────────────────────────────────────

@Composable
private fun AIChatInputBar(input: String, onInputChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = PageBg) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(Color.White).border(1.dp, Color(0xFFE0E8FF), RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(fontSize = 14.sp, color = NavyDeep),
                    cursorBrush = SolidColor(TealAccent),
                    decorationBox = { inner ->
                        if (input.isEmpty()) Text("Ask me anything…", fontSize = 14.sp, color = SlateHint)
                        inner()
                    },
                )
                Icon(Icons.Outlined.Mic, contentDescription = "Voice", tint = SlateHint, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (input.isNotBlank()) TealAccent else Color(0xFFE0E8FF))
                    .clickable(enabled = input.isNotBlank()) { onSend() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Send, contentDescription = "Send", tint = if (input.isNotBlank()) Color.White else SlateHint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Sample data matching Figma exactly ───────────────────────────────────────

private val sampleMessages = listOf(
    ChatMessage(
        id = "1",
        isUser = false,
        text = "Hello! I'm ChitSmart AI. I can help you with your account details, installment schedules, and approved chit fund policies. How can I assist you today?",
        timestamp = "10:30 AM",
    ),
    ChatMessage(
        id = "2",
        isUser = true,
        text = "What is my total due this month?",
        timestamp = "10:31 AM",
    ),
    ChatMessage(
        id = "3",
        isUser = false,
        text = "Based on your active memberships, here's your payment summary for October 2026:",
        timestamp = "10:31 AM",
        policyChips = listOf(PolicyChip("Chit Funds Act §4 — Foreman commission max 5%", "section_4")),
        embeddedCard = EmbeddedCard(
            title = "PAYMENT SUMMARY — OCT 2026",
            bodyLines = listOf(
                "Family Savings 50K: ₹2,500 + ₹50 late fee",
                "Community Chit 1L: ₹5,000 (due 20/10)",
            ),
            totalAmount = "₹7,550",
            ctaLabel = "Pay Now — Quick Settlement",
        ),
    ),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "AI Chat")
@Composable
fun AIChatScreenPreview() { MaterialTheme { AIChatScreen() } }
