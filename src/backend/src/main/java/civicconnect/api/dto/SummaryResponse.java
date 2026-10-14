package civicconnect.api.dto;

/**
 * FR012 summary counts. The acceptance criterion needs Open, Overdue, Resolved and Closed. Assigned and InProgress are included so the counts add up to the total.
 */
public record SummaryResponse(
        long open,
        long assigned,
        long inProgress,
        long resolved,
        long closed,
        long overdue
) {
    public long total() {
        return open + assigned + inProgress + resolved + closed;
    }
}
