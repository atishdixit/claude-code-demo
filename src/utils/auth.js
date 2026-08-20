// No backend / no database — users are persisted in localStorage.
// Passwords are never stored in plain text: each one is hashed with a
// per-user random salt using the Web Crypto API (SHA-256). This is still
// NOT how you'd secure a real production app (that needs a server-side
// hash like bcrypt/argon2 with proper work factors) — it's here purely so
// this demo doesn't leave raw passwords sitting in localStorage.

const USERS_KEY = "authDemoUsers";
const SESSION_KEY = "authDemoCurrentUser";

function loadUsers() {
  const raw = localStorage.getItem(USERS_KEY);
  return raw ? JSON.parse(raw) : [];
}

function saveUsers(users) {
  localStorage.setItem(USERS_KEY, JSON.stringify(users));
}

function bufferToHex(buffer) {
  return Array.from(new Uint8Array(buffer))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

function randomSalt() {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  return bufferToHex(bytes.buffer);
}

async function hashPassword(password, salt) {
  const encoder = new TextEncoder();
  const data = encoder.encode(salt + password);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return bufferToHex(digest);
}

export async function registerUser({ username, email, password }) {
  const users = loadUsers();
  const usernameTaken = users.some((u) => u.username.toLowerCase() === username.toLowerCase());
  const emailTaken = users.some((u) => u.email.toLowerCase() === email.toLowerCase());

  if (usernameTaken) {
    throw new Error("That username is already taken.");
  }
  if (emailTaken) {
    throw new Error("An account with that email already exists.");
  }

  const salt = randomSalt();
  const passwordHash = await hashPassword(password, salt);
  const newUser = { username, email, salt, passwordHash, createdAt: new Date().toISOString() };

  saveUsers([...users, newUser]);
  return { username: newUser.username, email: newUser.email };
}

export async function loginUser({ usernameOrEmail, password }) {
  const users = loadUsers();
  const match = usernameOrEmail.toLowerCase();
  const user = users.find(
    (u) => u.username.toLowerCase() === match || u.email.toLowerCase() === match
  );

  if (!user) {
    throw new Error("No account found with that username/email.");
  }

  const attemptedHash = await hashPassword(password, user.salt);
  if (attemptedHash !== user.passwordHash) {
    throw new Error("Incorrect password.");
  }

  localStorage.setItem(SESSION_KEY, user.username);
  return { username: user.username, email: user.email };
}

export function logoutUser() {
  localStorage.removeItem(SESSION_KEY);
}

export function getCurrentUser() {
  const username = localStorage.getItem(SESSION_KEY);
  if (!username) return null;

  const users = loadUsers();
  const user = users.find((u) => u.username === username);
  return user ? { username: user.username, email: user.email } : null;
}
