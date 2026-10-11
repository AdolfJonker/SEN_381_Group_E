package com.example.demo.web;

import com.example.demo.domain.UserAccount;
import com.example.demo.service.ServiceRequestService;
import com.example.demo.web.dto.RequestDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ServiceRequestController {

	private final ServiceRequestService service;
	private final CurrentUserResolver currentUser;

	public ServiceRequestController(ServiceRequestService service, CurrentUserResolver currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	@GetMapping("/requests")
	public List<RequestDtos.RequestDto> list(
			Authentication authentication
	) {
		UserAccount user = currentUser.require(authentication);
		return service.listFor(user);
	}

	@GetMapping("/requests/{id}")
	public RequestDtos.RequestDto get(
			@PathVariable String id,
			Authentication authentication
	) {
		return service.get(id, currentUser.require(authentication));
	}

	@PostMapping("/requests")
	public RequestDtos.RequestDto create(
			@Valid @RequestBody RequestDtos.CreateRequest body,
			Authentication authentication
	) {
		return service.create(currentUser.require(authentication), body);
	}

	@PostMapping("/requests/{id}/assign")
	public RequestDtos.RequestDto assign(
			@PathVariable String id,
			Authentication authentication
	) {
		return service.assign(id, currentUser.require(authentication));
	}

	@PostMapping("/requests/{id}/status")
	public RequestDtos.RequestDto transition(
			@PathVariable String id,
			@Valid @RequestBody RequestDtos.StatusUpdate body,
			Authentication authentication
	) {
		return service.transition(id, currentUser.require(authentication), body);
	}

	@PostMapping("/requests/{id}/comments")
	public RequestDtos.RequestDto comment(
			@PathVariable String id,
			@Valid @RequestBody RequestDtos.CommentCreate body,
			Authentication authentication
	) {
		return service.addComment(id, currentUser.require(authentication), body);
	}

	@GetMapping("/summary")
	public RequestDtos.SummaryDto summary(
			Authentication authentication
	) {
		return service.summary(currentUser.require(authentication));
	}
}
