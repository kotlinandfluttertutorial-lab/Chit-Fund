package com.chitsmart.ui.chits

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg = Color(0xFFF9F9FF)
private val NavyDeep = Color(0xFF041B3C)
private val NavyMid = Color(0xFF002141)
private val TealAccent = Color(0xFF00B3A1)
private val BlueAccent = Color(0xFF0261A2)
private val ErrorRed = Color(0xFFBA1A1A)
private val SlateBody = Color(0xFF43474F)
private val SlateHint = Color(0xFF737780)
private val SurfaceBlue = Color(0xFFF1F3FF)
private val InputPill = Color(0xFFE8EDFF)
private val FieldBg = Color.White
private val FieldBorder = Color(0xFFE0E8FF)
private val SuccessGreen = Color(0xFF22C55E)

// ── State machine ─────────────────────────────────────────────────────────────

enum class EnrollmentStep { SUMMARY, NOMINEE, MANDATE, ESIGN, SUCCESS }

@Composable
fun EnrollmentScreen(
    groupName: String = "Family Savings 50K",
    chitValue: String = "₹50,000",
    monthlyInstallment: String = "₹2,500",
    durationMonths: Int = 24,
    foremanCommission: String = "5%",
    onBack: () -> Unit = {},
    onEnrollmentComplete: (membershipId: String) -> Unit = {},
) {
    var step by remember { mutableStateOf(EnrollmentStep.SUMMARY) }

    // Nominee fields
    var nomineeName by remember { mutableStateOf("") }
    var nomineeRelationship by remember { mutableStateOf("") }
    var nomineeMobile by remember { mutableStateOf("") }

    // Bank mandate
    var selectedBankAccount by remember { mutableStateOf("HDFC Bank ••••4092") }
    var autoDebitEnabled by remember { mutableStateOf(true) }

    // E-sign
    var scrolledToBottom by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(false) }

    // Back navigation between steps
    BackStepHandler(step = step, onBack = onBack) { step = it }

    when (step) {
        EnrollmentStep.SUMMARY ->
            SummaryStep(
                groupName = groupName,
                chitValue = chitValue,
                monthlyInstallment = monthlyInstallment,
                durationMonths = durationMonths,
                foremanCommission = foremanCommission,
                currentStep = 1,
                onContinue = { step = EnrollmentStep.NOMINEE },
                onBack = onBack,
            )
        EnrollmentStep.NOMINEE ->
            NomineeStep(
                nomineeName = nomineeName,
                relationship = nomineeRelationship,
                mobile = nomineeMobile,
                onNameChange = { nomineeName = it },
                onRelationshipChange = { nomineeRelationship = it },
                onMobileChange = { nomineeMobile = it },
                onContinue = { step = EnrollmentStep.MANDATE },
                onBack = { step = EnrollmentStep.SUMMARY },
            )
        EnrollmentStep.MANDATE ->
            MandateStep(
                selectedAccount = selectedBankAccount,
                autoDebitEnabled = autoDebitEnabled,
                onAutoDebitToggle = { autoDebitEnabled = it },
                onContinue = { step = EnrollmentStep.ESIGN },
                onBack = { step = EnrollmentStep.NOMINEE },
            )
        EnrollmentStep.ESIGN ->
            ESignStep(
                groupName = groupName,
                scrolledToBottom = scrolledToBottom,
                onScrolledToBottom = { scrolledToBottom = true },
                agreed = agreed,
                onAgreedChange = { agreed = it },
                onConfirm = { step = EnrollmentStep.SUCCESS },
                onBack = { step = EnrollmentStep.MANDATE },
            )
        EnrollmentStep.SUCCESS ->
            SuccessStep(
                groupName = groupName,
                membershipId = "CS-MEM-2026-${(10000..99999).random()}",
                onViewMyChit = { onEnrollmentComplete("") },
                onGoHome = onBack,
            )
    }
}

// ── Back handler helper ───────────────────────────────────────────────────────

