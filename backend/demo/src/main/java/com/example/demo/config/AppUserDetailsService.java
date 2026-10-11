package com.example.demo.config;

import com.example.demo.domain.UserAccount;
import com.example.demo.repo.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a CivicConnect user by email for Spring Security.
 * The user's single Role becomes the authority ROLE_<Role>, e.g. ROLE_Management.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

	private final UserAccountRepository users;

	public AppUserDetailsService(UserAccountRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		UserAccount account = users.findByEmailIgnoreCase(email)
				// Same message for "unknown email" and "wrong password" (no user enumeration)
				.orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
		return User.withUsername(account.getEmail())
				.password(account.getPasswordHash())
				.roles(account.getRole().name())
				.build();
	}
}
