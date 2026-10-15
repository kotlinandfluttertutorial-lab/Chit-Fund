# Kiro Prompt — KAN-253: Splash Screen

**Ticket:** KAN-253  
**Epic:** KAN-241 — Authentication & Registration  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Auth-1.1-Splash-Default` — node `1:1415`  
**File:** `app/src/main/java/com/chitsmart/ui/splash/SplashScreen.kt`

---

## Task

Implement the ChitSmart splash screen as the first screen shown on cold app start.

## Implementation

### `SplashScreen.kt`
- Full-bleed `MaterialTheme.colorScheme.primary` (`#073763`) background
- Centred `Column`: ChitSmart logo drawable + wordmark text in white `headlineLarge`
- Tagline: "Your trusted chit fund partner" — `bodyMedium`, white, `fontWeight = Light`
- `LinearProgressIndicator` at bottom, color = `MaterialTheme.colorScheme.tertiary` (teal)
- Version number `Text` bottom-right, `bodySmall`, white 60% alpha

### Navigation logic
- Read DataStore key `onboarding_completed` (Boolean, default false)
- After 2000ms delay (`kotlinx.coroutines.delay`):
  - `false` → navigate to `AppRoutes.ONBOARDING`
  - `true` → navigate to `AppRoutes.LOGIN`
- Use `LaunchedEffect(Unit)` inside the composable for the delay + navigation

### ViewModel: `SplashViewModel.kt`
```kotlin
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository,
) : ViewModel() {
    val isOnboardingCompleted: Flow<Boolean> =
        userPrefsRepository.isOnboardingCompleted
}
```

### DataStore key
Add to `data/local/UserPrefsRepository.kt`:
```kotlin
val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[ONBOARDING_COMPLETED_KEY] ?: false
}
private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
```

## Acceptance criteria
- [ ] Screen appears immediately on app launch — no white flash
- [ ] Correct background colour (#073763) fills status bar and navigation bar too (edge-to-edge)
- [ ] Progress bar animates at the bottom
- [ ] Navigates to Onboarding on first launch, Login on subsequent launches
- [ ] Matches Figma frame `App-Auth-1.1-Splash-Default`
