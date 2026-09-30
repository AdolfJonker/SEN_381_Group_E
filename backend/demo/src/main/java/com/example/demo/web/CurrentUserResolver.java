package com.example.demo.web;

import com.example.demo.domain.UserAccount;
import com.example.demo.repo.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUserResolver {

	public static final String USER_HEADER = "X-User-Id";

	private final UserAccountRepository users;

	public CurrentUserResolver(UserAccountRepository users) {
		this.users = users;
	}

	public UserAccount require(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing " + USER_HEADER + " header");
		}
		return users.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
	}
}
