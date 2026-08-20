export default function Dashboard({ user, onLogout }) {
  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Welcome, {user.username}</h1>
        <p className="auth-sub">You're logged in — this page is only reachable with a session.</p>

        <div className="field">
          <label>Username</label>
          <p className="static-value">{user.username}</p>
        </div>
        <div className="field">
          <label>Email</label>
          <p className="static-value">{user.email}</p>
        </div>

        <button className="btn btn-danger btn-block" onClick={onLogout}>
          Log out
        </button>
      </div>
    </div>
  );
}
