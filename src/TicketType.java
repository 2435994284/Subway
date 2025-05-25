public enum TicketType {
    SINGLE_JOURNEY("单程票"),
    WUHAN_TONG("武汉通(9折)"),
    ONE_DAY_PASS("1日票(18元)"),
    THREE_DAY_PASS("3日票(45元)"),
    SEVEN_DAY_PASS("7日票(90元)");

    private final String description;

    TicketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
