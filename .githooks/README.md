# Git Hooks

This directory contains Git hooks for the ChitSmart repository.

## Enable the pre-push hook

Run this once per developer machine (from the repo root):

```sh
git config core.hooksPath .githooks
```

## What the pre-push hook checks

| Layer | Checks run | Condition |
|-------|-----------|----------|
| `admin/` | `npm run lint`, `npm run type-check`, `npm run test`, `npm run build` | When any file under `admin/` has changed |
| `backend/` | `ruff check .`, then `pytest` if `DATABASE_URL` is set | When any file under `backend/` has changed |
| `app/` | Warning only — run `./gradlew lint testDebugUnitTest` manually | When any Android file has changed |

## Bypass (emergency only)

```sh
git push --no-verify
```

Use sparingly. CI will still catch failures.

## Disable the hook

```sh
git config --unset core.hooksPath
```
