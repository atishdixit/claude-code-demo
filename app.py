"""Small Flask CRUD demo backed by in-memory storage (no database).

Resource: "items" — each has id, name, description, price.
Swagger UI is available at /apidocs once the server is running.
"""

from itertools import count

from flasgger import Swagger
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # allow the React dev server (different origin) to call this API

app.config["SWAGGER"] = {
    "title": "python-web-demo API",
    "uiversion": 3,
}
swagger_template = {
    "info": {
        "title": "python-web-demo API",
        "description": "CRUD demo API for an in-memory 'items' resource. No database — data resets on restart.",
        "version": "1.0.0",
    },
    "definitions": {
        "Item": {
            "type": "object",
            "properties": {
                "id": {"type": "integer", "example": 1},
                "name": {"type": "string", "example": "Notebook"},
                "description": {"type": "string", "example": "A5 ruled notebook"},
                "price": {"type": "number", "example": 4.99},
            },
        },
        "ItemInput": {
            "type": "object",
            "required": ["name"],
            "properties": {
                "name": {"type": "string", "example": "Mug"},
                "description": {"type": "string", "example": "Ceramic mug"},
                "price": {"type": "number", "example": 6.5},
            },
        },
        "Error": {
            "type": "object",
            "properties": {"error": {"type": "string", "example": "item not found"}},
        },
    },
}
Swagger(app, template=swagger_template)

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
            "docs": "/apidocs",
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
    """List all items
    ---
    tags:
      - items
    responses:
      200:
        description: Array of items
        schema:
          type: array
          items:
            $ref: '#/definitions/Item'
    """
    return jsonify(list(items.values()))


@app.get("/items/<int:item_id>")
def get_item(item_id):
    """Get a single item
    ---
    tags:
      - items
    parameters:
      - name: item_id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: The item
        schema:
          $ref: '#/definitions/Item'
      404:
        description: Item not found
        schema:
          $ref: '#/definitions/Error'
    """
    item = items.get(item_id)
    if item is None:
        return jsonify({"error": "item not found"}), 404
    return jsonify(item)


@app.post("/items")
def create_item():
    """Create an item
    ---
    tags:
      - items
    parameters:
      - name: body
        in: body
        required: true
        schema:
          $ref: '#/definitions/ItemInput'
    responses:
      201:
        description: The created item
        schema:
          $ref: '#/definitions/Item'
      400:
        description: Validation error (name is required)
        schema:
          $ref: '#/definitions/Error'
    """
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
    """Update an item
    ---
    tags:
      - items
    parameters:
      - name: item_id
        in: path
        type: integer
        required: true
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            name:
              type: string
            description:
              type: string
            price:
              type: number
    responses:
      200:
        description: The updated item
        schema:
          $ref: '#/definitions/Item'
      404:
        description: Item not found
        schema:
          $ref: '#/definitions/Error'
    """
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
    """Delete an item
    ---
    tags:
      - items
    parameters:
      - name: item_id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: The deleted item
        schema:
          type: object
          properties:
            deleted:
              $ref: '#/definitions/Item'
      404:
        description: Item not found
        schema:
          $ref: '#/definitions/Error'
    """
    item = items.pop(item_id, None)
    if item is None:
        return jsonify({"error": "item not found"}), 404
    return jsonify({"deleted": item})


if __name__ == "__main__":
    app.run(debug=True, port=5000)
