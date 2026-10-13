const API_BASE_URL =
  import.meta.env?.VITE_API_BASE_URL ?? "/api/v1";

async function parseError(response) {
  try {
    const body = await response.json();
    if (body?.details?.length) {
      return body.details.join(" ");
    }
    if (body?.message) {
      return body.message;
    }
  } catch {
    // ignore parse errors
  }
  return `Request failed (${response.status})`;
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export async function login(email, password) {
  const data = await apiRequest("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  return data.user;
}

export async function fetchCurrentUser() {
  return apiRequest("/auth/me");
}

export async function logout() {
  return apiRequest("/auth/logout", { method: "POST" });
}

export async function checkBackendHealth() {
  return apiRequest("/health");
}

export async function createRequest({ title, description, category }) {
  return apiRequest("/requests", {
    method: "POST",
    body: JSON.stringify({ title, description, category }),
  });
}

export async function fetchMyRequests() {
  return apiRequest("/requests");
}

export { API_BASE_URL };