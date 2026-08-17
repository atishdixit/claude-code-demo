// Public API: JSONPlaceholder (https://jsonplaceholder.typicode.com) — a free
// fake REST API for testing. It accepts POST/DELETE requests and responds
// realistically, but it does NOT persist changes on the server. To make this
// demo feel like a real working CRUD app across page loads, we mirror the
// API calls into localStorage as the "source of truth" for the UI.

const API_BASE = "https://jsonplaceholder.typicode.com/posts";
const STORAGE_KEY = "myPostPosts";

function loadPosts() {
  const raw = localStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

function savePosts(posts) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(posts));
}

async function fetchInitialPosts() {
  const res = await fetch(`${API_BASE}?_limit=15`);
  if (!res.ok) throw new Error(`Failed to fetch posts (status ${res.status})`);
  return res.json();
}

// Returns the current post list, seeding it from the live API on first run.
export async function getPosts() {
  let posts = loadPosts();
  if (!posts) {
    posts = await fetchInitialPosts();
    savePosts(posts);
  }
  return posts;
}

export async function addPost({ title, body, userId }) {
  const res = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json; charset=UTF-8" },
    body: JSON.stringify({ title, body, userId }),
  });
  if (!res.ok) throw new Error(`Failed to add post (status ${res.status})`);
  const data = await res.json();

  // JSONPlaceholder always echoes id:101 for new posts and never actually
  // stores them, so we assign a locally-unique id to keep the demo usable.
  const newPost = { ...data, id: Date.now() };

  const posts = await getPosts();
  const updated = [newPost, ...posts];
  savePosts(updated);
  return newPost;
}

export async function deletePost(id) {
  // Only hit the real API for ids that came from it (<= 100); locally
  // created posts use timestamp ids and only need to be removed locally.
  if (id <= 100) {
    await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  }
  const posts = loadPosts() || [];
  const updated = posts.filter((p) => p.id !== id);
  savePosts(updated);
  return updated;
}

export function resetDemoData() {
  localStorage.removeItem(STORAGE_KEY);
}
