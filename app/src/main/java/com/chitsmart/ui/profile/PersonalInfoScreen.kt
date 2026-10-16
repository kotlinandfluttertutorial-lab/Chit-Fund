package com.chitsmart.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val PageBg       = Color(0xFFF9F9FF)
private val NavyDeep     = Color(0xFF041B3C)
private val NavyMid      = Color(0xFF002141)
private val TealAccent   = Color(0xFF00B3A1)
private val BlueAccent   = Color(0xFF0261A2)
private val ErrorRed     = Color(0xFFBA1A1A)
private val SlateBody    = Color(0xFF43474F)
private val SlateHint    = Color(0xFF737780)
private val SurfaceBlue  = Color(0xFFF1F3FF)
private val LockedBg     = Color(0xFFF5F5F5)
private val LockedText   = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    initialName: String = "Priya Sharma",
    initialEmail: String = "priya.sharma@example.com",
    initialAltMobile: String = "",
    primaryMobile: String = "+91 98765 43210",
    dob: String = "12 Mar 1990",
    memberId: String = "CS-8402",
    onBack: () -> Unit = {},
    onSave: (name: String, email: String, altMobile: String) -> Unit = { _, _, _ -> },
) {
    var fullName by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf(initialEmail) }
    var altMobile by remember { mutableStateOf(initialAltMobile) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var altMobileError by remember { mutableStateOf<String?>(null) }

    val hasChanges = fullName != initialName || email != initialEmail || altMobile != initialAltMobile

    fun validate(): Boolean {
        nameError = if (fullName.isBlank()) "Full name is required" else null
        emailError = if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Enter a valid email address" else null
        altMobileError = if (altMobile.isNotBlank() && !altMobile.matches(Regex("^[6-9]\\d{9}\$"))) "Enter a valid 10-digit mobile number" else null
        return nameError == null && emailError == null && altMobileError == null
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
                        Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = NavyDeep)
                        Text("Manage your profile details", fontSize = 11.sp, color = SlateBody, letterSpacing = 0.33.sp)
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = PageBg) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = { if (validate()) onSave(fullName, email, altMobile) },
                        enabled = hasChanges,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealAccent,
                            disabledContainerColor = Color(0xFFCCE9E6),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Editable fields card
            Column(
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("EDITABLE INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent, letterSpacing = 0.55.sp)

                // Full Name
                EditableField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it; nameError = null },
                    placeholder = "Enter your full name",
                    error = nameError,
                    keyboardType = KeyboardType.Text,
                )

                // Email
                EditableField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    placeholder = "Enter email address",
                    error = emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                )

                // Alternate Mobile
                EditableField(
                    label = "Alternate Mobile Number",
                    value = altMobile,
                    onValueChange = { altMobile = it; altMobileError = null },
                    placeholder = "Enter alternate mobile (optional)",
                    error = altMobileError,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                )
            }

            // Locked fields card
            Column(
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("LOCKED INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateHint, letterSpacing = 0.55.sp)
                Text("These fields can only be changed by contacting support.", fontSize = 12.sp, color = SlateBody)

                LockedField(label = "Primary Mobile Number", value = primaryMobile)
                LockedField(label = "Date of Birth", value = dob)
                LockedField(label = "Member ID", value = "#$memberId")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateBody)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SlateHint) },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                errorBorderColor = ErrorRed,
                focusedTextColor = NavyDeep,
                unfocusedTextColor = NavyDeep,
            ),
        )
        if (error != null) {
            Text(error, fontSize = 11.sp, color = ErrorRed)
        }
    }
}

@Composable
private fun LockedField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LockedBg).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = SlateHint)
            Text(value, fontSize = 14.sp, color = LockedText, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = LockedText, modifier = Modifier.size(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860, name = "Personal Info")
@Composable
fun PersonalInfoScreenPreview() { MaterialTheme { PersonalInfoScreen() } }
