import { useState } from "react";
import { Navigate, NavLink, Route, Routes } from "react-router-dom";
import "./App.css";
import Register from "./pages/Register.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import { getCurrentUser, logoutUser } from "./utils/auth.js";

export default function App() {
  const [user, setUser] = useState(() => getCurrentUser());

  function handleLogout() {
    logoutUser();
    setUser(null);
  }

  return (
    <>
      <header className="topbar">
        <NavLink className="brand" to="/">
          <span className="brand-badge">A</span> Auth Demo
        </NavLink>
        <nav className="tabs">
          {user ? (
            <span className="topbar-user">Signed in as {user.username}</span>
          ) : (
            <>
              <NavLink to="/login" className={({ isActive }) => (isActive ? "active" : "")}>
                Log in
              </NavLink>
              <NavLink to="/register" className={({ isActive }) => (isActive ? "active" : "")}>
                Register
              </NavLink>
            </>
          )}
        </nav>
      </header>

      <main className="container">
        <Routes>
          <Route path="/" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
          <Route
            path="/register"
            element={user ? <Navigate to="/dashboard" replace /> : <Register />}
          />
          <Route
            path="/login"
            element={user ? <Navigate to="/dashboard" replace /> : <Login onLogin={setUser} />}
          />
          <Route
            path="/dashboard"
            element={
              user ? (
                <Dashboard user={user} onLogout={handleLogout} />
              ) : (
                <Navigate to="/login" replace />
              )
            }
          />
        </Routes>
      </main>
    </>
  );
}
