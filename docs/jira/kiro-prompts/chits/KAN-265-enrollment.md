# Kiro Prompt — KAN-265: Membership Enrollment Flow

**Ticket:** KAN-265  
**Epic:** KAN-239 — Chit Group Management (Customer)  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Figma:** `App-MyChits-3.3-Enrollment-Default`  
**File:** `app/src/main/java/com/chitsmart/ui/chits/EnrollmentScreen.kt`

---

## Task

Implement the multi-step membership enrollment flow. User reaches this screen from Group Details → Enroll Now.

## Steps

### Step 1 — Enrollment Summary
- Group name, chit value, monthly installment, total commitment (installment × months)
- "Continue" CTA → Step 2

### Step 2 — Nominee Details
- Form: Nominee Full Name (required), Relationship (dropdown: Spouse/Parent/Child/Sibling/Other), Mobile Number (required)
- "Continue" CTA → Step 3

### Step 3 — Bank Mandate
- Dropdown: select saved bank account OR "Add Bank Account" → navigate to bank account setup
- Auto-debit authorization toggle with explanation text
- "Continue" CTA → Step 4

### Step 4 — E-sign Terms
- Scrollable group terms document (from API)
- "I have read and agree to the terms" checkbox (enabled only after scrolling to bottom)
- "Confirm Enrollment" CTA (PrimaryNavy, full-width)
- Loading state during API call

### Step 5 — Success Screen
- Confetti animation (Lottie)
- "Enrollment Successful!" headline
- Membership ID displayed (large, teal)
- "View My Chit" primary CTA → `AppRoutes.MY_CHITS`
- "Go to Home" secondary CTA → `AppRoutes.HOME`

## Architecture
- Use single `EnrollmentViewModel` with step-based state machine
- Progress indicator at top showing current step (1–4)
- Back navigation moves to previous step (not back to Group Details until Step 1)

## API
`POST /api/v1/chit-groups/{id}/enroll` — body: nominee details, bank account ID, terms accepted

## Acceptance criteria
- [ ] Step indicator shows correct current step
- [ ] Back navigation between steps works correctly
- [ ] Terms "agree" checkbox only enabled after scrolling to bottom
- [ ] Success screen shows membership ID from API response
- [ ] Confetti animation plays on success
- [ ] Error handling: shows inline error if enrollment fails (e.g. group is full)
