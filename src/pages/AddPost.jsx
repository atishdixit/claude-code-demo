import { useState } from "react";
import { Link } from "react-router-dom";
import { addPost } from "../api.js";

export default function AddPost() {
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [userId, setUserId] = useState(1);
  const [status, setStatus] = useState({ state: "idle" }); // idle | saving | success | error

  async function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !body.trim()) {
      setStatus({ state: "error", message: "Title and body are both required." });
      return;
    }
    setStatus({ state: "saving" });
    try {
      const post = await addPost({ title: title.trim(), body: body.trim(), userId: Number(userId) || 1 });
      setStatus({ state: "success", message: `Post "${post.title}" was created.` });
      setTitle("");
      setBody("");
    } catch (err) {
      setStatus({ state: "error", message: err.message || "Something went wrong." });
    }
  }

  return (
    <div>
      <h1 className="page-title">Add a new post</h1>
      <p className="page-sub">
        Submits to the JSONPlaceholder test API and saves the result locally so it shows up on the
        View Posts page.
      </p>

      <div className="card">
        {status.state === "success" && (
          <div className="alert alert-success">
            {status.message} <Link to="/posts">View it now &rarr;</Link>
          </div>
        )}
        {status.state === "error" && <div className="alert alert-error">{status.message}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="title">Title</label>
            <input
              id="title"
              type="text"
              placeholder="e.g. My first post"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="body">Body</label>
            <textarea
              id="body"
              placeholder="Write something..."
              value={body}
              onChange={(e) => setBody(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="userId">User ID</label>
            <input
              id="userId"
              type="number"
              min="1"
              max="10"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
            />
          </div>

          <button className="btn btn-primary" type="submit" disabled={status.state === "saving"}>
            {status.state === "saving" ? "Saving..." : "Add Post"}
          </button>
        </form>
      </div>

      <p className="footnote">
        Data source:{" "}
        <a href="https://jsonplaceholder.typicode.com" target="_blank" rel="noreferrer">
          jsonplaceholder.typicode.com
        </a>
      </p>
    </div>
  );
}
