package com.example.demo.repo;

import com.example.demo.domain.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, String> {

	List<ServiceRequest> findBySubmittedByIdOrderByCreatedAtDesc(String submittedById);

	List<ServiceRequest> findAllByOrderByCreatedAtDesc();

	@Query("""
			select sr from ServiceRequest sr
			left join fetch sr.submittedBy
			left join fetch sr.assignedTo
			where sr.id = :id
			""")
	Optional<ServiceRequest> findDetailedById(String id);
}
