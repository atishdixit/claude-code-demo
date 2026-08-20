import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { loginUser } from "../utils/auth.js";
import { validateRequired } from "../utils/validation.js";

export default function Login({ onLogin }) {
  const navigate = useNavigate();
  const location = useLocation();
  const justRegistered = location.state?.justRegistered;
  const prefillUsername = location.state?.username || "";

  const [form, setForm] = useState({ usernameOrEmail: prefillUsername, password: "" });
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  }

  function handleBlur(e) {
    const { name, value } = e.target;
    const label = name === "usernameOrEmail" ? "Username or email" : "Password";
    setErrors((prev) => ({ ...prev, [name]: validateRequired(value, label) }));
  }

  function validateAll() {
    const next = {
      usernameOrEmail: validateRequired(form.usernameOrEmail, "Username or email"),
      password: validateRequired(form.password, "Password"),
    };
    setErrors(next);
    return Object.values(next).every((msg) => !msg);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError("");
    if (!validateAll()) return;

    setSubmitting(true);
    try {
      const user = await loginUser(form);
      onLogin(user);
      navigate("/dashboard");
    } catch (err) {
      setSubmitError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Log in</h1>
        <p className="auth-sub">Stored locally in your browser — no server, no database.</p>

        {justRegistered && (
          <div className="alert alert-success">Account created — log in to continue.</div>
        )}
        {submitError && <div className="alert alert-error">{submitError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="usernameOrEmail">Username or email</label>
            <input
              id="usernameOrEmail"
              name="usernameOrEmail"
              type="text"
              value={form.usernameOrEmail}
              onChange={handleChange}
              onBlur={handleBlur}
              className={errors.usernameOrEmail ? "invalid" : ""}
            />
            {errors.usernameOrEmail && <span className="field-error">{errors.usernameOrEmail}</span>}
          </div>

          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              onBlur={handleBlur}
              className={errors.password ? "invalid" : ""}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? "Logging in..." : "Log in"}
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </div>
  );
}
