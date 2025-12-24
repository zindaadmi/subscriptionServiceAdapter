# Subscription Service Frontend

React + TypeScript + Vite + shadcn/ui frontend for the Subscription Service.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Add missing shadcn/ui dependencies:
```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button card input label
```

3. Install tailwindcss-animate:
```bash
npm install tailwindcss-animate
```

4. Start development server:
```bash
npm run dev
```

## Features

- ✅ React + TypeScript
- ✅ Vite for fast builds
- ✅ shadcn/ui components
- ✅ Tailwind CSS
- ✅ React Router for navigation
- ✅ Axios for API calls
- ✅ Authentication context
- ✅ Protected routes with role-based access
- ✅ Admin dashboard
- ✅ Agent dashboard
- ✅ User dashboard
- ✅ Audit logs viewer
- ✅ Login with username/mobile

## Environment Variables

Create `.env` file:
```
VITE_API_URL=http://localhost:8080/api
```

## Project Structure

```
src/
  components/
    ui/          # shadcn/ui components
  contexts/      # React contexts (Auth)
  pages/         # Page components
    admin/       # Admin pages
    agent/       # Agent pages
    user/        # User pages
  services/      # API service functions
  lib/           # Utilities
```

