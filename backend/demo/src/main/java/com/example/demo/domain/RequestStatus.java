package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Map;

public enum RequestStatus {
	Open,
	Assigned,
	InProgress,
	Resolved,
	Closed;

	private static final Map<RequestStatus, List<RequestStatus>> TRANSITIONS = Map.of(
			Open, List.of(Assigned),
			Assigned, List.of(InProgress),
			InProgress, List.of(Resolved),
			Resolved, List.of(Closed),
			Closed, List.of()
	);

	@JsonValue
	public String json() {
		return name();
	}

	@JsonCreator
	public static RequestStatus from(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Status is required");
		}
		String normalized = value.replace(" ", "");
		for (RequestStatus status : values()) {
			if (status.name().equalsIgnoreCase(normalized)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown status: " + value);
	}

	public boolean canTransitionTo(RequestStatus next) {
		return TRANSITIONS.getOrDefault(this, List.of()).contains(next);
	}
}
