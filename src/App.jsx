import { NavLink, Routes, Route } from "react-router-dom";
import "./App.css";
import AddPost from "./pages/AddPost.jsx";
import Posts from "./pages/Posts.jsx";

export default function App() {
  return (
    <>
      <header className="topbar">
        <NavLink className="brand" to="/">
          <span className="brand-badge">M</span> MyPost
        </NavLink>
        <nav className="tabs">
          <NavLink to="/" end className={({ isActive }) => (isActive ? "active" : "")}>
            Add Post
          </NavLink>
          <NavLink to="/posts" className={({ isActive }) => (isActive ? "active" : "")}>
            View Posts
          </NavLink>
        </nav>
      </header>

      <main className="container">
        <Routes>
          <Route path="/" element={<AddPost />} />
          <Route path="/posts" element={<Posts />} />
        </Routes>
      </main>
    </>
  );
}
