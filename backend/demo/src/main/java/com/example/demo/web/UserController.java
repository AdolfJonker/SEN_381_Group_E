package com.example.demo.web;

import com.example.demo.repo.UserAccountRepository;
import com.example.demo.web.dto.UserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserAccountRepository users;

	public UserController(UserAccountRepository users) {
		this.users = users;
	}

	@GetMapping
	public List<UserDto> listDemoUsers() {
		return users.findAllByOrderByNameAsc().stream().map(UserDto::from).toList();
	}
}
