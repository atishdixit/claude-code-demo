# react-auth-demo

A small React app demonstrating Register / Log in / Dashboard flows with **no
backend and no database** — accounts and sessions are persisted entirely in the
browser's `localStorage`. Built with Vite + React + React Router.

## Pages

- **Register** (`/register`) — create an account: username, email, password,
  confirm password.
- **Log in** (`/login`) — sign in with username *or* email + password.
- **Dashboard** (`/dashboard`) — protected route, shows the logged-in user's info
  and a logout button.

Route protection is state-driven: logged-in users are redirected away from
`/login`/`/register` to `/dashboard`, and logged-out users are redirected away
from `/dashboard` to `/login`. The session survives a page reload (it's read from
`localStorage` on load).

## Validation

All fields are validated both on blur (as you leave a field) and on submit —
errors show inline under each field, and the submit button won't proceed until
everything passes:

| Field | Rules |
|---|---|
| Username | required, 3–20 characters, letters/numbers/underscore only |
| Email | required, must match a valid email pattern |
| Password | required, 8+ characters, needs an uppercase letter, a lowercase letter, and a number |
| Confirm password | required, must match password |

Registration also rejects a username or email that's already taken (checked
against what's in `localStorage`), and login rejects an unknown
username/email or an incorrect password — both with a clear error message.

## Storage & security notes

- `localStorage.authDemoUsers` — array of `{ username, email, salt, passwordHash, createdAt }`.
  **Passwords are never stored in plain text**: each one is hashed with a random
  per-user salt using the Web Crypto API (`SHA-256(salt + password)`), in
  [`src/utils/auth.js`](src/utils/auth.js).
- `localStorage.authDemoCurrentUser` — just the logged-in username, acting as the
  "session".
- This is still **not** how you'd secure a real app — a genuine backend would use
  a proper password hash (bcrypt/argon2/scrypt) with a real work factor, and a
  session shouldn't be a client-readable/writable localStorage key. This demo's
  hashing is there so a real password never sits in localStorage in the clear,
  not as a claim of production-grade security.

## Run it

```bash
npm install
npm run dev
```

Open the URL Vite prints (default **http://localhost:5175** — fixed in
`vite.config.js` to avoid clashing with other Vite projects).

## Tested

Ran live in the browser end-to-end:

- Submitting an empty Register form shows all 4 "required" errors at once.
- Submitting with a too-short username, invalid email, weak password, and
  mismatched confirm-password shows the corresponding format errors.
- A valid registration succeeds, redirects to `/login` with a success banner,
  and the account (with hashed password, no plaintext) lands in
  `localStorage.authDemoUsers`.
- Registering a second account with the same username, or same email, is
  rejected with a clear error.
- Logging in with the wrong password is rejected.
- Logging in with the correct password (tried via both username and email)
  succeeds and reaches the dashboard with the right user info.
- Reloading the page while logged in stays on the dashboard (session persists).
- Visiting `/login` or `/register` while logged in redirects to `/dashboard`;
  visiting `/dashboard` while logged out redirects to `/login`.
- Logging out clears the session and redirects to `/login`.
