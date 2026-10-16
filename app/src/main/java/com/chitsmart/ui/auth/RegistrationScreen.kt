package com.chitsmart.ui.auth

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens from Figma (node 1:1570) ───────────────────────────────────
private val PageBg       = Color(0xFFF9F9FF)
private val NavyDeep     = Color(0xFF041B3C)
private val NavyMid      = Color(0xFF002141)
private val TealAccent   = Color(0xFF00B3A1)
private val TealDark     = Color(0xFF003E37)
private val TealBadgeBg  = Color(0xFFE6FAF3)
private val BlueAccent   = Color(0xFF0261A2)
private val ErrorRed     = Color(0xFFBA1A1A)
private val SlateBody    = Color(0xFF43474F)
private val SlateHint    = Color(0xFF737780)
private val SurfaceBlue  = Color(0xFFF1F3FF)
private val InputPill    = Color(0xFFE8EDFF)
private val FieldBg      = Color.White
private val FieldBorder  = Color(0xFFE0E8FF)
private val LockedBg     = Color(0xFFF1F3FF)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RegistrationScreen(
    mobileNumber: String = "98765 43210",
    onRegister: (fullName: String, email: String?, dob: String?, gender: String?) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit = {},
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var consentChecked by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
    val isFormValid = fullName.isNotBlank() && consentChecked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .systemBarsPadding(),
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(InputPill).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = NavyDeep, modifier = Modifier.size(13.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create your account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TealAccent))
                    Text("Verified Rotational Chit Network", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealDark, letterSpacing = 0.33.sp)
                }
            }
            Box(modifier = Modifier.size(40.dp)) // spacer for symmetry
        }

        // Progress bar
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("STEP 1 OF 3: ACCOUNT CREATION", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SlateBody, letterSpacing = 0.55.sp)
                Text("35% Completed", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = BlueAccent, letterSpacing = 0.33.sp)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(InputPill)) {
                Box(modifier = Modifier.fillMaxWidth(0.35f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(NavyMid))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Create your account", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = (-0.6).sp, lineHeight = 32.sp)
            Text("Enter your basic details to start saving in verified chit circles.", fontSize = 14.sp, color = SlateBody, lineHeight = 20.sp)
        }

        Spacer(Modifier.height(16.dp))

        // ── Form ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Field 1: Full Name
            FormField(
                label = "Full Name",
                hint = "As per official ID",
                value = fullName,
                onValueChange = { fullName = it; nameError = null },
                isRequired = true,
                error = nameError,
                leadingIcon = Icons.Outlined.Person,
            )

            // Field 2: Mobile (locked)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mobile Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.14.sp)
                    Text("*", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                    Spacer(Modifier.weight(1f))
                    Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(TealBadgeBg).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TealAccent, modifier = Modifier.size(11.dp))
                        Text("OTP Verified", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TealDark, letterSpacing = 0.33.sp)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(LockedBg).border(1.dp, FieldBorder, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("🇮🇳 +91", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyMid, letterSpacing = 0.24.sp)
                    Text(mobileNumber, fontSize = 16.sp, color = NavyDeep, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = SlateBody, modifier = Modifier.size(14.dp))
                }
                Text("Linked to registration credentials", fontSize = 12.sp, color = SlateBody)
            }

            // Field 3: Email (optional)
            FormField(
                label = "Email Address",
                isOptional = true,
                hint = "For cycle dividend alerts",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Outlined.Email,
            )

            // Field 4: Date of Birth
            FormField(
                label = "Date of Birth",
                hint = "DD/MM/YYYY",
                value = dob,
                onValueChange = { dob = it },
                leadingIcon = Icons.Outlined.CalendarToday,
                trailingIcon = Icons.Outlined.CalendarMonth,
            )

            // Field 5: Gender
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Gender", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.14.sp)
                ExposedDropdownMenuBox(expanded = showGenderMenu, onExpandedChange = { showGenderMenu = it }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).menuAnchor().clip(RoundedCornerShape(8.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp))
                            Text(if (gender.isBlank()) "Select gender" else gender, fontSize = 16.sp, color = if (gender.isBlank()) SlateHint else NavyDeep)
                        }
                        Icon(if (showGenderMenu) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null, tint = SlateBody)
                    }
                    ExposedDropdownMenu(expanded = showGenderMenu, onDismissRequest = { showGenderMenu = false }) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { gender = option; showGenderMenu = false })
                        }
                    }
                }
            }

            // Consent
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceBlue).padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(
                    checked = consentChecked,
                    onCheckedChange = { consentChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = NavyMid),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append("I agree to the ") }
                        withStyle(SpanStyle(color = BlueAccent, fontSize = 12.sp, textDecoration = TextDecoration.Underline)) { append("Terms and Conditions") }
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append(" and ") }
                        withStyle(SpanStyle(color = BlueAccent, fontSize = 12.sp, textDecoration = TextDecoration.Underline)) { append("Privacy Policy") }
                        withStyle(SpanStyle(color = SlateBody, fontSize = 12.sp)) { append(" of ChitSmart and authorise Chit Funds Act mandated collections.") }
                    },
                    lineHeight = 16.sp,
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Register CTA ──
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    if (fullName.isBlank()) { nameError = "Full name is required"; return@Button }
                    isLoading = true
                    onRegister(fullName, email.ifBlank { null }, dob.ifBlank { null }, gender.ifBlank { null })
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyMid, disabledContainerColor = NavyMid.copy(0.5f)),
                elevation = ButtonDefaults.buttonElevation(4.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create Account & Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    isOptional: Boolean = false,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep, letterSpacing = 0.14.sp)
            if (isRequired) Text(" *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
            if (isOptional) Text(" (Optional)", fontSize = 12.sp, color = SlateBody)
            Spacer(Modifier.weight(1f))
            Text(hint, fontSize = 12.sp, color = SlateBody)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(if (error != null) Color(0xFFFFEDED) else FieldBg).border(1.dp, if (error != null) ErrorRed else FieldBorder, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.let { Icon(it, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp)) }
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
            trailingIcon?.let { Icon(it, contentDescription = null, tint = SlateBody, modifier = Modifier.size(16.dp)) }
        }
        error?.let { Text(it, fontSize = 12.sp, color = ErrorRed) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Registration")
@Composable
fun RegistrationScreenPreview() { MaterialTheme { RegistrationScreen() } }
