package virtual_robot.lessons;

public class ContentBlock {
    public enum Type { TEXT, CODE, LINK }

    public final Type type;
    public final String value;
    public final String language;
    public final String label;
    public final String url;

    private ContentBlock(Type type, String value, String language, String label, String url) {
        this.type = type;
        this.value = value;
        this.language = language;
        this.label = label;
        this.url = url;
    }

    public static ContentBlock text(String value) {
        return new ContentBlock(Type.TEXT, value, null, null, null);
    }

    public static ContentBlock code(String language, String value) {
        return new ContentBlock(Type.CODE, value, language, null, null);
    }

    public static ContentBlock link(String label, String url) {
        return new ContentBlock(Type.LINK, null, null, label, url);
    }
}
