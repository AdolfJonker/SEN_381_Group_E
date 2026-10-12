const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateLoginForm({ email, password }) {
  const errors = {};
  const trimmedEmail = (email ?? "").trim();

  if (!trimmedEmail) {
    errors.email = "Email is required.";
  } else if (trimmedEmail.length > 255) {
    errors.email = "Email must be at most 255 characters.";
  } else if (!EMAIL_PATTERN.test(trimmedEmail)) {
    errors.email = "Enter a valid email address.";
  }

  if (!password) {
    errors.password = "Password is required.";
  } else if (password.length < 8) {
    errors.password = "Password must be at least 8 characters.";
  } else if (password.length > 128) {
    errors.password = "Password must be at most 128 characters.";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    values: {
      email: trimmedEmail.toLowerCase(),
      password,
    },
  };
}