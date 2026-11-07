# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**NautiFlow** is a platform for boat operators ("lancheros") in the Tigre Delta region to register and offer their services. The repo is a monorepo with a React/TypeScript frontend and a Spring Boot backend.

## Repository Structure

```
/
├── front/NautiFlow/    # React 19 + Vite + Tailwind CSS v4 frontend
├── back/               # Spring Boot 4 + Java 21 backend (Maven)
└── docker-compose.yml  # PostgreSQL 16 for local development
```

## Commands

### Frontend (`front/NautiFlow/`)

```bash
npm run dev       # start Vite dev server
npm run build     # TypeScript check + production build
npm run lint      # ESLint
npm run preview   # preview production build
```

### Backend (`back/`)

```bash
./mvnw spring-boot:run     # run the application
./mvnw test                # run all tests
./mvnw -Dtest=ClassName test  # run a single test class
./mvnw package             # build JAR
```

### Database

```bash
docker compose up -d    # start PostgreSQL (required before running the backend)
docker compose down     # stop PostgreSQL
```

The database connection is configured in [back/src/main/resources/application.properties](back/src/main/resources/application.properties): `jdbc:postgresql://localhost:5432/delta_tigre_db` with user `delta_user` / password `delta_password`.

## Architecture

### Frontend

- **Entry point:** [front/NautiFlow/src/App.tsx](front/NautiFlow/src/App.tsx) renders `<LancheroRegistro>`, the top-level registration wizard.
- **Multi-step form:** [LancheroRegistro.tsx](front/NautiFlow/src/components/lanchero/LancheroRegistro.tsx) controls a `paso` state (0–4): intro → step 1 (personal data) → step 2 (vessel) → step 3 (documents) → confirmation. All form state lives here as `DatosLanchero` and is passed down via props.
- **Shared type:** [types.ts](front/NautiFlow/src/components/lanchero/types.ts) defines `DatosLanchero` and `datosIniciales`.
- **Styling:** Tailwind CSS v4 with a full design-token system defined in [src/index.css](front/NautiFlow/src/index.css) using `@theme`. Use the semantic tokens (e.g. `bg-bg-fill-primary`, `text-text-primary`) — do not hardcode colors.

### Backend

- Spring Boot 4 with JPA (Hibernate `validate` mode — schema must exist before startup), Spring Security, and PostgreSQL.
- Lombok is used for boilerplate reduction.
- The main application class is [NautiflowApplication.java](back/src/main/java/com/nautiflow/NautiflowApplication.java).

## Branches

- `main` — stable
- `develop` — integration branch
- `design` — current branch for UI/design work
- Feature branches follow `feature/<name>`


## Reglas al implementar diseños de Figma
- Usa solo clases generadas por los tokens de @theme (bg-primary-500, text-sm, rounded-lg, etc.).
- Prohibido hex, rgb o valores arbitrarios tipo w-[347px] o p-[13px].
- Si el diseño usa un valor que no existe en @theme, avísame; no lo hardcodees ni lo agregues tú.
- Reutiliza componentes de src/components/ui. Si falta uno, pregunta antes de crearlo, todos los componentes del frame en figma estan basadados en shadcn pero como no se usa shadcn, debes crearlos tu mismo solamente con tailwind y react.
- Estructura de pantallas: sigue el patrón de src/components/<pantalla-ejemplo>.
- Props tipados, mismo estilo de nombres que los componentes existentes.