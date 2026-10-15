# Kiro Prompt — KAN-257: New User Registration

**Ticket:** KAN-257  
**Epic:** KAN-241 — Authentication & Registration  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Auth-1.5-Registration-Default` — node `1:1570`  
**File:** `app/src/main/java/com/chitsmart/ui/auth/RegistrationScreen.kt`

---

## Task

Implement the new user registration screen. Only shown once, after OTP verification for a new mobile number.

## Fields

| Field | Type | Required | Notes |
|---|---|---|---|
| Mobile number | Pre-filled, read-only | — | Passed from OTP screen, show locked icon |
| Full name | `OutlinedTextField` | Yes | Min 2 characters |
| Email | `OutlinedTextField` | No | `KeyboardType.Email`, validate format if provided |
| Date of birth | Tap to open `DatePickerDialog` | No | Max date = today − 18 years |
| Gender | `ExposedDropdownMenuBox` | No | Options: Male, Female, Other, Prefer not to say |
| Consent | `Checkbox` + `ClickableText` | Yes | "I agree to the Terms and Conditions" — Terms opens WebView |

**DO NOT add Aadhaar or PAN fields. KYC is deferred.**

## Implementation

### `RegistrationScreen.kt`
- `LazyColumn` layout (keyboard pushes up properly)
- Mobile number row: `OutlinedTextField` disabled + `Lock` icon trailing
- All field validation triggered on "Register" button tap (not on each keystroke)
- Inline error messages below each field
- "Register" `Button` — full width, PrimaryNavy, disabled until consent checked
- Loading state: spinner in button
- On success: navigate to `AppRoutes.HOME`, clearing the back stack

### `RegistrationViewModel.kt`
```kotlin
fun register(fullName: String, email: String?, dob: String?, gender: String?) {
    viewModelScope.launch {
        _uiState.value = RegistrationUiState.Loading
        authRepository.register(fullName, email, dob, gender)
            .onSuccess { _uiState.value = RegistrationUiState.Success }
            .onFailure { _uiState.value = RegistrationUiState.Error(it.message ?: "Registration failed") }
    }
}
```

## Acceptance criteria
- [ ] Mobile number is pre-filled and non-editable
- [ ] NO Aadhaar or PAN fields anywhere on the screen
- [ ] Full name validation shows inline error if empty on submit
- [ ] Email validation shows inline error for invalid format (only if provided)
- [ ] Register button disabled until consent checkbox is checked
- [ ] Successful registration navigates to Home, back stack cleared (user cannot go back to registration)
- [ ] Matches Figma frame `App-Auth-1.5-Registration-Default`
