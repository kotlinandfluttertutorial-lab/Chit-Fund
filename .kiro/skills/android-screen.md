---
name: android-screen
description: Step-by-step instructions for implementing a Jetpack Compose screen in the ChitSmart Android app — from ViewModel to UI to navigation wiring.
---

# Skill: Implement a ChitSmart Android Screen

Use this skill whenever you are implementing a new Compose screen or updating an existing one.

## Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **DI:** Hilt
- **Navigation:** Jetpack Navigation Compose (`NavHostController`)
- **Async:** Kotlin Coroutines + `StateFlow`
- **Network:** Retrofit 2 + OkHttp
- **Local storage:** Room (for cache), EncryptedSharedPreferences (for tokens)
- **Images:** Coil

---

## File structure for a screen

Every screen lives under `app/src/main/java/com/chitsmart/ui/<feature>/`:

```
ui/<feature>/
├── <Feature>Screen.kt          — Composable UI
├── <Feature>ViewModel.kt       — state management, business logic calls
├── <Feature>UiState.kt         — sealed class / data class for UI state
└── components/                 — sub-composables used only in this feature
    └── <FeatureSpecific>Card.kt
```

---

## Step 1 — Define UiState

```kotlin
// <Feature>UiState.kt
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val isNewUser: Boolean) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

For screens with multiple pieces of data, use a data class:

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val memberName: String = "",
    val totalInvested: Long = 0L,
    val dueInstallments: List<DueInstallment> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val error: String? = null,
)
```

---

## Step 2 — ViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun sendOtp(mobileNumber: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authUseCase.sendOtp(mobileNumber)
                .onSuccess { _uiState.value = LoginUiState.Success(isNewUser = false) }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
```

---

## Step 3 — Composable screen

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onOtpSent: (mobileNumber: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mobileNumber by remember { mutableStateOf("") }

    // Always use ChitSmartTheme tokens — never hardcode colours
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // Use OutlinedTextField with prefix chip for +91
        // ... build UI matching Figma frame

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.sendOtp("+91$mobileNumber") },
            enabled = mobileNumber.length == 10 && uiState !is LoginUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Send OTP")
            }
        }
    }

    // Handle side effects (navigation) outside the layout tree
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onOtpSent("+91$mobileNumber")
        }
    }
}
```

---

## Step 4 — Wire into NavGraph

In `ui/navigation/NavGraph.kt`, add the route under the appropriate nav graph:

```kotlin
composable(AppRoutes.LOGIN) {
    LoginScreen(
        onOtpSent = { mobile ->
            navController.navigate(AppRoutes.OTP.replace("{mobileNumber}", mobile))
        },
    )
}
```

---

## Design token rules

- **Always** use `MaterialTheme.colorScheme.*` — never raw hex or `Color(0xFF...)` in Compose UI
- The theme is defined in `ui/theme/Theme.kt`, tokens in `ui/theme/Color.kt`
- Key tokens:
  - Primary action buttons → `containerColor = MaterialTheme.colorScheme.primary` (PrimaryNavy #073763)
  - Teal accents → `MaterialTheme.colorScheme.tertiary` (TealAccent #14B8A6)
  - Background → `MaterialTheme.colorScheme.background` (#F7FAFC)
  - Error → `MaterialTheme.colorScheme.error` (#EF4444)
- Spacing: use multiples of 4.dp (4, 8, 12, 16, 20, 24, 32, 40, 48)
- Touch targets: minimum 48.dp × 48.dp
- Corner radii: cards=12.dp, buttons=8.dp, chips=20.dp

---

## Accessibility rules

- All `Image` and `Icon` composables must have `contentDescription`
- Interactive elements must have `semantics { role = Role.Button }` or equivalent
- All text must pass WCAG AA contrast (enforced by the theme)
- Do not rely on colour alone to convey state — use icons + text

---

## Scope guards (check before implementing)

- **No KYC screens** — KYC is deferred. See `docs/scope-decisions.md`.
- **No Auctions tab** — Auctions are deferred. Bottom nav is: Home | My Chits | Payments | Assistant | Profile.
- **No "Gold Saver 50K"** in any mock data, previews, or strings.
- **No bid-related UI** (bid history, Place Bid, Upcoming Auctions).

---

## Figma reference

Figma file: `https://www.figma.com/design/H3sDKKBcj9dveyJdgs7Jn5/Untitled`

Look up the node ID for the target screen in `docs/design/figma-mcp-implementation.md` (Section 6 — Screen-to-Code Mapping) and use Figma MCP to extract exact layout values when available.
