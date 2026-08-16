const { useState, useEffect } = React;

function PostsPage() {
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
          <a className="btn btn-primary" href="index.html">
            Add your first post
          </a>
        </div>
      )}

      {posts !== null && posts.length > 0 && (
        <div className="post-list">
          {posts.map((post) => (
            <div className={`post-card ${deletingId === post.id ? "removing" : ""}`} key={post.id}>
              <div className="post-main">
                <p className="post-title">{post.title}</p>
                <p className="post-body">{post.body}</p>
                <p className="post-meta">Post #{post.id} &middot; User {post.userId}</p>
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

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<PostsPage />);
