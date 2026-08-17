# MyPost

A small two-page React app for creating, viewing, and deleting posts, backed by the
[JSONPlaceholder](https://jsonplaceholder.typicode.com) public test API.

Built with [Vite](https://vitejs.dev) + React 19 + React Router.

## Pages

- **Add Post** (`/`) — form to create a new post (title, body, user ID). Submits to
  `POST /posts` on JSONPlaceholder.
- **View Posts** (`/posts`) — lists posts (seeded from `GET /posts`) with a **Delete**
  button per post, which calls `DELETE /posts/:id`.

## Prerequisites

- [Node.js](https://nodejs.org) 18 or later (includes `npm`). Verify with:

  ```bash
  node -v
  npm -v
  ```

  If either command isn't found, install Node.js first. On Windows with `winget`:

  ```bash
  winget install OpenJS.NodeJS.LTS
  ```

## Setup

Clone the repo and install dependencies:

```bash
git clone https://github.com/atishdixit/claude-code-demo.git
cd claude-code-demo/MyPost
npm install
```

(If you already have the project locally, just run `npm install` from the `MyPost` folder.)

## Run it

Start the Vite dev server:

```bash
npm run dev
```

Vite will print a local URL (default **http://localhost:5173**) — open it in your browser.
The dev server supports hot reload, so edits to files in `src/` show up immediately.

To stop it, press `Ctrl+C` in the terminal running the server.

### Other scripts

```bash
npm run build     # production build, output to dist/
npm run preview   # serve the production build locally
npm run lint      # run oxlint
```

## Project structure

```
MyPost/
├── index.html          # Vite entry HTML
├── src/
│   ├── main.jsx         # app bootstrap, router setup
│   ├── App.jsx           # layout + route definitions
│   ├── App.css / index.css
│   ├── api.js             # JSONPlaceholder API + localStorage helpers
│   └── pages/
│       ├── AddPost.jsx    # "/" — create post form
│       └── Posts.jsx      # "/posts" — list + delete posts
├── package.json
└── vite.config.js
```

## Notes

- JSONPlaceholder is a fake API — it accepts POST/DELETE requests and responds realistically,
  but doesn't actually persist changes server-side. To make the app feel fully functional
  across page loads, `src/api.js` mirrors the list into `localStorage` after each successful
  API call, so adds/deletes stick around when you navigate between pages or reload.
- Use **Reset demo data** on the View Posts page to clear localStorage and re-seed from the
  live API.
