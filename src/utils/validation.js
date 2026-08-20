const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const USERNAME_REGEX = /^[a-zA-Z0-9_]+$/;

export function validateUsername(username) {
  if (!username.trim()) return "Username is required.";
  if (username.length < 3) return "Username must be at least 3 characters.";
  if (username.length > 20) return "Username must be at most 20 characters.";
  if (!USERNAME_REGEX.test(username)) {
    return "Username can only contain letters, numbers, and underscores.";
  }
  return "";
}

export function validateEmail(email) {
  if (!email.trim()) return "Email is required.";
  if (!EMAIL_REGEX.test(email)) return "Enter a valid email address.";
  return "";
}

export function validatePassword(password) {
  if (!password) return "Password is required.";
  if (password.length < 8) return "Password must be at least 8 characters.";
  if (!/[a-z]/.test(password)) return "Password must include a lowercase letter.";
  if (!/[A-Z]/.test(password)) return "Password must include an uppercase letter.";
  if (!/[0-9]/.test(password)) return "Password must include a number.";
  return "";
}

export function validateConfirmPassword(password, confirmPassword) {
  if (!confirmPassword) return "Please confirm your password.";
  if (password !== confirmPassword) return "Passwords do not match.";
  return "";
}

export function validateRequired(value, label) {
  return value.trim() ? "" : `${label} is required.`;
}
