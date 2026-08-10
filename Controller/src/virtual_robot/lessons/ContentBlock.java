package virtual_robot.lessons;

import java.util.List;

public class ContentBlock {
    public enum Type { TEXT, CODE, LINK, QUIZ }

    public final Type type;
    public final String value;
    public final String language;
    public final String label;
    public final String url;
    public final String question;
    public final List<String> options;
    public final int correctIndex;

    private ContentBlock(Type type, String value, String language, String label, String url,
                         String question, List<String> options, int correctIndex) {
        this.type = type;
        this.value = value;
        this.language = language;
        this.label = label;
        this.url = url;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public static ContentBlock text(String value) {
        return new ContentBlock(Type.TEXT, value, null, null, null, null, null, -1);
    }

    public static ContentBlock code(String language, String value) {
        return new ContentBlock(Type.CODE, value, language, null, null, null, null, -1);
    }

    public static ContentBlock link(String label, String url) {
        return new ContentBlock(Type.LINK, null, null, label, url, null, null, -1);
    }

    public static ContentBlock quiz(String question, List<String> options, int correctIndex) {
        return new ContentBlock(Type.QUIZ, null, null, null, null, question, options, correctIndex);
    }
}
