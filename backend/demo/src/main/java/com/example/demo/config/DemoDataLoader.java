package com.example.demo.config;

import com.example.demo.domain.Comment;
import com.example.demo.domain.RequestCategory;
import com.example.demo.domain.RequestStatus;
import com.example.demo.domain.Role;
import com.example.demo.domain.ServiceRequest;
import com.example.demo.domain.StatusHistory;
import com.example.demo.domain.UserAccount;
import com.example.demo.repo.ServiceRequestRepository;
import com.example.demo.repo.UserAccountRepository;
import com.example.demo.service.ServiceRequestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
public class DemoDataLoader {

	@Bean
	CommandLineRunner seedDemoData(
			UserAccountRepository users,
			ServiceRequestRepository requests,
			ServiceRequestService service
	) {
		return args -> {
			if (users.count() > 0) {
				service.syncSequenceFromExisting();
				return;
			}

			UserAccount requester = users.save(new UserAccount(
					"u-req-1",
					"Aisha Ndlovu",
					"aisha@campus.edu",
					"{noop}demo",
					Role.Requester
			));
			UserAccount staff = users.save(new UserAccount(
					"u-staff-1",
					"Johan Botha",
					"johan@campus.edu",
					"{noop}demo",
					Role.Staff
			));
			users.save(new UserAccount(
					"u-mgmt-1",
					"Thandi Mokoena",
					"thandi@campus.edu",
					"{noop}demo",
					Role.Management
			));

			Instant t1 = Instant.now().minus(10, ChronoUnit.DAYS);
			ServiceRequest projector = new ServiceRequest(
					"SR-1001",
					"Broken projector in Lecture Hall B",
					"The ceiling projector flickers and shuts off after a few minutes. Affects morning lectures.",
					RequestCategory.DAMAGED_EQUIPMENT,
					RequestStatus.Assigned,
					requester,
					t1
			);
			projector.setAssignedTo(staff);
			projector.addHistory(new StatusHistory("h-1001-1", null, RequestStatus.Open, requester, t1, "Request submitted"));
			projector.addHistory(new StatusHistory(
					"h-1001-2",
					RequestStatus.Open,
					RequestStatus.Assigned,
					staff,
					t1.plus(1, ChronoUnit.DAYS),
					"Accepted by facilities AV team"
			));
			projector.addComment(new Comment(
					"c-1001-1",
					staff,
					"Spare bulb ordered; ETA Thursday.",
					t1.plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)
			));
			requests.save(projector);

			Instant t2 = Instant.now().minus(2, ChronoUnit.DAYS);
			ServiceRequest leak = new ServiceRequest(
					"SR-1002",
					"Water leak near Library entrance",
					"Puddle forming by the automatic doors after rain. Possible roof or drainage issue.",
					RequestCategory.FACILITY_FAULT,
					RequestStatus.Open,
					requester,
					t2
			);
			leak.addHistory(new StatusHistory("h-1002-1", null, RequestStatus.Open, requester, t2, "Request submitted"));
			requests.save(leak);

			Instant t3 = Instant.now().minus(12, ChronoUnit.DAYS);
			ServiceRequest vpn = new ServiceRequest(
					"SR-1003",
					"VPN access for remote lab",
					"Cannot connect to the campus VPN from home; error 809 after credentials accepted.",
					RequestCategory.IT_SUPPORT,
					RequestStatus.InProgress,
					requester,
					t3
			);
			vpn.setAssignedTo(staff);
			vpn.addHistory(new StatusHistory("h-1003-1", null, RequestStatus.Open, requester, t3, "Request submitted"));
			vpn.addHistory(new StatusHistory(
					"h-1003-2",
					RequestStatus.Open,
					RequestStatus.Assigned,
					staff,
					t3.plus(1, ChronoUnit.DAYS),
					null
			));
			vpn.addHistory(new StatusHistory(
					"h-1003-3",
					RequestStatus.Assigned,
					RequestStatus.InProgress,
					staff,
					t3.plus(5, ChronoUnit.DAYS),
					"Checking RADIUS logs"
			));
			requests.save(vpn);

			service.syncSequenceFromExisting();
		};
	}
}
