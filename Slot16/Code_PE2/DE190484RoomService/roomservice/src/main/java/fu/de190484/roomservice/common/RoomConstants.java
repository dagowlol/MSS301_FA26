package fu.de190484.roomservice.common;

public final class RoomConstants {
    private RoomConstants() {}

    public static final String AVAILABLE = "AVAILABLE";
    public static final String OCCUPIED = "OCCUPIED";
    public static final String MAINTENANCE = "MAINTENANCE";

    // Regex for validation
    public static final String TYPE_REGEX = "SINGLE|DOUBLE|SUITE|DELUXE";
    public static final String STATUS_REGEX = "AVAILABLE|OCCUPIED|MAINTENANCE";
}
