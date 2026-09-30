package com.example.demo.web.dto;

import com.example.demo.domain.Role;
import com.example.demo.domain.UserAccount;

public record UserDto(String id, String name, String email, Role role) {
	public static UserDto from(UserAccount user) {
		return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
	}
}
