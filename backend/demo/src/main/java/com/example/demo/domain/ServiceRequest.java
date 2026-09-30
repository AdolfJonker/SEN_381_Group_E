package com.example.demo.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_request")
public class ServiceRequest {

	@Id
	private String id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private RequestCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private RequestStatus status;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "submitted_by_id", nullable = false)
	private UserAccount submittedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to_id")
	private UserAccount assignedTo;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("changedAt ASC")
	private List<StatusHistory> history = new ArrayList<>();

	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt ASC")
	private List<Comment> comments = new ArrayList<>();

	protected ServiceRequest() {
	}

	public ServiceRequest(
			String id,
			String title,
			String description,
			RequestCategory category,
			RequestStatus status,
			UserAccount submittedBy,
			Instant createdAt
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.category = category;
		this.status = status;
		this.submittedBy = submittedBy;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public RequestCategory getCategory() {
		return category;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public UserAccount getSubmittedBy() {
		return submittedBy;
	}

	public UserAccount getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(UserAccount assignedTo) {
		this.assignedTo = assignedTo;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void touch(Instant when) {
		this.updatedAt = when;
	}

	public List<StatusHistory> getHistory() {
		return history;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void addHistory(StatusHistory entry) {
		history.add(entry);
		entry.setRequest(this);
	}

	public void addComment(Comment comment) {
		comments.add(comment);
		comment.setRequest(this);
	}
}
