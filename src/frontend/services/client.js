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
    const error = new Error(await parseError(response));
    // Keep the HTTP status so views can tell 401 (sign in again) from 403 (role not allowed).
    error.status = response.status;
    throw error;
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

// ----- Request views (FR005, FR012, FR013) -----

function toQuery(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value) query.set(key, value);
  });
  const text = query.toString();
  return text ? `?${text}` : "";
}

/**FR005: requests the signed-in role may see (the server applies the role's strategy).*/

export async function fetchRequests(filters = {}) {
  return apiRequest(`/requests${toQuery(filters)}`);
}

export async function fetchRequest(id) {
  return apiRequest(`/requests/${id}`);
}

/** FR012: Management only. Other roles get 403. */
export async function fetchSummary() {
  return apiRequest("/requests/summary");
}

export { API_BASE_URL };