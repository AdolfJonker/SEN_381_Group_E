package civicconnect.api.dto;

/**
 * Optional list filters for FR006 (staff search/filter/sort) and FR013 (management filters).
 * Applied after the role FilterStrategy has scoped the rows.
 */
public record RequestListQuery(
        String search,
        String status,
        String category,
        String sort
) {
    public static RequestListQuery empty() {
        return new RequestListQuery(null, null, null, null);
    }
}
