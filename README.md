# MyPost

A small two-page React demo app for creating, viewing, and deleting posts, backed by the
[JSONPlaceholder](https://jsonplaceholder.typicode.com) public test API.

No build tooling required — React, ReactDOM, and Babel are loaded from CDN, and JSX is
compiled in the browser. (Node.js/npm were not available in the environment this was built
in, so this avoids needing a bundler while still being a real, working React app.)

## Pages

- **Add Post** (`index.html`) — form to create a new post (title, body, user ID). Submits to
  `POST /posts` on JSONPlaceholder.
- **View Posts** (`posts.html`) — lists posts (seeded from `GET /posts`) with a **Delete**
  button per post, which calls `DELETE /posts/:id`.

## Run it

JSONPlaceholder requires the page to be served over http(s) rather than opened as a `file://`
URL. Any static file server works, e.g.:

```bash
py -m http.server 5173
```

Then open http://localhost:5173/index.html in a browser.

## Notes

- JSONPlaceholder is a fake API — it accepts POST/DELETE requests and responds realistically,
  but doesn't actually persist changes server-side. To make the app feel fully functional
  across page loads, `api.js` mirrors the list into `localStorage` after each successful API
  call, so adds/deletes stick around when you navigate between pages or reload.
- Use **Reset demo data** on the View Posts page to clear localStorage and re-seed from the
  live API.
