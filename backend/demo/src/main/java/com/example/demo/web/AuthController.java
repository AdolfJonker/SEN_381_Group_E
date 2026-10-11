package com.example.demo.web;

import com.example.demo.web.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login check for the React login screen.
 * The browser sends email + password with HTTP Basic. If they are valid,
 * this returns the user's id, name, email and role so the UI can pick the
 * correct landing view. If they are not valid, Spring Security returns 401
 * before this method runs.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final CurrentUserResolver currentUser;

	public AuthController(CurrentUserResolver currentUser) {
		this.currentUser = currentUser;
	}

	@GetMapping("/me")
	public UserDto me(Authentication authentication) {
		return UserDto.from(currentUser.require(authentication));
	}
}
