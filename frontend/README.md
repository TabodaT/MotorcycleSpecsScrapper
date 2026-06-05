# MotoMarket Intelligence — Frontend

React 18 + TypeScript + Vite dashboard for the Motorcycle Market Intelligence backend.

## Development

```bash
npm install
npm run dev       # starts Vite dev server on http://localhost:5173 (proxies /api -> :18080)
```

## Production build

```bash
npm run build     # tsc + vite build -> dist/
npm run preview   # serve dist/ locally for a quick smoke-test
```

The `dist/` folder is served directly by the Spring Boot app at the same origin, so all `/api` calls are relative.
