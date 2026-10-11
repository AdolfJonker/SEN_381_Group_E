package civicconnect.records;

public enum RequestCategory {
    FACILITY_FAULT("Facility Fault"),
    DAMAGED_EQUIPMENT("Damaged Equipment"),
    SECURITY_CONCERN("Security Concern"),
    IT_SUPPORT("IT Support"),
    MAINTENANCE("Maintenance"),
    LOST_PROPERTY("Lost Property"),
    OTHER("Other");

    private final String label;

    RequestCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RequestCategory fromLabel(String label) {
        for (RequestCategory category : values()) {
            if (category.label.equals(label)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + label);
    }
}
