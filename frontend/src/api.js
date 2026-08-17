const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:5000";

async function request(path, options) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const message = (data && data.error) || `Request failed (status ${res.status})`;
    throw new Error(message);
  }
  return data;
}

export function listItems() {
  return request("/items");
}

export function getItem(id) {
  return request(`/items/${id}`);
}

export function createItem(item) {
  return request("/items", { method: "POST", body: JSON.stringify(item) });
}

export function updateItem(id, item) {
  return request(`/items/${id}`, { method: "PUT", body: JSON.stringify(item) });
}

export function deleteItem(id) {
  return request(`/items/${id}`, { method: "DELETE" });
}

export { BASE_URL };
