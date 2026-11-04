package fu.de190484.entity1service.common;

/**
 * TODO: Cập nhật regex và hằng số theo đề bài
 */
public final class Entity1Constants {
    private Entity1Constants() {}

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";

    // Regex dùng cho validation
    public static final String TYPE_REGEX = "TYPE_A|TYPE_B|TYPE_C|TYPE_D";
    public static final String STATUS_REGEX = "AVAILABLE|OCCUPIED|MAINTENANCE";
}
