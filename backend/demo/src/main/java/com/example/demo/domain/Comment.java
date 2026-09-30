package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "request_comment")
public class Comment {

	@Id
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false)
	private ServiceRequest request;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private UserAccount author;

	@Column(nullable = false, length = 4000)
	private String content;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected Comment() {
	}

	public Comment(String id, UserAccount author, String content, Instant createdAt) {
		this.id = id;
		this.author = author;
		this.content = content;
		this.createdAt = createdAt;
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

	public UserAccount getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
