import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../utils/auth.js";
import {
  validateUsername,
  validateEmail,
  validatePassword,
  validateConfirmPassword,
} from "../utils/validation.js";

const emptyForm = { username: "", email: "", password: "", confirmPassword: "" };

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function validateField(name, value) {
    switch (name) {
      case "username":
        return validateUsername(value);
      case "email":
        return validateEmail(value);
      case "password":
        return validatePassword(value);
      case "confirmPassword":
        return validateConfirmPassword(form.password, value);
      default:
        return "";
    }
  }

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  }

  function handleBlur(e) {
    const { name, value } = e.target;
    setErrors((prev) => ({ ...prev, [name]: validateField(name, value) }));
  }

  function validateAll() {
    const next = {
      username: validateUsername(form.username),
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      confirmPassword: validateConfirmPassword(form.password, form.confirmPassword),
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
      await registerUser(form);
      navigate("/login", { state: { justRegistered: true, username: form.username } });
    } catch (err) {
      setSubmitError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Create an account</h1>
        <p className="auth-sub">Stored locally in your browser — no server, no database.</p>

        {submitError && <div className="alert alert-error">{submitError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              name="username"
              type="text"
              value={form.username}
              onChange={handleChange}
              onBlur={handleBlur}
              className={errors.username ? "invalid" : ""}
            />
            {errors.username && <span className="field-error">{errors.username}</span>}
          </div>

          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              onBlur={handleBlur}
              className={errors.email ? "invalid" : ""}
            />
            {errors.email && <span className="field-error">{errors.email}</span>}
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
            {errors.password ? (
              <span className="field-error">{errors.password}</span>
            ) : (
              <span className="field-hint">
                At least 8 characters, with uppercase, lowercase, and a number.
              </span>
            )}
          </div>

          <div className="field">
            <label htmlFor="confirmPassword">Confirm password</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={handleChange}
              onBlur={handleBlur}
              className={errors.confirmPassword ? "invalid" : ""}
            />
            {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
          </div>

          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
