package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "status_history")
public class StatusHistory {

	@Id
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false)
	private ServiceRequest request;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 32)
	private RequestStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 32)
	private RequestStatus toStatus;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by_id", nullable = false)
	private UserAccount changedBy;

	@Column(name = "changed_at", nullable = false)
	private Instant changedAt;

	@Column(length = 1000)
	private String note;

	protected StatusHistory() {
	}

	public StatusHistory(
			String id,
			RequestStatus fromStatus,
			RequestStatus toStatus,
			UserAccount changedBy,
			Instant changedAt,
			String note
	) {
		this.id = id;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.changedBy = changedBy;
		this.changedAt = changedAt;
		this.note = note;
	}

	public String getId() {
		return id;
	}

	public ServiceRequest getRequest() {
		return request;
	}

	void setRequest(ServiceRequest request) {
		this.request = request;
	}

	public RequestStatus getFromStatus() {
		return fromStatus;
	}

	public RequestStatus getToStatus() {
		return toStatus;
	}

	public UserAccount getChangedBy() {
		return changedBy;
	}

	public Instant getChangedAt() {
		return changedAt;
	}

	public String getNote() {
		return note;
	}
}
