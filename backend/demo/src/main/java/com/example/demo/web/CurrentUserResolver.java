package com.example.demo.web;

import com.example.demo.domain.UserAccount;
import com.example.demo.repo.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns the authenticated principal (set by Spring Security) into a UserAccount.
 * Replaces the old X-User-Id header, which any client could forge.
 */
@Component
public class CurrentUserResolver {

	private final UserAccountRepository users;

	public CurrentUserResolver(UserAccountRepository users) {
		this.users = users;
	}

	public UserAccount require(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
		}
		return users.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
	}
}
