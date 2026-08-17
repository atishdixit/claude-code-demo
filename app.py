"""Small Flask CRUD demo backed by in-memory storage (no database).

Resource: "items" — each has id, name, description, price.
"""

from itertools import count

from flask import Flask, jsonify, request

app = Flask(__name__)

# In-memory storage. Reset whenever the process restarts.
items = {}
next_id = count(1)


def seed():
    for name, description, price in [
        ("Notebook", "A5 ruled notebook", 4.99),
        ("Pen", "Blue ballpoint pen", 1.25),
        ("Backpack", "Water-resistant daypack", 39.99),
    ]:
        item_id = next(next_id)
        items[item_id] = {"id": item_id, "name": name, "description": description, "price": price}


seed()


@app.get("/")
def index():
    return jsonify(
        {
            "message": "Python CRUD demo (in-memory storage)",
            "endpoints": {
                "GET /items": "list all items",
                "GET /items/<id>": "get one item",
                "POST /items": "create an item (json: name, description, price)",
                "PUT /items/<id>": "update an item (json: any of name, description, price)",
                "DELETE /items/<id>": "delete an item",
            },
        }
    )


@app.get("/items")
def list_items():
    return jsonify(list(items.values()))


@app.get("/items/<int:item_id>")
def get_item(item_id):
    item = items.get(item_id)
    if item is None:
        return jsonify({"error": "item not found"}), 404
    return jsonify(item)


@app.post("/items")
def create_item():
    data = request.get_json(silent=True) or {}
    name = data.get("name")
    if not name:
        return jsonify({"error": "name is required"}), 400

    item_id = next(next_id)
    item = {
        "id": item_id,
        "name": name,
        "description": data.get("description", ""),
        "price": data.get("price", 0),
    }
    items[item_id] = item
    return jsonify(item), 201


@app.put("/items/<int:item_id>")
def update_item(item_id):
    item = items.get(item_id)
    if item is None:
        return jsonify({"error": "item not found"}), 404

    data = request.get_json(silent=True) or {}
    for field in ("name", "description", "price"):
        if field in data:
            item[field] = data[field]
    return jsonify(item)


@app.delete("/items/<int:item_id>")
def delete_item(item_id):
    item = items.pop(item_id, None)
    if item is None:
        return jsonify({"error": "item not found"}), 404
    return jsonify({"deleted": item})


if __name__ == "__main__":
    app.run(debug=True, port=5000)
