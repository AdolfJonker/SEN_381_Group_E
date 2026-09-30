package com.example.demo.web.dto;

import com.example.demo.domain.Comment;
import com.example.demo.domain.RequestCategory;
import com.example.demo.domain.RequestStatus;
import com.example.demo.domain.ServiceRequest;
import com.example.demo.domain.StatusHistory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class RequestDtos {

	private RequestDtos() {
	}

	public record CreateRequest(
			@NotBlank @Size(max = 200) String title,
			@NotBlank @Size(max = 4000) String description,
			@NotNull RequestCategory category
	) {
	}

	public record StatusUpdate(
			@NotNull RequestStatus toStatus,
			@Size(max = 1000) String note
	) {
	}

	public record CommentCreate(
			@NotBlank @Size(max = 4000) String content
	) {
	}

	public record HistoryDto(
			String id,
			RequestStatus fromStatus,
			RequestStatus toStatus,
			String changedById,
			Instant timestamp,
			String note
	) {
		public static HistoryDto from(StatusHistory history) {
			return new HistoryDto(
					history.getId(),
					history.getFromStatus(),
					history.getToStatus(),
					history.getChangedBy().getId(),
					history.getChangedAt(),
					history.getNote()
			);
		}
	}

	public record CommentDto(
			String id,
			String authorId,
			String content,
			Instant createdAt
	) {
		public static CommentDto from(Comment comment) {
			return new CommentDto(
					comment.getId(),
					comment.getAuthor().getId(),
					comment.getContent(),
					comment.getCreatedAt()
			);
		}
	}

	public record RequestDto(
			String id,
			String title,
			String description,
			RequestCategory category,
			RequestStatus status,
			String submittedById,
			String assignedToId,
			Instant createdAt,
			Instant updatedAt,
			List<HistoryDto> history,
			List<CommentDto> comments
	) {
		public static RequestDto from(ServiceRequest request) {
			return new RequestDto(
					request.getId(),
					request.getTitle(),
					request.getDescription(),
					request.getCategory(),
					request.getStatus(),
					request.getSubmittedBy().getId(),
					request.getAssignedTo() == null ? null : request.getAssignedTo().getId(),
					request.getCreatedAt(),
					request.getUpdatedAt(),
					request.getHistory().stream().map(HistoryDto::from).toList(),
					request.getComments().stream().map(CommentDto::from).toList()
			);
		}

		public static RequestDto summary(ServiceRequest request) {
			return new RequestDto(
					request.getId(),
					request.getTitle(),
					request.getDescription(),
					request.getCategory(),
					request.getStatus(),
					request.getSubmittedBy().getId(),
					request.getAssignedTo() == null ? null : request.getAssignedTo().getId(),
					request.getCreatedAt(),
					request.getUpdatedAt(),
					List.of(),
					List.of()
			);
		}
	}

	public record SummaryDto(
			long open,
			long assigned,
			long inProgress,
			long resolved,
			long closed,
			long overdue
	) {
	}
}
