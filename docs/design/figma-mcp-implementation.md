# Figma MCP Implementation Readiness

## Status: Pending — Figma file has not yet been created or populated.

**Scope update (15/10/2026):** Three scope decisions have been applied. Gold Saver 50K is removed. KYC screens are deferred. Bidding/auction screens are deferred. The screen-to-code mapping table below reflects the updated scope. Deferred screens are excluded. See `docs/scope-decisions.md` for full rationale.

This document defines the prerequisites, workflow and acceptance criteria for using the Figma MCP (Model Context Protocol) integration to drive code generation for the ChitSmart platform.

---

## 1. What is Figma MCP

The Figma MCP is a tool integration available inside Kiro that allows the AI agent to:

- Read design context (components, layout, styles, constraints) directly from a Figma file.
- Extract design tokens (colours, typography, spacing) without manual copy-paste.
- Map Figma frames to target implementation components.
- Compare generated UI against the source Figma frame visually.

---

## 2. Connection Prerequisites

Before Figma MCP can be used for implementation, all of the following must be true:

| Prerequisite | How to satisfy | Status |
|---|---|---|
| Figma file exists and is accessible | Create via Figma → New File, share with "Anyone with the link can view" | ⬜ Pending |
| Figma file URL is recorded | Add URL to `docs/design/figma-file-registry.md` | ⬜ Pending |
| All batch designs are transferred to Figma | Follow `stitch-to-figma-handoff.md` | ⬜ Pending |
| Figma MCP is connected in Kiro | Open Kiro → MCP Servers panel → Figma MCP → Connect | ⬜ Pending |
| Design tokens are published as Figma Styles | All colour, text and effect tokens are in the Figma library | ⬜ Pending |
| Components are published to Figma library | All reusable components are in the shared Figma library | ⬜ Pending |
| Frame names follow the naming convention | See `stitch-to-figma-handoff.md` Section 4 | ⬜ Pending |

---

## 3. Connecting Figma MCP in Kiro

1. Open the Kiro IDE.
2. Open the Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`) and search **MCP**.
3. Select **Open MCP Settings**.
4. Locate or add the Figma MCP server entry in `.kiro/settings/mcp.json`:

```json
{
  "mcpServers": {
    "figma": {
      "command": "uvx",
      "args": ["figma-mcp@latest"],
      "env": {
        "FIGMA_ACCESS_TOKEN": "<your-figma-personal-access-token>"
      },
      "disabled": false
    }
  }
}
```

5. Obtain a Figma Personal Access Token:
   - Figma → Profile → Settings → Security → Personal Access Tokens → Generate.
   - Store only in the `env` block above. Do not commit to source control.
6. After saving `mcp.json`, open the MCP Servers panel and confirm the Figma server shows **Connected**.

---

## 4. Selecting the Correct Figma File and Frames

When instructing Kiro to implement a screen using Figma MCP, provide:

- **Figma file URL**: `https://www.figma.com/design/<FILE_KEY>/<FILE_NAME>`
- **Node ID**: The specific frame node ID (visible in the URL when you select a frame in Figma: `?node-id=<NODE_ID>`)
- **Batch and screen reference**: e.g. "Batch A — App-Auth-1.3-MobileLogin-Default"

Example Kiro prompt for implementation:

```
Implement the Mobile Login screen for the ChitSmart Android app.
Figma file: https://www.figma.com/design/PLACEHOLDER_FILE_KEY/ChitSmart
Frame node: PLACEHOLDER_NODE_ID (App-Auth-1.3-MobileLogin-Default)
Target file: app/src/main/java/com/chitsmart/ui/auth/LoginScreen.kt
Use Jetpack Compose. Extract colours from the Figma design tokens.
Match the layout exactly. Implement all states: Default, Loading, Error.
```

---

## 5. Design Token Extraction

When Figma MCP is connected, extract design tokens before beginning implementation:

### Colours

Ask Kiro: *"Extract all colour styles from the Figma file and generate a Kotlin/XML design token file."*

Expected output — `ui/theme/Color.kt`:

