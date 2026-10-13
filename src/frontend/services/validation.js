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

export const REQUEST_CATEGORIES = [
  "Facility Fault",
  "Damaged Equipment",
  "Security Concern",
  "IT Support",
  "Maintenance",
  "Lost Property",
  "Other",
];
export const TITLE_MAX = 200;
export const DESCRIPTION_MAX = 2000;

export function validateRequestForm({ title, description, category }) {
  const errors = {};
  const trimmedTitle = (title ?? "").trim();
  const trimmedDescription = (description ?? "").trim();

  if (!trimmedTitle) {
    errors.title = "Title is required.";
  } else if (trimmedTitle.length > TITLE_MAX) {
    errors.title = `Title must be at most ${TITLE_MAX} characters.`;
  }

  if (!trimmedDescription) {
    errors.description = "Description is required.";
  } else if (trimmedDescription.length > DESCRIPTION_MAX) {
    errors.description = `Description must be at most ${DESCRIPTION_MAX} characters.`;
  }

  if (!category) {
    errors.category = "Choose a category.";
  } else if (!REQUEST_CATEGORIES.includes(category)) {
    errors.category = "Choose a category from the list.";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    values: {
      title: trimmedTitle,
      description: trimmedDescription,
      category,
    },
  };
}

export const COMMENT_MAX = 2000;

export function validateComment(content) {
  const trimmed = (content ?? "").trim();
  const errors = {};

  if (!trimmed) {
    errors.content = "Write a comment before saving.";
  } else if (trimmed.length > COMMENT_MAX) {
    errors.content = `Comment must be at most ${COMMENT_MAX} characters.`;
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    values: { content: trimmed },
  };
}