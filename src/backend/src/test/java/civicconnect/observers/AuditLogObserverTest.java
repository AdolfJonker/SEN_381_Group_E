package civicconnect.observers;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.StatusHistory;
import civicconnect.records.User;
import civicconnect.repositories.StatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogObserverTest {

    @Mock
    private StatusHistoryRepository historyRepository;

    @DisplayName("TC-E-24 one history row records from, to, who, when and the note (NFR004)")
    @Test
    void recordsOneRowWithAllFields() {
        AuditLogObserver observer = new AuditLogObserver(historyRepository);
        User actor = new User();
        actor.setId(5L);
        actor.setRole(Role.Staff);
        ServiceRequest request = new ServiceRequest();
        request.setId(41L);
        when(historyRepository.save(any(StatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        StatusHistory row = observer.record(
                request, RequestStatus.Assigned, RequestStatus.InProgress, actor, "Started work");

        verify(historyRepository, times(1)).save(any(StatusHistory.class));
        assertEquals(request, row.getRequest());
        assertEquals(RequestStatus.Assigned, row.getFromStatus());
        assertEquals(RequestStatus.InProgress, row.getToStatus());
        assertEquals(actor, row.getChangedBy());
        assertEquals("Started work", row.getNote());
        assertNotNull(row.getChangedAt());
    }
}
