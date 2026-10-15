# Kiro Prompt — KAN-256: OTP Verification Screen

**Ticket:** KAN-256  
**Epic:** KAN-241 — Authentication & Registration  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Auth-1.4-OTPVerification-Default` — node `1:115`  
**File:** `app/src/main/java/com/chitsmart/ui/auth/OTPScreen.kt`

---

## Task

Implement the OTP verification screen. User arrives here after entering their mobile number.

## Implementation

### `OTPScreen.kt`
- Headline: "Verify your number" — `headlineMedium`
- Sub-text: "OTP sent to +91 XXXXX XX210" (last 5 digits shown, rest masked) — `bodyMedium`, `SecondaryText`
- OTP input: 6 individual single-character `BasicTextField` boxes in a `Row`
  - Each box: 48dp × 56dp, `OutlinedBorder`, corner 8dp
  - Auto-advance focus to next box on digit entry
  - Auto-move focus back on backspace when empty
  - Filled border = `TealAccent`, empty border = `BorderDivider`
  - Error state: all boxes get `ErrorRed` border
- Countdown timer: "Resend OTP in **00:45**" — counting down from 45s
  - When reaches 0: show "Resend OTP" `TextButton` (teal, enabled)
  - Use `LaunchedEffect` + `ticker` coroutine for countdown
- "Verify" `Button` — full width, PrimaryNavy fill
  - Disabled when fewer than 6 digits entered
  - Loading state: `CircularProgressIndicator`
- Inline error text below boxes: "Incorrect OTP. Please try again." — red, `bodySmall`
- On success: navigate to `AppRoutes.REGISTRATION` (new user) or `AppRoutes.HOME` (existing)

### `OTPViewModel.kt`
```kotlin
fun verifyOtp(mobileNumber: String, otp: String) {
    viewModelScope.launch {
        _uiState.value = OTPUiState.Loading
        authRepository.verifyOtp(mobileNumber, otp)
            .onSuccess { response ->
                _uiState.value = OTPUiState.Success(isNewUser = response.isNewUser)
            }
            .onFailure { _uiState.value = OTPUiState.Error("Incorrect OTP. Please try again.") }
    }
}
```

## Acceptance criteria
- [ ] Auto-advances focus between the 6 boxes on digit entry
- [ ] Backspace on empty box moves focus to previous box
- [ ] Countdown timer works accurately; Resend enabled only after it reaches 0
- [ ] Incorrect OTP shows red border on all 6 boxes with inline error message
- [ ] Correct OTP navigates to Registration (new user) or Home (existing user)
- [ ] Matches Figma frame `App-Auth-1.4-OTPVerification-Default`
