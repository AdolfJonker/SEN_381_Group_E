package com.example.demo.web;

import com.example.demo.domain.UserAccount;
import com.example.demo.service.ServiceRequestService;
import com.example.demo.web.dto.RequestDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		UserAccount user = currentUser.require(userId);
		return service.listFor(user);
	}

	@GetMapping("/requests/{id}")
	public RequestDtos.RequestDto get(
			@PathVariable String id,
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.get(id, currentUser.require(userId));
	}

	@PostMapping("/requests")
	public RequestDtos.RequestDto create(
			@Valid @RequestBody RequestDtos.CreateRequest body,
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.create(currentUser.require(userId), body);
	}

	@PostMapping("/requests/{id}/assign")
	public RequestDtos.RequestDto assign(
			@PathVariable String id,
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.assign(id, currentUser.require(userId));
	}

	@PostMapping("/requests/{id}/status")
	public RequestDtos.RequestDto transition(
			@PathVariable String id,
			@Valid @RequestBody RequestDtos.StatusUpdate body,
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.transition(id, currentUser.require(userId), body);
	}

	@PostMapping("/requests/{id}/comments")
	public RequestDtos.RequestDto comment(
			@PathVariable String id,
			@Valid @RequestBody RequestDtos.CommentCreate body,
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.addComment(id, currentUser.require(userId), body);
	}

	@GetMapping("/summary")
	public RequestDtos.SummaryDto summary(
			@RequestHeader(value = CurrentUserResolver.USER_HEADER, required = false) String userId
	) {
		return service.summary(currentUser.require(userId));
	}
}
