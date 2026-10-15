# Kiro Prompt — KAN-254: Onboarding Carousel

**Ticket:** KAN-254  
**Epic:** KAN-241 — Authentication & Registration  
**Priority:** Medium  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-Auth-1.2-Onboarding-Default` — node `1:1468`  
**File:** `app/src/main/java/com/chitsmart/ui/onboarding/OnboardingScreen.kt`

---

## Task

Implement the 3-slide onboarding carousel shown only on first app launch.

## Slides

| Slide | Illustration | Headline | Body |
|---|---|---|---|
| 1 | Group savings (people in a circle) | "Grow Together" | "Join a chit fund group and build savings with your community" |
| 2 | Calendar + payment tick | "Pay Easily, Track Everything" | "Never miss an installment — we remind you and make payment simple" |
| 3 | AI chat bubble / robot | "AI That Understands You" | "Ask ChitSmart AI anything about your account in any language" |

**Slide 2 MUST show a payments/calendar illustration — NOT an auction gavel or bidding image.**

## Implementation

### `OnboardingScreen.kt`
- `HorizontalPager` (Accompanist or Foundation) — 3 pages
- Each page: illustration `Image` (top 55% height), headline `headlineLarge`, body `bodyMedium`
- Bottom controls row:
  - Page indicator dots: filled circle = current, outlined = other
  - "Skip" `TextButton` (top-right, visible on slides 1 and 2)
  - "Next" filled `Button` on slides 1 and 2
  - "Get Started" filled `Button` on slide 3 (PrimaryNavy fill, full-width)
- On Skip or Get Started:
  - Set `onboarding_completed = true` in DataStore
  - Navigate to `AppRoutes.LOGIN`

### `OnboardingViewModel.kt`
```kotlin
fun completeOnboarding() {
    viewModelScope.launch { userPrefsRepository.setOnboardingCompleted(true) }
}
```

## Acceptance criteria
- [ ] 3 slides swipeable horizontally
- [ ] Skip navigates directly to Login from any slide
- [ ] Get Started on slide 3 navigates to Login
- [ ] Onboarding NOT shown again after completion (DataStore persists)
- [ ] Slide 2 has NO auction or bidding visual
- [ ] Matches Figma frame `App-Auth-1.2-Onboarding-Default`
