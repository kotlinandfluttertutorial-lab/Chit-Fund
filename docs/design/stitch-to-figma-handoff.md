# Stitch-to-Figma Handoff Guide

## Status: Pending — Google Stitch generation has not yet been completed.

**Scope update (15/10/2026):** Three scope decisions have been applied. Gold Saver 50K is removed. KYC screens are deferred. Bidding/auction screens are deferred. The Auctions bottom nav tab is replaced by a Payments tab. See `docs/scope-decisions.md` for full rationale. The Figma page structure and batch descriptions below reflect the updated scope.

This document describes the process for transferring Google Stitch designs into Figma once the Stitch generation step is done by the user. No frames have been transferred yet.

---

## 1. Prerequisites

Before starting the transfer:

- Google Stitch generation is complete for the target batch (A, B, C, or D).
- You have edit access to the destination Figma file.
- The Figma file has been created with the correct page structure (see Section 4).
- You have the Google Stitch export URL or downloaded asset package.

---

## 2. Transferring Designs from Google Stitch to Figma

There are two working methods in 2026, depending on which Stitch agent you used to generate. Try Method A first; fall back to Method B if the button is unavailable.

---

### Method A — Native "Copy to Figma" (Rapid Mode / Fast / Gemini 2.5 Flash)

This is the fastest path. It works when you generated using **Rapid Mode**, **Fast**, or **Gemini 2.5 Flash**. It does NOT work with Gemini 3 Pro (use Method B in that case).

1. In Google Stitch, open the generated screen.
2. Check the mode toggle (top-right of the Stitch canvas) — confirm it says **Rapid** or **Fast**.
3. Look for the **"Copy to Figma"** button in the top toolbar. If it is active (not greyed out), click it.
4. Stitch copies a specialised JSON payload to your clipboard that Figma recognises as native layers.
5. Switch to Figma, navigate to the correct page (e.g. `📱 App — Batch A`), and press **Ctrl+V** (Windows) or **Cmd+V** (Mac).
6. Figma pastes the frame with Auto Layout structures intact.
7. Rename the pasted frame to match the naming convention (see Section 4).
8. Repeat for each screen in the batch.

**If "Copy to Figma" is greyed out** while in Rapid Mode:
- Click the **code icon** (`< >`) in the right panel to load the code view.
- Once the code is loaded, the "Copy to Figma" button in the right pane becomes active.
- Click it, then paste into Figma as above.

---

### Method B — Download Code + html.to.design plugin (works with all agents including Gemini 3 Pro)

Use this method if you generated with **Gemini 3 Pro** (which gives higher quality output but disables the native Figma button), or if Method A produces unsatisfactory layer structure.

**Step 1 — Download the code from Stitch**

1. In Google Stitch, open the generated screen.
2. Click the **More options menu** (three dots or hamburger icon, top-left of your Stitch dashboard).
3. Select **Download** — this downloads a `.zip` file containing the HTML and CSS for that screen.
4. Save the zip with a descriptive name: `batch-a-login.zip`, `batch-a-home-dashboard.zip`, etc.

**Step 2 — Install html.to.design in Figma**

1. In Figma, open the **Plugins** menu → **Browse plugins**.
2. Search for **html.to.design** (by DesignCode).
3. Click **Install**.
4. This only needs to be done once.

**Step 3 — Import into Figma**

1. In Figma, navigate to the correct page (e.g. `📱 App — Batch A`).
2. Open **Plugins → html.to.design**.
3. Go to the **File** tab inside the plugin.
4. Drag and drop the `.zip` file downloaded from Stitch, or click to upload it.
5. Adjust settings if prompted (scale, units).
6. Click **Import**.
7. The screen appears as an editable Figma frame with a natural layer structure.
8. Rename the frame to match the naming convention (see Section 4).

**Why Method B often produces better results:**
- Preserves higher fidelity than the native clipboard transfer.
- Generates cleaner, more natural Figma layer structure.
- Works regardless of which Stitch agent was used.

---

### Method C — Screenshot reference (last resort)

Use only if both Method A and B fail (e.g. tool outage or access issue).

1. Take full-resolution browser screenshots of every Stitch screen.
2. In Figma, create a `_Stitch Reference` page.
3. Paste screenshots there, lock them (right-click → Lock), set opacity to 60%.
4. Manually recreate each screen as an editable Figma frame on the working page, tracing the screenshot as a guide and applying design tokens from the design system.
5. Mark the frame annotation: `Transfer Method: Screenshot Reference`.

---

## 3. Verifying Imported Frames

After every import or recreation step, verify the following:

| Check | How to Verify |
|---|---|
| All batch screens are present | Cross-reference against the screen inventory in `google-stitch-master-prompt.md` |
| Frame names follow naming convention (Section 4) | Review Figma layers panel |
| Colours match design tokens | Use Figma plugin "Design Tokens" or inspect fill values |
| Typography matches token values | Inspect text layers — font, size, weight, line-height |
| Spacing is consistent with 4 dp grid | Enable Figma layout grid on frames |
| Touch targets ≥ 48 × 48 dp (mobile) | Measure interactive elements with the ruler |
| Components are linked to Figma component library | Check instances panel — no detached overrides |
| All states present: Default, Loading, Error, Empty, Success | Review each screen for all required states |

