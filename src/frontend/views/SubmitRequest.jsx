// SubmitRequest view

import { useState } from "react";
import { createRequest } from "../services/client.js";
import {
    DESCRIPTION_MAX,
    REQUEST_CATEGORIES,
    TITLE_MAX,
    validateRequestForm,
} from "../services/validation.js";
import { CategoryIcon } from "./Visuals.jsx";

export default function SubmitRequest({ onCreated, initialCategory = "" }) {
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [category, setCategory] = useState(initialCategory);
    const [fieldErrors, setFieldErrors] = useState({});
    const [formError, setFormError] = useState("");
    const [created, setCreated] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setFormError("");
        setCreated(null);

        const result = validateRequestForm({ title, description, category });
        setFieldErrors(result.errors);
        if (!result.isValid) {
            return;
        }

        setSubmitting(true);
        try {
            const saved = await createRequest(result.values);
            setCreated(saved ?? {});
            setTitle("");
            setDescription("");
            setCategory("");
            onCreated?.(saved);
        } catch (error) {
            setFormError(error.message || "Could not submit the request.");
        } finally {
            setSubmitting(false);
        }
    }

    const createdId = created?.id ?? created?.requestId;

    return (
        <div className="card form-card">
            <div className="form-head">
                <CategoryIcon category={category || "Other"} size="lg" />
                <div>
                    <h2 className="page-title">Submit a request</h2>
                    <p className="muted">Tell us what needs attention. All fields are required.</p>
                </div>
            </div>

            {created && (
                <div className="alert alert-success" role="status">
                    Request {createdId ? `#${createdId} ` : ""}submitted. Status: {created.status ?? "Open"}.
                </div>
            )}
            {formError && (
                <div className="alert alert-error" role="alert">
                    {formError}
                </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
                <div className="field">
                    <label htmlFor="category">Category</label>
                    <select
                        id="category"
                        name="category"
                        value={category}
                        onChange={(e) => setCategory(e.target.value)}
                        disabled={submitting}
                        aria-invalid={Boolean(fieldErrors.category)}
                    >
                        <option value="">Choose a category</option>
                        {REQUEST_CATEGORIES.map((c) => (
                            <option key={c} value={c}>
                                {c}
                            </option>
                        ))}
                    </select>
                    {fieldErrors.category && <p className="error">{fieldErrors.category}</p>}
                </div>

                <div className="field">
                    <label htmlFor="title">Title</label>
                    <input
                        id="title"
                        name="title"
                        type="text"
                        maxLength={TITLE_MAX + 50}
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        disabled={submitting}
                        aria-invalid={Boolean(fieldErrors.title)}
                    />
                    <div className="field-hint">
                        <span>A short summary of the problem</span>
                        <span>
              {title.trim().length}/{TITLE_MAX}
            </span>
                    </div>
                    {fieldErrors.title && <p className="error">{fieldErrors.title}</p>}
                </div>

                <div className="field">
                    <label htmlFor="description">Description</label>
                    <textarea
                        id="description"
                        name="description"
                        rows={6}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={submitting}
                        aria-invalid={Boolean(fieldErrors.description)}
                    />
                    <div className="field-hint">
                        <span>Where it is and what you have noticed</span>
                        <span>
              {description.trim().length}/{DESCRIPTION_MAX}
            </span>
                    </div>
                    {fieldErrors.description && <p className="error">{fieldErrors.description}</p>}
                </div>

                <div className="form-actions">
                    <button type="submit" className="btn-block" disabled={submitting}>
                        {submitting ? "Submitting..." : "Submit request"}
                    </button>
                </div>
            </form>
        </div>
    );
}