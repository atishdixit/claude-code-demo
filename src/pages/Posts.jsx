import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { deletePost, getPosts, resetDemoData } from "../api.js";

export default function Posts() {
  const [posts, setPosts] = useState(null); // null = loading
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);

  async function load() {
    setError("");
    try {
      const data = await getPosts();
      setPosts(data);
    } catch (err) {
      setError(err.message || "Failed to load posts.");
      setPosts([]);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleDelete(id) {
    setDeletingId(id);
    try {
      const updated = await deletePost(id);
      setPosts(updated);
    } catch (err) {
      setError(err.message || "Failed to delete post.");
    } finally {
      setDeletingId(null);
    }
  }

  function handleReset() {
    resetDemoData();
    setPosts(null);
    load();
  }

  return (
    <div>
      <div className="toolbar">
        <div>
          <h1 className="page-title">Your posts</h1>
          <p className="page-sub" style={{ marginBottom: 0 }}>
            Fetched from JSONPlaceholder, plus anything you've added locally.
          </p>
        </div>
        <button className="btn btn-ghost" onClick={handleReset}>
          Reset demo data
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {posts === null && <div className="spinner"></div>}

      {posts !== null && posts.length === 0 && (
        <div className="empty-state">
          <p>No posts yet.</p>
          <Link className="btn btn-primary" to="/">
            Add your first post
          </Link>
        </div>
      )}

      {posts !== null && posts.length > 0 && (
        <div className="post-list">
          {posts.map((post) => (
            <div className={`post-card ${deletingId === post.id ? "removing" : ""}`} key={post.id}>
              <div className="post-main">
                <p className="post-title">{post.title}</p>
                <p className="post-body">{post.body}</p>
                <p className="post-meta">
                  Post #{post.id} &middot; User {post.userId}
                </p>
              </div>
              <button
                className="btn btn-danger"
                onClick={() => handleDelete(post.id)}
                disabled={deletingId === post.id}
              >
                {deletingId === post.id ? "Deleting..." : "Delete"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
