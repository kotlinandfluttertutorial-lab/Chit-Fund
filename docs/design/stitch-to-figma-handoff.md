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

### Option A — Stitch → Figma direct export (if supported)

1. In Google Stitch, open the generated design collection.
2. Select all frames for the completed batch.
3. Use **Export → Figma (.fig or copy link)** if Stitch provides this option.
4. In Figma, use **File → Import** or paste the Figma link into your drafts.
5. Move imported frames to the correct Figma page (see Section 4).

### Option B — PNG/SVG export and manual recreation

If Stitch does not provide a direct Figma export:

1. In Google Stitch, export all frames as PNG at 2× resolution (or SVG where available).
2. Import PNGs into Figma as reference layers:
   - Create a page called `_Stitch Reference`.
   - Paste PNGs there, locked, with 50% opacity.
3. Recreate each screen as editable Figma frames on the working pages, using the reference images as a guide.
4. Apply the design tokens from the design system (colours, type styles, effects) while recreating.

### Option C — Screenshot and annotate

If neither export option is available:

1. Take full-resolution screenshots of every Stitch frame.
2. Import screenshots into Figma on the `_Stitch Reference` page.
3. Annotate with Figma comments: screen name, batch, pending implementation notes.
4. Proceed with manual design in Figma, referencing screenshots.

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
