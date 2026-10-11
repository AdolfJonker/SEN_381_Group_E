const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8081/api'

/*
 * Login credentials are kept in memory only (never localStorage), so they
 * are gone when the tab is closed or refreshed. Every call sends them with
 * HTTP Basic. This must run over HTTPS outside local development.
 */
let authHeader = null
let onUnauthorized = null

function toBase64(text) {
  // btoa only accepts Latin-1, so encode as UTF-8 bytes first
  const bytes = new TextEncoder().encode(text)
  let binary = ''
  bytes.forEach((b) => {
    binary += String.fromCharCode(b)
  })
  return btoa(binary)
}

export function setCredentials(email, password) {
  authHeader = `Basic ${toBase64(`${email}:${password}`)}`
}

export function clearCredentials() {
  authHeader = null
}

/** Called when any API call returns 401 (e.g. wrong password or expired login). */
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

function toQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value) query.set(key, value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' }
  if (authHeader) headers.Authorization = authHeader
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(
      'Cannot reach the server. Check that the backend is running.',
      0,
    )
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    if (response.status === 401) message = 'Please sign in again.'
    if (response.status === 403)
      message = 'Your role is not allowed to do this.'
    try {
      const data = await response.json()
      message = data.message || data.detail || message
    } catch {
      // Body is empty for 401/403 from Spring Security; keep the default message
    }
    if (response.status === 401 && onUnauthorized) onUnauthorized()
    throw new ApiError(message, response.status)
  }

  if (response.status === 204) return null
  return response.json()
}

export const api = {
  /** Checks the login and returns { id, name, email, role }. */
  me: () => request('/auth/me'),
  /**
   * FR005 / FR006 / FR013. Optional filters are sent as query parameters
   * (status, category) for the server-side filter endpoint.
   */
  listRequests: (filters = {}) => request(`/requests${toQuery(filters)}`),
  getRequest: (id) => request(`/requests/${id}`),
  createRequest: (payload) =>
    request('/requests', { method: 'POST', body: payload }),
  assignRequest: (id) => request(`/requests/${id}/assign`, { method: 'POST' }),
  updateStatus: (id, payload) =>
    request(`/requests/${id}/status`, { method: 'POST', body: payload }),
  addComment: (id, content) =>
    request(`/requests/${id}/comments`, {
      method: 'POST',
      body: { content },
    }),
  /** FR012: Management only (403 for other roles). */
  getSummary: () => request('/summary'),
}
