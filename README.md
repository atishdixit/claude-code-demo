# python-web-demo

A small Flask REST API demonstrating full CRUD (Create, Read, Update, Delete) on an
`items` resource, backed by **in-memory storage** — no database required. Data resets
whenever the server restarts.

## Prerequisites

- Python 3
- Flask (`pip install -r requirements.txt`)

## Setup

```bash
cd python-web-demo
py -m pip install -r requirements.txt
```

## Run it

```bash
py app.py
```

The server starts at **http://localhost:5000**, seeded with 3 sample items.

## Endpoints

| Method | Path          | Description                                             |
|--------|---------------|----------------------------------------------------------|
| GET    | `/`           | API info / list of endpoints                             |
| GET    | `/items`      | List all items                                            |
| GET    | `/items/<id>` | Get a single item                                          |
| POST   | `/items`      | Create an item — JSON body: `name` (required), `description`, `price` |
| PUT    | `/items/<id>` | Update an item — JSON body: any of `name`, `description`, `price` |
| DELETE | `/items/<id>` | Delete an item                                              |

## Example usage (curl)

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

Errors are returned as JSON with an appropriate HTTP status, e.g. `404` for a missing
item or `400` when `name` is omitted on create.

## Quick test script (Windows)

With the server running, `test-api.bat` exercises every endpoint (list, get, create,
update, delete) using curl. Run it from Command Prompt:

```bat
test-api.bat
```