@Composable
private fun BackStepHandler(
    step: EnrollmentStep,
    onBack: () -> Unit,
    onStepBack: (EnrollmentStep) -> Unit,
) {
    androidx.activity.compose.BackHandler(enabled = step != EnrollmentStep.SUCCESS) {
        when (step) {
            EnrollmentStep.SUMMARY -> onBack()
            EnrollmentStep.NOMINEE -> onStepBack(EnrollmentStep.SUMMARY)
            EnrollmentStep.MANDATE -> onStepBack(EnrollmentStep.NOMINEE)
            EnrollmentStep.ESIGN -> onStepBack(EnrollmentStep.MANDATE)
            EnrollmentStep.SUCCESS -> {} // no back from success
        }
    }
}

// ── Step progress indicator ───────────────────────────────────────────────────

@Composable
private fun EnrollmentStepProgress(currentStep: Int) {
    val steps = listOf("Summary", "Nominee", "Mandate", "E-Sign")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Step $currentStep of ${steps.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BlueAccent,
                letterSpacing = 0.33.sp,
            )
            Text("${(currentStep * 100 / steps.size)}% complete", fontSize = 11.sp, color = SlateBody)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            steps.forEachIndexed { i, _ ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i < currentStep) NavyMid else Color(0xFFE0E8FF)),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEachIndexed { i, label ->
                Text(
                    label,
                    fontSize = 10.sp,
                    color =
                        if (i < currentStep) {
                            NavyMid
                        } else if (i == currentStep - 1) {
                            BlueAccent
                        } else {
                            SlateHint
                        },
                    fontWeight = if (i == currentStep - 1) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Step 1: Summary ───────────────────────────────────────────────────────────

@Composable
private fun SummaryStep(
    groupName: String,
    chitValue: String,
    monthlyInstallment: String,
    durationMonths: Int,
    foremanCommission: String,
    currentStep: Int,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val commitmentAmount =
        monthlyInstallment
            .replace("₹", "")
            .replace(",", "")
            .trim()
            .toLongOrNull()
            ?.let { it * durationMonths }
            ?.let { "%,d".format(it) } ?: "—"
    val totalCommitment = "₹$commitmentAmount"

    Column(modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding()) {
        EnrollmentTopBar(title = "Enrollment", subtitle = groupName, onBack = onBack)
        EnrollmentStepProgress(currentStep = 1)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Summary card
            Column(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Enrollment Summary", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                HorizontalDivider(color = SurfaceBlue)
                SummaryRow("Group", groupName)
                SummaryRow("Chit Value", chitValue)
                SummaryRow("Monthly Installment", monthlyInstallment)
                SummaryRow("Duration", "$durationMonths months")
                SummaryRow("Foreman Commission", foremanCommission)
                HorizontalDivider(color = SurfaceBlue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Total Commitment", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text(totalCommitment, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TealAccent)
                }
            }

            // Legal note
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Gavel,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                )
                Text(
                    "All enrollments are governed by the Chit Funds Act, 1982 and the group's registered terms document.",
                    fontSize = 11.sp,
                    color = SlateBody,
                    lineHeight = 15.sp,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Text("Continue to Nominee Details →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = SlateBody)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
    }
}

// ── Step 2: Nominee ───────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NomineeStep(
    nomineeName: String,
    relationship: String,
    mobile: String,
    onNameChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    var showRelMenu by remember { mutableStateOf(false) }
    val relationships = listOf("Spouse", "Parent", "Child", "Sibling", "Other")
    val isValid = nomineeName.isNotBlank() && relationship.isNotBlank() && mobile.length == 10

    Column(modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding()) {
        EnrollmentTopBar(title = "Nominee Details", subtitle = "Who should receive in your absence", onBack = onBack)
        EnrollmentStepProgress(currentStep = 2)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EnrollmentTextField(
                label = "Nominee Full Name",
                value = nomineeName,
                onValueChange = onNameChange,
                hint = "Full legal name",
                isRequired = true,
                icon = Icons.Outlined.Person,
            )

            // Relationship dropdown
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text("Relationship", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.14.sp)
                    Text(" *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                }
                ExposedDropdownMenuBox(expanded = showRelMenu, onExpandedChange = { showRelMenu = it }) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().height(
                                48.dp,
                            ).menuAnchor().clip(
                                RoundedCornerShape(8.dp),
                            ).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.People, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp))
                            Text(
                                if (relationship.isBlank()) "Select relationship" else relationship,
                                fontSize = 16.sp,
                                color = if (relationship.isBlank()) SlateHint else NavyDeep,
                            )
                        }
                        Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = SlateBody)
                    }
                    ExposedDropdownMenu(expanded = showRelMenu, onDismissRequest = { showRelMenu = false }) {
                        relationships.forEach { rel ->
                            DropdownMenuItem(text = { Text(rel) }, onClick = {
                                onRelationshipChange(rel)
                                showRelMenu = false
                            })
                        }
                    }
                }
            }

            EnrollmentTextField(
                label = "Nominee Mobile",
                value = mobile,
                onValueChange = {
                    if (it.length <= 10 && it.all(Char::isDigit)) onMobileChange(it)
                },
                hint = "10-digit mobile number",
                isRequired = true,
                icon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Number,
                prefix = "+91 ",
            )

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                Text(
                    "Nominee details can be updated later from your Profile → Nominees section.",
                    fontSize = 11.sp,
                    color = SlateBody,
                    lineHeight = 15.sp,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = onContinue,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid, disabledContainerColor = NavyMid.copy(0.5f)),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Text("Continue to Bank Mandate →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Step 3: Bank Mandate ──────────────────────────────────────────────────────

@Composable
private fun MandateStep(
    selectedAccount: String,
    autoDebitEnabled: Boolean,
    onAutoDebitToggle: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding()) {
        EnrollmentTopBar(title = "Bank Mandate", subtitle = "Set up automatic installment collection", onBack = onBack)
        EnrollmentStepProgress(currentStep = 3)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Bank account selector
            Column(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Select Bank Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(InputPill), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedAccount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("Savings Account • Verified", fontSize = 12.sp, color = SlateBody)
                    }
                    Box(
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(NavyMid).border(2.dp, NavyMid, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    }
                }
                Row(
                    modifier =
                        Modifier.fillMaxWidth().clip(
                            RoundedCornerShape(8.dp),
                        ).border(1.dp, FieldBorder, RoundedCornerShape(8.dp)).background(FieldBg).padding(12.dp).clickable {
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBlue), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                    }
                    Text("Add Bank Account", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BlueAccent)
                }
            }

            // Auto-debit toggle
            Row(
                modifier =
                    Modifier.fillMaxWidth().shadow(
                        1.dp,
                        RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Auto-Debit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                    Text(
                        "Monthly installments collected automatically on due date",
                        fontSize = 12.sp,
                        color = SlateBody,
                        lineHeight = 16.sp,
                    )
                }
                Switch(
                    checked = autoDebitEnabled,
                    onCheckedChange = onAutoDebitToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Security, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                Text(
                    "Auto-debit mandates are secured under RBI's NACH framework. You can revoke at any time from Profile → Bank Accounts.",
                    fontSize = 11.sp,
                    color = SlateBody,
                    lineHeight = 15.sp,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                Text("Continue to E-Sign →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Step 4: E-Sign ────────────────────────────────────────────────────────────

@Composable
private fun ESignStep(
    groupName: String,
    scrolledToBottom: Boolean,
    onScrolledToBottom: () -> Unit,
    agreed: Boolean,
    onAgreedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    val isLoading = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding()) {
        EnrollmentTopBar(title = "Agreement & E-Sign", subtitle = "Review and confirm enrollment", onBack = onBack)
        EnrollmentStepProgress(currentStep = 4)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier =
                    Modifier.fillMaxWidth().padding(
                        horizontal = 16.dp,
                    ).shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                    Text("Group Terms & Conditions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                }
                HorizontalDivider(color = SurfaceBlue)
                // Scrollable terms content
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = NavyDeep, fontSize = 13.sp)) {
                            append("CHITSMART CHIT FUND ENROLLMENT AGREEMENT\n\n")
                        }
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) {
                            append(
                                "This agreement is entered into between ChitSmart Foreman Services Pvt. Ltd. " +
                                    "(hereinafter \"Foreman\") and the Member (hereinafter \"Member\") " +
                                    "for participation in the chit group \"$groupName\".\n\n",
                            )
                            append(
                                "1. INSTALLMENT OBLIGATIONS\nThe Member agrees to pay the stipulated monthly " +
                                    "installment on or before the due date. Late payments attract a fee as " +
                                    "specified in the schedule.\n\n",
                            )
                            append(
                                "2. PAYOUT DISBURSEMENT\nPayouts are made via NEFT/IMPS to the registered " +
                                    "bank account within 3 working days of payout cycle completion.\n\n",
                            )
                            append(
                                "3. FOREMAN COMMISSION\nForeman is entitled to a commission not exceeding " +
                                    "5% of the chit value as permitted under the Chit Funds Act, 1982.\n\n",
                            )
                            append(
                                "4. NOMINEE DESIGNATION\nIn the event of the Member's demise, the nominee " +
                                    "designated during enrollment shall be entitled to receive the Member's share.\n\n",
                            )
                            append(
                                "5. GOVERNING LAW\nThis agreement is governed by the Chit Funds Act, 1982 " +
                                    "and applicable state chit fund rules.\n\n",
                            )
                            append(
                                "6. DISPUTE RESOLUTION\nAny dispute arising out of this agreement shall be " +
                                    "referred to the jurisdictional Chit Fund Registrar.\n\n",
                            )
                            append(
                                "By checking the consent box and tapping Confirm Enrollment, the Member " +
                                    "agrees to be bound by all terms above.",
                            )
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                // Detect scroll to bottom
                if (!scrolledToBottom) {
                    SideEffect { onScrolledToBottom() }
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(
                        horizontal = 16.dp,
                    ).clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = onAgreedChange,
                    colors = CheckboxDefaults.colors(checkedColor = NavyMid),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "I have read and agree to the Group Terms & Conditions and authorise the Foreman to " +
                        "collect monthly installments as per the NACH mandate.",
                    fontSize = 12.sp,
                    color = SlateBody,
                    lineHeight = 16.sp,
                )
            }

            Spacer(Modifier.height(4.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    isLoading.value = true
                    onConfirm()
                },
                enabled = agreed && !isLoading.value,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent, disabledContainerColor = TealAccent.copy(0.5f)),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Enrollment", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

// ── Step 5: Success ───────────────────────────────────────────────────────────

@Composable
private fun SuccessStep(
    groupName: String,
    membershipId: String,
    onViewMyChit: () -> Unit,
    onGoHome: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val scale by infiniteTransition.animateFloat(
        0.95f,
        1.05f,
        infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale",
    )

    Column(
        modifier = Modifier.fillMaxSize().background(PageBg).systemBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Success icon with scale animation
        Box(
            modifier = Modifier.size((80 * scale).dp).clip(CircleShape).background(SuccessGreen.copy(0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text("🎉 Enrollment Successful!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyDeep, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("You're now a member of $groupName", fontSize = 16.sp, color = SlateBody, textAlign = TextAlign.Center, lineHeight = 22.sp)

        Spacer(Modifier.height(24.dp))

        // Membership ID card
        Column(
            modifier =
                Modifier.fillMaxWidth().shadow(
                    2.dp,
                    RoundedCornerShape(16.dp),
                ).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("MEMBERSHIP ID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.55.sp)
            Text(membershipId, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyDeep, textAlign = TextAlign.Center)
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save this ID for your records", fontSize = 11.sp, color = BlueAccent)
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onViewMyChit,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
            elevation = ButtonDefaults.buttonElevation(4.dp),
        ) {
            Text("View My Chit →", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onGoHome) {
            Text("Go to Home", fontSize = 14.sp, color = BlueAccent)
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep) } },
        title = {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Text(subtitle, fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp, maxLines = 1)
            }
        },
    )
}

@Composable
private fun EnrollmentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isRequired: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.14.sp)
            if (isRequired) Text(" *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
        }
        Row(
            modifier =
                Modifier.fillMaxWidth().height(
                    48.dp,
                ).clip(
                    RoundedCornerShape(8.dp),
                ).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp))
            if (prefix != null) Text(prefix, fontSize = 16.sp, color = SlateBody)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 16.sp, color = NavyDeep),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(TealAccent),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(hint, fontSize = 16.sp, color = SlateHint)
                    inner()
                },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Enrollment — Summary")
@Composable
fun EnrollmentScreenPreview() {
    MaterialTheme { EnrollmentScreen() }
}
