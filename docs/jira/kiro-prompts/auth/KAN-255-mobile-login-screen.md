# Kiro Prompt — KAN-255: Mobile Number Login Screen

**Ticket:** KAN-255  
**Epic:** KAN-241 — Authentication & Registration  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Auth-1.3-MobileLogin-Default` — node `1:2`  
**File:** `app/src/main/java/com/chitsmart/ui/auth/LoginScreen.kt`

---

## Task

Implement the mobile number login screen. This is the main entry point for returning users.

## Implementation

### `LoginScreen.kt`
- Top app bar: ChitSmart logo (left), language toggle chip `EN | हिंदी` (right)
- Headline: "Enter your mobile number" — `headlineMedium`, `MainText`
- Mobile input row:
  - `+91` prefix chip (Surface variant background, non-editable)
  - `BasicTextField` — `KeyboardType.Number`, `KeyboardCapitalization.None`, max 10 chars
  - Validation: exactly 10 digits, first digit 6–9
  - Inline error below field: "Enter a valid 10-digit mobile number" (red, visible on invalid submit attempt)
- "Send OTP" `Button` — full width, PrimaryNavy fill
  - Disabled when input < 10 digits
  - Shows `CircularProgressIndicator` (20dp, white) when loading
- "New to ChitSmart? Register" `TextButton` — centred below button
- Terms disclaimer: "By continuing, you agree to our Terms of Service and Privacy Policy" — `bodySmall`, `SecondaryText`, centred
- On success: `navController.navigate(AppRoutes.OTP.replace("{mobileNumber}", "+91$input"))`

### `LoginViewModel.kt`
```kotlin
fun sendOtp(mobileNumber: String) {
    viewModelScope.launch {
        _uiState.value = LoginUiState.Loading
        authRepository.sendOtp(mobileNumber)
            .onSuccess { _uiState.value = LoginUiState.OtpSent }
            .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Failed to send OTP") }
    }
}
```

## Acceptance criteria
- [ ] Button disabled when input is less than 10 digits
- [ ] Validates first digit is 6–9 before API call
- [ ] Loading spinner replaces button text during API call
- [ ] Error message appears inline under input field (not a toast)
- [ ] Language toggle is visible and tappable (language switch can be a no-op stub in this ticket)
- [ ] Matches Figma frame `App-Auth-1.3-MobileLogin-Default`
