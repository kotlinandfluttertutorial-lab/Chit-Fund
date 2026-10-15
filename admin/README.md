# ChitSmart Admin Dashboard

React 18 · TypeScript · Vite · TanStack Query · Tailwind CSS

## Setup

```bash
# Install dependencies
npm install

# Start development server (requires backend running on :8000)
npm run dev
# → http://localhost:5173
```

## Environment

Create `.env.local` in this directory:

```
VITE_API_BASE_URL=http://localhost:8000/api/v1
```

## Scripts

| Command | Description |
|---|---|
| `npm run dev` | Start dev server on :5173 |
| `npm run build` | Production build to `dist/` |
| `npm run test` | Run tests (single pass) |
| `npm run lint` | ESLint check |
| `npm run type-check` | TypeScript check without emit |

## Sidebar navigation (current release)

Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings

**Not present in this release:** KYC, Auctions.  
See `docs/scope-decisions.md`.
