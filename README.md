# python-web-demo

A small Flask REST API demonstrating full CRUD (Create, Read, Update, Delete) on an
`items` resource, backed by **in-memory storage** — no database required. Data resets
whenever the server restarts.

Includes a React UI (in `frontend/`) for exercising the same CRUD actions from a browser.

## Project structure

```
python-web-demo/
├── app.py             # Flask API (backend)
├── requirements.txt
├── test-api.bat        # curl-based CRUD smoke test (Windows CMD)
└── frontend/            # React + Vite UI that calls the API
    └── src/
        ├── api.js         # fetch helpers for the API
        └── App.jsx          # create/list/edit/delete UI + request log
```

## Prerequisites

- Python 3
- Node.js (for the React frontend)

  Verify with:

  ```bash
  python --version
  node -v
  npm -v
  ```

## 1. Run the backend (API)

```bash
cd python-web-demo
py -m pip install -r requirements.txt
py app.py
```

The API starts at **http://localhost:5000**, seeded with 3 sample items. `flask-cors`
is enabled so the React dev server (a different origin) can call it directly.

### API endpoints

| Method | Path          | Description                                             |
|--------|---------------|----------------------------------------------------------|
| GET    | `/`           | API info / list of endpoints                             |
| GET    | `/items`      | List all items                                            |
| GET    | `/items/<id>` | Get a single item                                          |
| POST   | `/items`      | Create an item — JSON body: `name` (required), `description`, `price` |
| PUT    | `/items/<id>` | Update an item — JSON body: any of `name`, `description`, `price` |
| DELETE | `/items/<id>` | Delete an item                                              |

Errors are returned as JSON with an appropriate HTTP status, e.g. `404` for a missing
item or `400` when `name` is omitted on create.

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

With the server running, `test-api.bat` exercises every endpoint (list, get, create,
update, delete) using curl. Run it from Command Prompt:

```bat
test-api.bat
```

## 2. Run the frontend (React UI)

In a **second terminal**, with the backend still running:

```bash
cd python-web-demo/frontend
npm install
npm run dev
```

Open the URL Vite prints (default **http://localhost:5174** — the port is fixed in
`vite.config.js` so it doesn't collide with other Vite projects). By default it talks
to the API at `http://localhost:5000`; override with a `VITE_API_URL` env var if your
backend runs elsewhere, e.g.:

```bash
VITE_API_URL=http://localhost:5000 npm run dev
```

### What the UI does

- **Create item** — form at the top (`POST /items`)
- **Items** — live list from `GET /items`, each with:
  - **Edit** — inline form, saves with `PUT /items/<id>`
  - **Delete** — removes it with `DELETE /items/<id>`
- **Refresh** button to re-fetch the list on demand
- **Request log** — shows the last 20 API calls made from the UI (method, path,
  result, timestamp), so you can see exactly what each action sent and whether it
  succeeded — useful for testing/demoing the API's behavior.

## Notes

- Storage is in-memory on the Flask side — restarting `app.py` resets the data back
  to the 3 seeded items.
- The frontend has no build step required for local testing — `npm run dev` is enough.
  Use `npm run build` / `npm run preview` in `frontend/` for a production build.
