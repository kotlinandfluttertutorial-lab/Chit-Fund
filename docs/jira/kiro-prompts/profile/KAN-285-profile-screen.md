# Kiro Prompt — KAN-285: Profile Screen

**Ticket:** KAN-285  
**Epic:** KAN-236 — Customer Profile & Settings  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Profile-6.1-ProfileSettings-Default` — node `1:2753`  
**File:** `app/src/main/java/com/chitsmart/ui/profile/ProfileScreen.kt`

---

## Task

Implement the Profile tab root screen.

## Layout

### Header card
- Avatar circle: photo (Coil) or initials from name (first letter, teal background)
- Member name — `headlineMedium`
- Member ID — `bodySmall`, `SecondaryText`
- Account status badge: Active (green) | Inactive (gray) | Suspended (red)
- **NO KYC status badge**

### Menu sections (`LazyColumn` of `ProfileMenuItem` composables)
| Icon | Label | Action |
|---|---|---|
| Person | Personal Info | `AppRoutes.PERSONAL_INFO` |
| AccountBalance | Bank Accounts | `AppRoutes.BANK_ACCOUNTS` |
| People | Nominees | `AppRoutes.NOMINEES` |
| Security | Security Settings | `AppRoutes.SECURITY` |
| Language | Language & Preferences | `AppRoutes.LANGUAGE_PREFS` |
| Help | Help Centre | `AppRoutes.HELP_CENTER` |
| Info | About | show version + licenses dialog |
| Logout | Log Out | confirmation dialog → clear tokens → `AppRoutes.LOGIN` |

**NO "Documents" or "KYC" menu item.**

### Logout
- Confirmation `AlertDialog`: "Are you sure you want to log out?"
- On confirm: clear EncryptedSharedPreferences tokens, navigate to Login (clear back stack)

## Acceptance criteria
- [ ] Avatar shows initials if no photo
- [ ] Account status badge visible (not KYC status)
- [ ] All 8 menu items present, no KYC or Documents item
- [ ] Logout shows confirmation dialog
- [ ] Logout clears tokens and returns to Login with back stack cleared
- [ ] Matches Figma frame `App-Profile-6.1-ProfileSettings-Default`
