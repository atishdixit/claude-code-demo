import { useEffect, useState } from "react";
import "./App.css";
import { BASE_URL, createItem, deleteItem, listItems, updateItem } from "./api.js";

const emptyForm = { name: "", description: "", price: "" };

function logEntry(method, path, ok, detail) {
  return {
    id: Date.now() + Math.random(),
    time: new Date().toLocaleTimeString(),
    method,
    path,
    ok,
    detail,
  };
}

export default function App() {
  const [items, setItems] = useState(null); // null = loading
  const [error, setError] = useState("");
  const [log, setLog] = useState([]);

  const [createForm, setCreateForm] = useState(emptyForm);
  const [creating, setCreating] = useState(false);

  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState(emptyForm);
  const [savingId, setSavingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  function pushLog(entry) {
    setLog((prev) => [entry, ...prev].slice(0, 20));
  }

  async function refresh() {
    setError("");
    try {
      const data = await listItems();
      setItems(data);
      pushLog(logEntry("GET", "/items", true, `${data.length} item(s)`));
    } catch (err) {
      setError(err.message);
      setItems([]);
      pushLog(logEntry("GET", "/items", false, err.message));
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    if (!createForm.name.trim()) {
      setError("Name is required to create an item.");
      return;
    }
    setCreating(true);
    setError("");
    try {
      const payload = {
        name: createForm.name.trim(),
        description: createForm.description.trim(),
        price: createForm.price === "" ? 0 : Number(createForm.price),
      };
      const created = await createItem(payload);
      pushLog(logEntry("POST", "/items", true, `created #${created.id}`));
      setCreateForm(emptyForm);
      await refresh();
    } catch (err) {
      setError(err.message);
      pushLog(logEntry("POST", "/items", false, err.message));
    } finally {
      setCreating(false);
    }
  }

  function startEdit(item) {
    setEditingId(item.id);
    setEditForm({ name: item.name, description: item.description, price: String(item.price) });
  }

  function cancelEdit() {
    setEditingId(null);
    setEditForm(emptyForm);
  }

  async function handleSaveEdit(id) {
    setSavingId(id);
    setError("");
    try {
      const payload = {
        name: editForm.name.trim(),
        description: editForm.description.trim(),
        price: editForm.price === "" ? 0 : Number(editForm.price),
      };
      await updateItem(id, payload);
      pushLog(logEntry("PUT", `/items/${id}`, true, "updated"));
      cancelEdit();
      await refresh();
    } catch (err) {
      setError(err.message);
      pushLog(logEntry("PUT", `/items/${id}`, false, err.message));
    } finally {
      setSavingId(null);
    }
  }

  async function handleDelete(id) {
    setDeletingId(id);
    setError("");
    try {
      await deleteItem(id);
      pushLog(logEntry("DELETE", `/items/${id}`, true, "deleted"));
      await refresh();
    } catch (err) {
      setError(err.message);
      pushLog(logEntry("DELETE", `/items/${id}`, false, err.message));
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="page">
      <header className="topbar">
        <div className="brand">
          <span className="brand-badge">C</span> CRUD Tester
        </div>
        <div className="api-target">
          API: <code>{BASE_URL}</code>
        </div>
      </header>

      <main className="container">
        <section className="card">
          <h2 className="section-title">Create item</h2>
          {error && <div className="alert alert-error">{error}</div>}
          <form className="inline-form" onSubmit={handleCreate}>
            <input
              type="text"
              placeholder="Name"
              value={createForm.name}
              onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
            />
            <input
              type="text"
              placeholder="Description"
              value={createForm.description}
              onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
            />
            <input
              type="number"
              step="0.01"
              placeholder="Price"
              value={createForm.price}
              onChange={(e) => setCreateForm({ ...createForm, price: e.target.value })}
            />
            <button className="btn btn-primary" type="submit" disabled={creating}>
              {creating ? "Creating..." : "POST /items"}
            </button>
          </form>
        </section>

        <section className="toolbar">
          <h2 className="section-title" style={{ margin: 0 }}>
            Items
          </h2>
          <button className="btn btn-ghost" onClick={refresh}>
            Refresh (GET /items)
          </button>
        </section>

        {items === null && <div className="spinner"></div>}

        {items !== null && items.length === 0 && <p className="empty-state">No items yet — create one above.</p>}

        {items !== null && items.length > 0 && (
          <div className="item-list">
            {items.map((item) => {
              const isEditing = editingId === item.id;
              return (
                <div className="item-card" key={item.id}>
                  {isEditing ? (
                    <div className="item-edit-form">
                      <input
                        type="text"
                        value={editForm.name}
                        onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                      />
                      <input
                        type="text"
                        value={editForm.description}
                        onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                      />
                      <input
                        type="number"
                        step="0.01"
                        value={editForm.price}
                        onChange={(e) => setEditForm({ ...editForm, price: e.target.value })}
                      />
                      <div className="item-actions">
                        <button
                          className="btn btn-primary"
                          onClick={() => handleSaveEdit(item.id)}
                          disabled={savingId === item.id}
                        >
                          {savingId === item.id ? "Saving..." : "PUT save"}
                        </button>
                        <button className="btn btn-ghost" onClick={cancelEdit}>
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="item-main">
                        <p className="item-name">{item.name}</p>
                        <p className="item-desc">{item.description}</p>
                        <p className="item-meta">
                          #{item.id} &middot; ${Number(item.price).toFixed(2)}
                        </p>
                      </div>
                      <div className="item-actions">
                        <button className="btn btn-ghost" onClick={() => startEdit(item)}>
                          Edit
                        </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => handleDelete(item.id)}
                          disabled={deletingId === item.id}
                        >
                          {deletingId === item.id ? "Deleting..." : "Delete"}
                        </button>
                      </div>
                    </>
                  )}
                </div>
              );
            })}
          </div>
        )}

        <section className="card log-card">
          <h2 className="section-title">Request log</h2>
          {log.length === 0 && <p className="empty-state">Actions you take will show up here.</p>}
          <ul className="log-list">
            {log.map((entry) => (
              <li key={entry.id} className={`log-entry ${entry.ok ? "ok" : "fail"}`}>
                <span className="log-method">{entry.method}</span>
                <span className="log-path">{entry.path}</span>
                <span className="log-detail">{entry.detail}</span>
                <span className="log-time">{entry.time}</span>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </div>
  );
}
