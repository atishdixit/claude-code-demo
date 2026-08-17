# Architecture

`python-web-demo` is two small processes running side by side on your machine: a
Flask REST API holding data in memory, and a React UI that talks to it over HTTP.
There's no database, no auth, and no build/deploy pipeline — it's intentionally
minimal so the CRUD flow and the API/UI boundary stay easy to follow.

## Component diagram

```mermaid
flowchart TB
    Dev["npm run dev<br/>(concurrently)"]

    Browser((Browser))

    subgraph Frontend["React Frontend — Vite dev server :5174"]
        UI["App.jsx<br/>Create / List / Edit / Delete UI + request log"]
        ApiJs["api.js<br/>fetch wrapper"]
        UI --> ApiJs
    end

    subgraph Backend["Flask Backend :5000 (app.py)"]
        Cors["flask-cors"]
        Routes["REST routes<br/>GET / POST / PUT / DELETE /items"]
        Swagger["flasgger<br/>/apidocs + /apispec_1.json"]
        Store[("In-memory dict<br/>items = {}")]
        Cors --> Routes
        Routes --> Store
        Routes --> Swagger
    end

    CurlClients["curl / test-api.bat"]
    SwaggerUser((Developer))

    Browser --> UI
    ApiJs -- "fetch JSON (CORS)" --> Cors
    CurlClients -- "direct HTTP" --> Routes
    SwaggerUser -- "Try it out" --> Swagger

    Dev -.starts.-> Frontend
    Dev -.starts.-> Backend
```

### Pieces

| Component | What it is | Where |
|---|---|---|
| **`npm run dev`** | Root script (`concurrently`) that starts both servers with one command and labels their output `[backend]` / `[frontend]`. | [`package.json`](package.json) |
| **React Frontend** | Vite dev server on port `5174`. `App.jsx` holds all UI state (items, form, edit mode, request log); `api.js` is a thin `fetch` wrapper around the four CRUD calls. | [`frontend/src/`](frontend/src) |
| **Flask Backend** | Single-file API on port `5000`. Routes read/write an in-memory Python `dict` — no database, so data resets whenever the process restarts. | [`app.py`](app.py) |
| **flask-cors** | Lets the frontend (a different origin: `:5174` vs `:5000`) call the API directly from the browser without CORS errors. | `app.py` |
| **flasgger** | Generates an OpenAPI spec from docstrings on each route and serves an interactive Swagger UI. | `app.py`, `/apidocs` |
| **In-memory store** | A plain `dict` keyed by item id, seeded with 3 sample items on startup. This *is* the database for this demo. | `app.py` |
| **curl / `test-api.bat`** | Alternate clients that hit the API directly, bypassing the React UI entirely — useful for scripted testing. | [`test-api.bat`](test-api.bat) |

## Request flow: creating an item

The sequence below shows what happens end-to-end when a user submits the "Create
item" form in the React UI. Every mutation (create/update/delete) in the UI follows
this same pattern: perform the action, then re-fetch the full list so the UI always
reflects the backend's current state.

```mermaid
sequenceDiagram
    participant User
    participant UI as React UI (App.jsx)
    participant ApiJs as api.js (fetch)
    participant Flask as Flask API (/items)
    participant Store as In-memory dict

    User->>UI: Fill form, click "POST /items"
    UI->>ApiJs: createItem({name, description, price})
    ApiJs->>Flask: POST /items (JSON body)
    Flask->>Flask: validate "name" is present
    Flask->>Store: assign next id, save item
    Store-->>Flask: item
    Flask-->>ApiJs: 201 Created + item JSON
    ApiJs-->>UI: created item
    UI->>UI: add entry to request log
    UI->>ApiJs: listItems()
    ApiJs->>Flask: GET /items
    Flask->>Store: read all items
    Store-->>Flask: items[]
    Flask-->>ApiJs: 200 OK + items[]
    ApiJs-->>UI: items[]
    UI-->>User: updated list + request log
```

If the backend returns an error (e.g. `400` for a missing `name`, `404` for an
unknown id), `api.js` throws and `App.jsx` shows it in the red alert banner and logs
the failure in the request log — no silent failures.

## Why these choices

- **In-memory storage instead of a database** — keeps the demo runnable with zero
  setup (no Postgres/SQLite/migrations). The tradeoff is that state doesn't survive
  a restart, which is fine for a demo/testing app.
- **Separate frontend/backend processes instead of Flask serving the built UI** —
  keeps the API independently testable (curl, Swagger, `test-api.bat`) and lets the
  frontend use Vite's dev server (fast HMR) instead of a production build during
  development.
- **`concurrently` instead of two manual terminals** — one command (`npm run dev`)
  to get a working full-stack environment; the two-terminal steps are kept in the
  README as a fallback for anyone who wants the processes separated.
