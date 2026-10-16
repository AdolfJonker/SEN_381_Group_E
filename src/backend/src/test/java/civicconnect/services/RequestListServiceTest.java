package civicconnect.services;

import civicconnect.records.Role;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import civicconnect.strategies.FilterStrategy;
import civicconnect.strategies.ManagementFilterStrategy;
import civicconnect.strategies.RequesterFilterStrategy;
import civicconnect.strategies.StaffFilterStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**TC-D-04: the service picks the right strategy for each role (FR005, ADR-DESIGN-002).*/
class RequestListServiceTest {

    private final ServiceRequestRepository requests = mock(ServiceRequestRepository.class);

    private RequestListService serviceWith(List<FilterStrategy> strategies) {
        return new RequestListService(strategies, mock(UserRepository.class), requests);
    }

    @Test
    @DisplayName("TC-D-04a each role resolves to its own strategy")
    void eachRoleGetsItsStrategy() {
        RequestListService service = serviceWith(List.of(
                new ManagementFilterStrategy(requests),
                new RequesterFilterStrategy(requests),
                new StaffFilterStrategy(requests)
        ));

        assertInstanceOf(RequesterFilterStrategy.class, service.strategyFor(Role.Requester));
        assertInstanceOf(StaffFilterStrategy.class, service.strategyFor(Role.Staff));
        assertInstanceOf(ManagementFilterStrategy.class, service.strategyFor(Role.Management));
    }

    @Test
    @DisplayName("TC-D-04b a role with no strategy is refused (fail closed)")
    void missingStrategyIsForbidden() {
        RequestListService service = serviceWith(List.of(new RequesterFilterStrategy(requests)));

        ResponseStatusException staff = assertThrows(ResponseStatusException.class,
                () -> service.strategyFor(Role.Staff));
        assertEquals(HttpStatus.FORBIDDEN.value(), staff.getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.strategyFor(null));
    }

    @Test
    @DisplayName("TC-D-04c two strategies for one role stop the app starting")
    void duplicateStrategyIsRejected() {
        assertThrows(IllegalStateException.class, () -> serviceWith(List.of(
                new RequesterFilterStrategy(requests),
                new RequesterFilterStrategy(requests)
        )));
    }
}
