# Kiro Prompt — KAN-329: Admin Dashboard Error Tracking (Sentry)

**Ticket:** KAN-329  
**Epic:** KAN-317 — Observability  
**Priority:** Medium  
**Skill:** Use the `admin-dashboard-component` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-011  
**Prerequisites:** KAN-326 (correlation ID in admin API client)  
**Files:**
- `admin/src/main.tsx` (initialise Sentry)
- `admin/src/api/client.ts` (attach correlation ID to Sentry scope)
- `admin/src/components/ErrorBoundary.tsx` (new React error boundary)
- `admin/.env.example` (add `VITE_SENTRY_DSN`, `VITE_SENTRY_ENVIRONMENT`)

---

## Objective

Integrate the Sentry browser SDK into the admin dashboard. Capture unhandled
JavaScript exceptions, unhandled promise rejections, React error boundary errors,
and 4xx/5xx API responses with their correlation IDs. Must be completely silent
(no errors, no warnings) when `VITE_SENTRY_DSN` is not set — local dev must not
require a Sentry account.

## Repository inspection

Before writing any code:
1. Read `admin/package.json` — check if `@sentry/react` is already present.
2. Read `admin/src/main.tsx` — understand where to add Sentry initialisation.
3. Read `admin/src/App.tsx` — understand the component tree to wrap with error boundary.
4. Read `admin/src/api/client.ts` — confirm the API error interceptor from KAN-326
   is present. The Sentry integration extends it.
5. Read `admin/.env.example` — check current env var list.

## Installation

```bash
cd admin
npm install @sentry/react@8 --save-exact
```

Add the exact version to `admin/package.json`.

## `admin/src/main.tsx` — Sentry initialisation

```typescript
import * as Sentry from "@sentry/react";

const dsn = import.meta.env.VITE_SENTRY_DSN as string | undefined;

if (dsn) {
  Sentry.init({
    dsn,
    environment: (import.meta.env.VITE_SENTRY_ENVIRONMENT as string) ?? "development",
    integrations: [
      Sentry.browserTracingIntegration(),
      Sentry.replayIntegration({ maskAllText: true, blockAllMedia: true }),
    ],
    tracesSampleRate: 0.1,      // 10% of transactions — adjust per environment
    replaysSessionSampleRate: 0.01,
    replaysOnErrorSampleRate: 1.0,
    // PII scrubbing — do not send user input content
    beforeSend(event) {
      // Strip any request body data that may have been attached
      if (event.request) {
        delete event.request.data;
        delete event.request.cookies;
      }
      return event;
    },
  });
}

// Expose on window for use in the API client interceptor (KAN-326)
(window as any).__chitsmart_sentry = dsn ? Sentry : undefined;
```

## `admin/src/components/ErrorBoundary.tsx`

```tsx
import React from "react";
import * as Sentry from "@sentry/react";

interface Props {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

interface State {
  hasError: boolean;
  eventId?: string;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    const eventId = (window as any).__chitsmart_sentry
      ? Sentry.captureException(error, { extra: { componentStack: info.componentStack } })
      : undefined;
    this.setState({ eventId });
    console.error("[ChitSmart] React error boundary caught:", error);
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback ?? (
        <div className="flex flex-col items-center justify-center h-screen gap-4">
          <h2 className="text-xl font-semibold text-gray-800">Something went wrong</h2>
          <p className="text-gray-500">Please refresh the page or contact support.</p>
          {this.state.eventId && (
            <p className="text-xs text-gray-400">Error ID: {this.state.eventId}</p>
          )}
        </div>
      );
    }
    return this.props.children;
  }
}
```

Wrap the root app in `admin/src/App.tsx`:

```tsx
import { ErrorBoundary } from "@/components/ErrorBoundary";

export default function App() {
  return (
    <ErrorBoundary>
      {/* existing app tree */}
    </ErrorBoundary>
  );
}
```

## `admin/.env.example` additions

```env
# Sentry error tracking (optional — leave blank to disable)
VITE_SENTRY_DSN=
VITE_SENTRY_ENVIRONMENT=development
```

## Acceptance criteria

- [ ] When `VITE_SENTRY_DSN` is not set, `npm run dev` produces no errors or console
  warnings related to Sentry
- [ ] When `VITE_SENTRY_DSN` is set to a valid DSN, Sentry initialises silently
- [ ] An uncaught JavaScript exception triggers `Sentry.captureException`
- [ ] A React component crash is caught by `ErrorBoundary` and reported to Sentry
- [ ] A 500 API response logs the `correlation_id` to console (from KAN-326 interceptor)
  and sets it as a Sentry tag
- [ ] `event.request.data` and `event.request.cookies` are stripped before transmission
  (verified in `beforeSend` callback test)
- [ ] `npm run build` succeeds with Sentry integration present
- [ ] `npm run lint` passes on all new/changed files
- [ ] Unit tests: ErrorBoundary renders fallback on error, Sentry not called when DSN unset

## Definition of done

- `npm test` passes including `ErrorBoundary.test.tsx`
- `npm run build` succeeds
- `npm run lint` and `npm run type-check` pass
- `admin/.env.example` updated with Sentry vars