```kotlin
val PrimaryNavy = Color(0xFF073763)
val SecondaryBlue = Color(0xFF1769AA)
val TealAccent = Color(0xFF14B8A6)
val Background = Color(0xFFF7FAFC)
val MainText = Color(0xFF172B4D)
val SecondaryText = Color(0xFF64748B)
val SuccessGreen = Color(0xFF22C55E)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val SurfaceWhite = Color(0xFFFFFFFF)
val BorderDivider = Color(0xFFE2E8F0)
```

### Typography

Ask Kiro: *"Extract all text styles from the Figma file and generate a Kotlin typography token file."*

Expected output — `ui/theme/Typography.kt` with Material 3 `TextStyle` definitions for all roles.

### Spacing

Spacing follows a 4 dp grid. Generate a `Dimens.kt` file:

```kotlin
object Dimens {
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space40 = 40.dp
    val Space48 = 48.dp
}
```

---

## 6. Screen-to-Code Mapping

For each Figma frame, record the mapping between the design frame and its implementation:

| Figma Frame | Implementation File | Component Name | Status |
|---|---|---|---|
| App-Auth-1.3-MobileLogin-Default | `ui/auth/LoginScreen.kt` | `LoginScreen` | ⬜ Pending |
| App-Auth-1.4-OTPVerification-Default | `ui/auth/OTPScreen.kt` | `OTPScreen` | ⬜ Pending |
| App-Auth-1.5-Registration-Default | `ui/auth/RegistrationScreen.kt` | `RegistrationScreen` | ⬜ Pending |
| App-Home-2.1-Dashboard-Default | `ui/home/HomeScreen.kt` | `HomeScreen` | ⬜ Pending |
| App-MyChits-3.1-BrowseGroups-Default | `ui/chits/BrowseGroupsScreen.kt` | `BrowseGroupsScreen` | ⬜ Pending |
| App-MyChits-3.4-ActiveMemberships-Default | `ui/chits/ActiveMembershipsScreen.kt` | `ActiveMembershipsScreen` | ⬜ Pending |
| App-MyChits-3.6-PaymentSummary-Default | `ui/payment/PaymentSummaryScreen.kt` | `PaymentSummaryScreen` | ⬜ Pending |
| App-Payments-4.1-PaymentsHome-Default | `ui/payment/PaymentsHomeScreen.kt` | `PaymentsHomeScreen` | ⬜ Pending |
| App-Payments-4.2-PayoutHistory-Default | `ui/payment/PayoutHistoryScreen.kt` | `PayoutHistoryScreen` | ⬜ Pending |
| App-Assistant-5.1-AssistantHome-Default | `ui/assistant/AssistantHomeScreen.kt` | `AssistantHomeScreen` | ⬜ Pending |
| App-Assistant-5.2-AIChat-Default | `ui/assistant/AIChatScreen.kt` | `AIChatScreen` | ⬜ Pending |
| Admin-Dashboard-A.1-Executive-Default | `admin/src/pages/Dashboard.tsx` | `DashboardPage` | ⬜ Pending |
| Admin-Dashboard-A.2-CollectionAnalytics-Default | `admin/src/pages/CollectionAnalytics.tsx` | `CollectionAnalyticsPage` | ⬜ Pending |
| Admin-Members-B.1-MemberSearch-Default | `admin/src/pages/MemberSearch.tsx` | `MemberSearchPage` | ⬜ Pending |
| Admin-Members-B.2-MemberDetails-Default | `admin/src/pages/MemberDetails.tsx` | `MemberDetailsPage` | ⬜ Pending |
| Admin-ChitGroups-C.1-GroupsList-Default | `admin/src/pages/ChitGroupsList.tsx` | `ChitGroupsListPage` | ⬜ Pending |
| Admin-Installments-D.1-Schedule-Default | `admin/src/pages/InstallmentSchedule.tsx` | `InstallmentSchedulePage` | ⬜ Pending |
| Admin-Payments-E.1-Transactions-Default | `admin/src/pages/PaymentTransactions.tsx` | `PaymentTransactionsPage` | ⬜ Pending |
| Admin-Payments-E.3-Reconciliation-Default | `admin/src/pages/Reconciliation.tsx` | `ReconciliationPage` | ⬜ Pending |
| Admin-AITools-I.1-CollectionSummary-Default | `admin/src/pages/AICollectionSummary.tsx` | `AICollectionSummaryPage` | ⬜ Pending |
| Admin-AITools-I.2-NLReporting-Default | `admin/src/pages/NLReporting.tsx` | `NLReportingPage` | ⬜ Pending |

