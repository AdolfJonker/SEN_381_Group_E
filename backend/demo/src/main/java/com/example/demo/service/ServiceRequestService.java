package com.example.demo.service;

import com.example.demo.domain.Comment;
import com.example.demo.domain.RequestStatus;
import com.example.demo.domain.Role;
import com.example.demo.domain.ServiceRequest;
import com.example.demo.domain.StatusHistory;
import com.example.demo.domain.UserAccount;
import com.example.demo.repo.ServiceRequestRepository;
import com.example.demo.web.dto.RequestDtos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServiceRequestService {

	private static final int OVERDUE_DAYS = 7;

	private final ServiceRequestRepository requests;
	private final AtomicInteger requestSeq = new AtomicInteger(1000);

	public ServiceRequestService(ServiceRequestRepository requests) {
		this.requests = requests;
	}

	@Transactional(readOnly = true)
	public List<RequestDtos.RequestDto> listFor(UserAccount user) {
		List<ServiceRequest> rows = switch (user.getRole()) {
			case Requester -> requests.findBySubmittedByIdOrderByCreatedAtDesc(user.getId());
			case Staff, Management -> requests.findAllByOrderByCreatedAtDesc();
		};
		return rows.stream().map(RequestDtos.RequestDto::summary).toList();
	}

	@Transactional(readOnly = true)
	public RequestDtos.RequestDto get(String id, UserAccount user) {
		ServiceRequest request = loadVisible(id, user);
		return RequestDtos.RequestDto.from(request);
	}

	@Transactional
	public RequestDtos.RequestDto create(UserAccount user, RequestDtos.CreateRequest body) {
		if (user.getRole() != Role.Requester) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only requesters can submit requests");
		}
		Instant now = Instant.now();
		String id = nextId();
		ServiceRequest request = new ServiceRequest(
				id,
				body.title().trim(),
				body.description().trim(),
				body.category(),
				RequestStatus.Open,
				user,
				now
		);
		request.addHistory(new StatusHistory(
				id("h"),
				null,
				RequestStatus.Open,
				user,
				now,
				"Request submitted"
		));
		return RequestDtos.RequestDto.from(requests.save(request));
	}

	@Transactional
	public RequestDtos.RequestDto assign(String id, UserAccount user) {
		requireStaff(user);
		ServiceRequest request = load(id);
		if (request.getStatus() != RequestStatus.Open) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only Open requests can be assigned");
		}
		Instant now = Instant.now();
		request.setAssignedTo(user);
		request.setStatus(RequestStatus.Assigned);
		request.touch(now);
		request.addHistory(new StatusHistory(
				id("h"),
				RequestStatus.Open,
				RequestStatus.Assigned,
				user,
				now,
				"Accepted by staff"
		));
		return RequestDtos.RequestDto.from(request);
	}

	@Transactional
	public RequestDtos.RequestDto transition(String id, UserAccount user, RequestDtos.StatusUpdate body) {
		requireStaff(user);
		ServiceRequest request = load(id);
		RequestStatus next = body.toStatus();
		if (!request.getStatus().canTransitionTo(next)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Illegal transition from " + request.getStatus() + " to " + next
			);
		}
		Instant now = Instant.now();
		RequestStatus previous = request.getStatus();
		request.setStatus(next);
		request.touch(now);
		request.addHistory(new StatusHistory(
				id("h"),
				previous,
				next,
				user,
				now,
				body.note()
		));
		return RequestDtos.RequestDto.from(request);
	}

	@Transactional
	public RequestDtos.RequestDto addComment(String id, UserAccount user, RequestDtos.CommentCreate body) {
		requireStaff(user);
		ServiceRequest request = load(id);
		Instant now = Instant.now();
		request.addComment(new Comment(id("c"), user, body.content().trim(), now));
		request.touch(now);
		return RequestDtos.RequestDto.from(request);
	}

	@Transactional(readOnly = true)
	public RequestDtos.SummaryDto summary(UserAccount user) {
		if (user.getRole() != Role.Management) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Management only");
		}
		List<ServiceRequest> all = requests.findAll();
		long open = all.stream().filter(r -> r.getStatus() == RequestStatus.Open).count();
		long assigned = all.stream().filter(r -> r.getStatus() == RequestStatus.Assigned).count();
		long inProgress = all.stream().filter(r -> r.getStatus() == RequestStatus.InProgress).count();
		long resolved = all.stream().filter(r -> r.getStatus() == RequestStatus.Resolved).count();
		long closed = all.stream().filter(r -> r.getStatus() == RequestStatus.Closed).count();
		Instant cutoff = Instant.now().minus(OVERDUE_DAYS, ChronoUnit.DAYS);
		long overdue = all.stream()
				.filter(r -> r.getStatus() != RequestStatus.Resolved && r.getStatus() != RequestStatus.Closed)
				.filter(r -> r.getCreatedAt().isBefore(cutoff))
				.count();
		return new RequestDtos.SummaryDto(open, assigned, inProgress, resolved, closed, overdue);
	}

	public void syncSequenceFromExisting() {
		requests.findAll().stream()
				.map(ServiceRequest::getId)
				.filter(id -> id.startsWith("SR-"))
				.map(id -> id.substring(3))
				.mapToInt(value -> {
					try {
						return Integer.parseInt(value);
					}
					catch (NumberFormatException ex) {
						return 0;
					}
				})
				.max()
				.ifPresent(requestSeq::set);
	}

	private ServiceRequest loadVisible(String id, UserAccount user) {
		ServiceRequest request = load(id);
		if (user.getRole() == Role.Requester && !request.getSubmittedBy().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
		}
		return request;
	}

	private ServiceRequest load(String id) {
		return requests.findDetailedById(id)
				.orElseGet(() -> requests.findById(id)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found")));
	}

	private void requireStaff(UserAccount user) {
		if (user.getRole() != Role.Staff) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff only");
		}
	}

	private String nextId() {
		return "SR-" + requestSeq.incrementAndGet();
	}

	private static String id(String prefix) {
		return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
	}
}
