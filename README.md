# python-web-demo

A small Flask REST API demonstrating full CRUD (Create, Read, Update, Delete) on an
`items` resource, backed by **in-memory storage** — no database required. Data resets
whenever the server restarts.

Includes a React UI (in `frontend/`) for exercising the same CRUD actions from a browser.

## Project structure

```
python-web-demo/
├── package.json        # root scripts to install/run backend + frontend together
├── app.py               # Flask API (backend)
├── requirements.txt
├── test-api.bat          # curl-based CRUD smoke test (Windows CMD)
└── frontend/              # React + Vite UI that calls the API
    └── src/
        ├── api.js           # fetch helpers for the API
        └── App.jsx            # create/list/edit/delete UI + request log
```

## Prerequisites

- Python 3
- Node.js (for the React frontend and the root dev script)

  Verify with:

  ```bash
  python --version
  node -v
  npm -v
  ```

## Setup

From the `python-web-demo` folder, install both the Python and Node dependencies in
one go:

```bash
cd python-web-demo
npm run install:all
```

(This runs `py -m pip install -r requirements.txt` and `npm install` inside
`frontend/`. You can run those two commands separately if you prefer.)

## Run it — one command

```bash
npm run dev
```

This starts **both** the Flask API (http://localhost:5000) and the React UI
(http://localhost:5174) together in one terminal, labeled `[backend]` / `[frontend]`
in the output. Stop both with `Ctrl+C`.

Open **http://localhost:5174** to use the CRUD Tester UI.

<details>
<summary>Running them separately instead</summary>

Backend, in one terminal:

```bash
py app.py
```

Frontend, in a second terminal:

```bash
cd frontend
npm run dev
```

</details>

## API endpoints

| Method | Path          | Description                                             |
|--------|---------------|----------------------------------------------------------|
| GET    | `/`           | API info / list of endpoints                             |
| GET    | `/items`      | List all items                                            |
| GET    | `/items/<id>` | Get a single item                                          |
| POST   | `/items`      | Create an item — JSON body: `name` (required), `description`, `price` |
| PUT    | `/items/<id>` | Update an item — JSON body: any of `name`, `description`, `price` |
| DELETE | `/items/<id>` | Delete an item                                              |

Errors are returned as JSON with an appropriate HTTP status, e.g. `404` for a missing
item or `400` when `name` is omitted on create. `flask-cors` is enabled so the React
dev server (a different origin) can call the API directly.

### Example usage (curl)

```bash
# List all items
curl http://localhost:5000/items

# Get one item
curl http://localhost:5000/items/1

# Create an item
curl -X POST http://localhost:5000/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Mug","description":"Ceramic mug","price":6.5}'

# Update an item
curl -X PUT http://localhost:5000/items/1 \
  -H "Content-Type: application/json" \
  -d '{"price":5.99}'

# Delete an item
curl -X DELETE http://localhost:5000/items/1
```

### Quick test script (Windows)

With the backend running, `test-api.bat` exercises every endpoint (list, get, create,
update, delete) using curl. Run it from Command Prompt:

```bat
test-api.bat
```

## What the UI does

- **Create item** — form at the top (`POST /items`)
- **Items** — live list from `GET /items`, each with:
  - **Edit** — inline form, saves with `PUT /items/<id>`
  - **Delete** — removes it with `DELETE /items/<id>`
- **Refresh** button to re-fetch the list on demand
- **Request log** — shows the last 20 API calls made from the UI (method, path,
  result, timestamp), so you can see exactly what each action sent and whether it
  succeeded — useful for testing/demoing the API's behavior.

By default the UI talks to `http://localhost:5000`; override with a `VITE_API_URL`
env var if the backend runs elsewhere.

## Notes

- Storage is in-memory on the Flask side — restarting `app.py` resets the data back
  to the 3 seeded items.
- The frontend has no build step required for local testing. Use `npm run build` /
  `npm run preview` inside `frontend/` for a production build.