**Excluded (deferred — do not implement in this release):**

| Deferred Figma Frame | Reason |
|---|---|
| App-Auth-1.6-KYCUpload / 1.7-KYCStatus | KYC deferred |
| App-Auctions-4.x (all auction screens) | Bidding/auctions deferred |
| Admin-KYC-C.1/C.2 | KYC deferred |
| Admin-Auctions-F.1–F.4 | Bidding/auctions deferred |
| Admin-Payouts-I.1/I.2 (approval workflow) | Deferred with auctions |
| Admin-AITools-K.2 AI Document Review | Deferred with KYC |

Update this table as screens are implemented.

---

## 7. Visual Comparison and Acceptance Criteria

After implementing each screen, perform a visual comparison:

### Step 1 — Side-by-side comparison

1. Run the app in the Android Emulator (Pixel 6 — 1080 × 2400, API 34) or a device at 360 dp width.
2. Navigate to the implemented screen.
3. Take a screenshot.
4. Place the screenshot next to the Figma frame in a comparison tool (Figma, Kaleidoscope, or any diff viewer).

### Step 2 — Acceptance criteria checklist

A screen passes visual QA when all of the following are true:

| Criterion | Tolerance |
|---|---|
| Background colour matches design token | Exact hex match |
| Primary action button colour and shape correct | Exact |
| Typography: font family, weight, size | ≤ 1 sp deviation |
| Spacing between elements | ≤ 4 dp deviation |
| Corner radii | ≤ 2 dp deviation |
| Icon sizes and colours | Exact |
| All states implemented (Default, Loading, Error, Empty, Success) | All states present |
| Touch targets ≥ 48 × 48 dp | No exceptions |
| Bottom navigation visible and correct | Exact tab labels and icons |
| No placeholder text (e.g. "Lorem ipsum") in production state | Zero placeholders |

### Step 3 — Annotate issues

For any criterion that fails, create a Figma comment on the source frame:

```
[IMPL-ISSUE] Screen: LoginScreen.kt
Issue: Button corner radius 4dp (should be 8dp)
File: ui/auth/LoginScreen.kt line 87
Status: Open
```

Resolve all IMPL-ISSUE comments before the screen is marked "Complete" in the traceability CSV.

---

## 8. What To Do If Figma MCP Is Unavailable

If the Figma MCP connection cannot be established:

1. **Use the Figma design specs manually:**
   - Open the Figma frame in the browser.
   - Use the **Inspect** panel (right sidebar) to read exact values for colours, fonts, spacing and dimensions.
   - Implement using those manually read values.

2. **Use the design token files:**
   - All tokens are defined in `docs/design/google-stitch-master-prompt.md` (Section: Design System).
   - Use these as ground truth if Figma values cannot be read.

3. **Use reference screenshots:**
   - Export the Figma frame as PNG at 2× (375 pt × 2 = 750 px wide for mobile).
   - Place in `docs/design/references/` directory.
   - Reference the screenshot in the implementation PR.

4. **Document the fallback:**
   - Add a comment in the implementation file:
   ```kotlin
   // NOTE: Implemented from Figma reference screenshot (MCP unavailable).
   // Figma frame: App-Auth-1.3-MobileLogin-Default
   // Verify against Figma when MCP is restored.
   ```

5. **Retry MCP connection:**
   - Check the MCP server status in Kiro's MCP panel.
   - Verify the Figma access token has not expired.
   - Check network connectivity.
   - Restart the Figma MCP server from the panel.

---

## 9. Figma File Registry

Once the Figma file is created, record its details here and in `docs/design/figma-file-registry.md`:

| Field | Value |
|---|---|
| Figma File Name | ChitSmart — GenAI Chit Fund Platform |
| Figma File URL | PLACEHOLDER — update after file creation |
| Figma File Key | PLACEHOLDER |
| Owner | PLACEHOLDER |
| Created | PLACEHOLDER |
| Last Updated | PLACEHOLDER |
| MCP Connection Status | Not connected |