---

## 4. Required Figma Pages and Naming Conventions

Create the following pages in the destination Figma file before import:

| Page Name | Contents |
|---|---|
| `_Stitch Reference` | Locked reference screenshots/exports from Google Stitch |
| `🎨 Design System` | Colour styles, text styles, effect styles, spacing tokens |
| `🧩 Components — Mobile` | Reusable Android app components |
| `🧩 Components — Web` | Reusable admin dashboard components |
| `📱 App — Batch A` | Login, Home, My Chits, Installment Payment, Payments Home, AI Assistant |
| `📱 App — Batch B` | Remaining customer app screens (no KYC, no auction screens) |
| `💻 Admin — Batch C` | Admin dashboard core screens (no KYC, no auction screens) |
| `💻 Admin — Batch D` | Remaining admin screens + shared states (no KYC, no auction screens) |
| `🔄 Prototypes` | Interactive prototype connections |
| `📋 Annotations` | Dev handoff annotations and redlines |

**Frame naming convention:**

```
[App]-[Group]-[Screen Number]-[Screen Name]-[State]
```

Examples:
- `App-Auth-1.3-MobileLogin-Default`
- `App-Auth-1.4-OTPVerification-Error`
- `App-Home-2.1-Dashboard-Default`
- `Admin-Members-B.2-MemberDetails-Default`
- `Admin-KYC-C.2-ReviewWorkspace-ApproveState`

**States suffix list:** `Default` | `Loading` | `Empty` | `Success` | `Error` | `Offline` | `Hover` | `Pressed` | `Disabled`

---

## 5. Recording the Source Stitch Design

For each Figma frame, add a Figma annotation (Cmd/Ctrl + Shift + A or use the Annotations plugin) with:

```
Stitch Source: [Batch A/B/C/D] — [Stitch Screen Name]
Stitch Generation Date: [DD/MM/YYYY]
Transferred By: [Name]
Transfer Method: [Direct Export / PNG Recreation / Screenshot Reference]
Notes: [Any deviations from the Stitch source]
```

Alternatively, maintain a frame-level description in the Figma frame's description field using this format.

---

## 6. Resolving Missing Assets, Fonts and Components

### Missing Fonts

If Inter is not available in Figma (requires Figma Desktop or font installed):

1. Install Inter from Google Fonts: https://fonts.google.com/specimen/Inter
2. Re-open Figma Desktop to detect the font.
3. If using Figma Web, upload the font files via **Figma → Settings → Fonts**.

### Missing Icons

The design uses Material Symbols. To install:

1. Use the Figma plugin **Material Symbols** (by Google).
2. Search by icon name and insert into frames.
3. Set colour to the appropriate design token.

### Missing Components

If a component referenced in Stitch is not in the Figma library:

1. Create the component manually in `🧩 Components — Mobile` or `🧩 Components — Web`.
2. Name it following the convention: `ComponentName/Variant/State`.
3. Add to the shared library (Figma Assets panel → Publish).
4. Replace placeholder with the library component.

### Missing Responsive States

For screens where Stitch only generated one breakpoint:

1. Duplicate the frame.
2. Rename with the breakpoint suffix: `_360dp` / `_430dp` / `_1280px` / `_768px`.
3. Adjust layout constraints and content manually.
4. Document in the annotation as a manually created responsive variation.

---

## 7. Handoff Completion Checklist (per Batch)

Complete these steps before marking a batch as "Figma Ready":

- [ ] All screens from the batch are present in Figma
- [ ] All frames follow the naming convention
- [ ] Stitch source annotations added to every frame
- [ ] All states present for every screen (Default, Loading, Error, Empty at minimum)
- [ ] Colours linked to Figma colour styles (no raw hex values on elements)
- [ ] Typography linked to Figma text styles
- [ ] Components are library instances (not flat copies)
- [ ] Prototype connections added for primary user flows
- [ ] Dev annotations added for non-obvious spacing and behaviour
- [ ] Design traceability CSV updated (`docs/design/design-traceability.csv`)
- [ ] Figma file link recorded in the project README

---

## 8. Current Status

| Batch | Stitch Generation | Figma Transfer | Verification | Status |
|---|---|---|---|---|
| Batch A — Core Customer | ⬜ Pending | ⬜ Pending | ⬜ Pending | Not Started |
| Batch B — Full Customer | ⬜ Pending | ⬜ Pending | ⬜ Pending | Not Started |
| Batch C — Admin Core | ⬜ Pending | ⬜ Pending | ⬜ Pending | Not Started |
| Batch D — Admin + Shared | ⬜ Pending | ⬜ Pending | ⬜ Pending | Not Started |

Update this table as batches are completed.
