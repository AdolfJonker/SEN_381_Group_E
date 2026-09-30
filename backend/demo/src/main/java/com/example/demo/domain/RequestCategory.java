package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestCategory {
	FACILITY_FAULT("Facility Fault"),
	DAMAGED_EQUIPMENT("Damaged Equipment"),
	SECURITY_CONCERN("Security Concern"),
	IT_SUPPORT("IT Support"),
	MAINTENANCE("Maintenance"),
	LOST_PROPERTY("Lost Property"),
	OTHER("Other");

	private final String label;

	RequestCategory(String label) {
		this.label = label;
	}

	@JsonValue
	public String getLabel() {
		return label;
	}

	@JsonCreator
	public static RequestCategory fromLabel(String value) {
		for (RequestCategory category : values()) {
			if (category.label.equalsIgnoreCase(value) || category.name().equalsIgnoreCase(value)) {
				return category;
			}
		}
		throw new IllegalArgumentException("Unknown category: " + value);
	}
}
