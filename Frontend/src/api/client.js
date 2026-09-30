const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8081/api'

async function request(path, { userId, method = 'GET', body } = {}) {
  const headers = {
    Accept: 'application/json',
  }
  if (userId) headers['X-User-Id'] = userId
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    let message = `Request failed (${response.status})`
    try {
      const data = await response.json()
      message = data.message || data.detail || message
    } catch {
      // ignore parse errors
    }
    throw new Error(message)
  }

  if (response.status === 204) return null
  return response.json()
}

export const api = {
  listUsers: () => request('/users'),
  listRequests: (userId) => request('/requests', { userId }),
  getRequest: (userId, id) => request(`/requests/${id}`, { userId }),
  createRequest: (userId, payload) =>
    request('/requests', { userId, method: 'POST', body: payload }),
  assignRequest: (userId, id) =>
    request(`/requests/${id}/assign`, { userId, method: 'POST' }),
  updateStatus: (userId, id, payload) =>
    request(`/requests/${id}/status`, { userId, method: 'POST', body: payload }),
  addComment: (userId, id, content) =>
    request(`/requests/${id}/comments`, {
      userId,
      method: 'POST',
      body: { content },
    }),
  getSummary: (userId) => request('/summary', { userId }),
}
