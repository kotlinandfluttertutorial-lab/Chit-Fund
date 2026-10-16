# ChitSmart — End-to-End Test Report

**Date:** 2026-10-16

---

## Summary

| Module | Build/Compile | Lint | Tests Passed | Tests Failed | Verdict |
|---|---|---|---|---|---|
| Android | ✅ PASS | ❌ FAIL (2 errors, 61 warnings) | 0 (skipped) | 0 | **FAIL** |
| Admin Dashboard | ✅ PASS | ✅ PASS | 7 | 0 | **PASS** |
| Python Backend | ✅ PASS | ⚠️ SKIPPED | 5 | 0 | **PARTIAL** |

---

## Overall Verdict

**PARTIAL** — Admin Dashboard and Python Backend tests pass cleanly, but the Android module has 2 lint errors that aborted its build and no unit tests exist yet; lint and type-check are also uninstalled in the Python backend environment.

---

## Android

### Compile

Kotlin compilation succeeded with all 17 tasks up-to-date.

```
=== COMPILE (:app:compileDebugKotlin) ===
BUILD SUCCESSFUL in 1m 37s
17 actionable tasks: 17 up-to-date
```

### Lint

Lint **FAILED** — 2 errors and 61 warnings. Build was aborted on errors.

```
=== LINT (:app:lintDebug) ===
BUILD FAILED in 2m 23s
Lint found 2 errors, 61 warnings.

ERROR 1: app/src/main/AndroidManifest.xml:45 [AppLinkUrlError]
  <intent-filter android:autoVerify="true"> is missing required deep-link elements.

ERROR 2 (implied): app/src/main/AndroidManifest.xml:38 [LockedOrientationActivity / DiscouragedApi]
  android:screenOrientation="portrait" should be unspecified or fullSensor.

WARNINGS SUMMARY (61 total):
- [ObsoleteLintCustomCheck] x3 — androidx.navigation lint checks compiled against Lint API 14, running API 16; navigation checks skipped.
- [AndroidGradlePluginVersion] x3 — AGP 8.7.2 in use; 9.4.1 available (also 8.7.3).
- [GradleDependency] x33 — multiple dependencies have newer stable versions available:
    core-ktx 1.15.0 -> 1.19.1, lifecycle* 2.8.7 -> 2.11.0, activity-compose 1.9.3 -> 1.13.0,
    compose-bom 2024.11.00 -> 2026.09.00, navigation-compose 2.8.4 -> 2.10.2,
    hilt-navigation-compose 1.2.0 -> 1.4.0, datastore-preferences 1.1.1 -> 1.2.1,
    security-crypto 1.1.0-alpha06 -> 1.1.0, room 2.6.1 -> 2.8.5,
    junit-ext 1.2.1 -> 1.3.0, espresso 3.6.1 -> 3.7.0.
- [DefaultLocale] x4 — ReceiptScreen.kt lines 195, 211, 213, 215: String.format without explicit Locale.
- [LockedOrientationActivity] x1 — AndroidManifest.xml:38 portrait lock.
- [DiscouragedApi] x1 — AndroidManifest.xml:38 orientation restriction.

Full lint report: app/build/reports/lint-results-debug.html
```

**Lint Failures:**

| File | Line | Error |
|---|---|---|
| `app/src/main/AndroidManifest.xml` | 45 | `[AppLinkUrlError]` Missing required elements/attributes for Android App Links — `<intent-filter android:autoVerify="true">` is incomplete. |
| `app/src/main/AndroidManifest.xml` | 38 | `[LockedOrientationActivity]` / `[DiscouragedApi]` Activity locked to portrait orientation; should use unspecified or fullSensor for multi-window/Chrome OS compatibility. |

### Unit Tests

```
=== UNIT TESTS ===
SKIPPED — no .kt test files found under app/src/test/ (only empty package directories).
```

No test files exist under `app/src/test/`. Task was skipped.

---

## Admin Dashboard

### Type Check

TypeScript type check passed with 0 errors.

```
--- type-check ---
> tsc --noEmit
Exit Code: 0
```

### Lint

ESLint passed with 0 errors and 0 warnings.

```
--- lint ---
> eslint src
Exit Code: 0
```

### Tests

All 7 tests across 2 test files passed.

```
--- test ---
> vitest run
 ✓ src/__tests__/types.test.ts (2 tests) 10ms
 ✓ src/__tests__/apiClient.test.ts (5 tests) 12ms
 Test Files  2 passed (2)
      Tests  7 passed (7)
   Duration  4.41s
Exit Code: 0
```

No failures.

### Build

