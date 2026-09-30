import { useState } from 'react'
import { CATEGORIES } from '../data/constants'

const empty = { title: '', description: '', category: '' }

export default function RequestForm({ onSubmit, onCancel }) {
  const [form, setForm] = useState(empty)
  const [error, setError] = useState('')

  function handleChange(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!form.title.trim()) {
      setError('Title is required.')
      return
    }
    if (!form.description.trim()) {
      setError('Description is required.')
      return
    }
    if (!form.category) {
      setError('Category is required.')
      return
    }
    onSubmit(form)
    setForm(empty)
  }

  return (
    <form className="panel form-panel" onSubmit={handleSubmit} noValidate>
      <header className="panel-header">
        <h2>Submit a service request</h2>
        <p>Title, description, and a category from the controlled list.</p>
      </header>

      <label>
        Title
        <input
          name="title"
          value={form.title}
          onChange={handleChange}
          maxLength={120}
          placeholder="Short summary of the issue"
        />
      </label>

      <label>
        Description
        <textarea
          name="description"
          value={form.description}
          onChange={handleChange}
          rows={5}
          placeholder="Where it is, what happens, and who is affected"
        />
      </label>

      <label>
        Category
        <select name="category" value={form.category} onChange={handleChange}>
          <option value="">Select a category…</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
      </label>

      {error ? <p className="form-error" role="alert">{error}</p> : null}

      <div className="form-actions">
        <button type="submit" className="btn primary">
          Submit request
        </button>
        {onCancel ? (
          <button type="button" className="btn ghost" onClick={onCancel}>
            Cancel
          </button>
        ) : null}
      </div>
    </form>
  )
}