Vite build succeeded. One chunk-size warning (not an error): the main JS bundle is ~922 KB (gzip ~244 KB), exceeding the 500 KB recommendation.

```
--- build ---
> tsc && vite build
✓ 2265 modules transformed.
dist/index.html                   0.74 kB │ gzip:   0.40 kB
dist/assets/index-DD0HdVm9.css   29.25 kB │ gzip:   6.33 kB
dist/assets/index-CEKN6x-Q.js   922.41 kB │ gzip: 243.53 kB
(!) Some chunks are larger than 500 kB after minification. Consider using dynamic import() to code-split.
✓ built in 56.27s
Exit Code: 0
```

No failures.

---

## Python Backend

### Lint

Skipped — `ruff` is not installed in the venv or globally.

```
LINT: ruff not installed in venv or globally — skipped.
```

### Type Check

Skipped — `mypy` is not installed in the venv or globally.

```
TYPE CHECK: mypy not installed in venv or globally — skipped.
```

### Tests

All 5 pytest tests passed. Two non-fatal warnings were emitted (neither is a test failure).

```
============================= test session starts =============================
platform win32 -- Python 3.12.10, pytest-8.3.4, pluggy-1.7.0
rootdir: J:\Android\AndroidStudioProjects\Kiro\Chit-Fund\backend
configfile: pyproject.toml
plugins: anyio-4.15.1, Faker-40.43.0, asyncio-0.24.0
asyncio: mode=Mode.AUTO
collected 5 items
tests/test_health.py::test_health_endpoint_returns_200 PASSED            [ 20%]
tests/test_health.py::test_health_endpoint_returns_json PASSED           [ 40%]
tests/test_health.py::test_health_endpoint_includes_version PASSED       [ 60%]
tests/test_health.py::test_health_content_type_is_json PASSED            [ 80%]
tests/test_health.py::test_app_is_fastapi_instance PASSED                [100%]
======================== 5 passed, 2 warnings in 3.25s ========================
WARNINGS: asyncio_default_fixture_loop_scope unset (pytest-asyncio); PydanticDeprecatedSince20 class-based config (starlette testclient dep). Neither warning is a test failure.
LINT: ruff not installed in venv or globally — skipped.
TYPE CHECK: mypy not installed in venv or globally — skipped.
```

No failures.

---

## Action Items

### Android

1. **[BLOCKER]** Fix `AppLinkUrlError` in `app/src/main/AndroidManifest.xml` line 45 — the `<intent-filter android:autoVerify="true">` block is missing required deep-link `<data>` elements. Either add valid App Link data attributes or remove the `autoVerify` flag if App Links are not needed for this release.
2. **[BLOCKER]** Fix `LockedOrientationActivity` / `DiscouragedApi` in `app/src/main/AndroidManifest.xml` line 38 — change `android:screenOrientation="portrait"` to `"unspecified"` or `"fullSensor"` to satisfy lint and enable multi-window / Chrome OS compatibility.
3. **[WARNING]** `ReceiptScreen.kt` lines 195, 211, 213, 215 — `String.format()` calls use the default locale; pass an explicit `Locale` (e.g. `Locale.getDefault()` or `Locale.US`) to fix `[DefaultLocale]` warnings.
4. **[WARNING]** Android Gradle Plugin 8.7.2 is in use; 8.7.3 / 9.4.1 are available. Consider upgrading to the latest 8.x patch release.
5. **[WARNING]** 33 Gradle dependencies have newer stable versions available (compose-bom, room, lifecycle, navigation-compose, etc.). Schedule a dependency update pass.
6. **[INFO]** No unit test files exist under `app/src/test/`. Add at minimum a placeholder test so the test task is exercised in CI.

### Admin Dashboard

7. **[WARNING]** Main JS bundle is ~922 KB (gzip ~244 KB), exceeding Vite's 500 KB recommendation. Consider code-splitting with dynamic `import()` to reduce initial load size.

### Python Backend

8. **[WARNING]** `ruff` is not installed in the venv — lint step is skipped on every run. Install with: `.venv\Scripts\pip.exe install ruff mypy` (both are already configured in `pyproject.toml`).
9. **[WARNING]** `mypy` is not installed in the venv — type-check step is skipped on every run. Install alongside `ruff` (see item 8).
10. **[WARNING]** `asyncio_default_fixture_loop_scope` is unset in pytest-asyncio configuration. Add `asyncio_mode = "auto"` and set `asyncio_default_fixture_loop_scope = "function"` in `pyproject.toml` to suppress the warning.
11. **[WARNING]** `PydanticDeprecatedSince20` warning from Starlette's test client dependency. No action required now; will resolve when the upstream dependency is updated.
