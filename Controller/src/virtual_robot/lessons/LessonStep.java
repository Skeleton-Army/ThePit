package virtual_robot.lessons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LessonStep {
    public final String title;
    public final String hint;
    public final List<ContentBlock> content;
    public final CompletionCheck check;

    private LessonStep(String title, String hint, List<ContentBlock> content, CompletionCheck check) {
        this.title = title;
        this.hint = hint;
        this.content = content;
        this.check = check;
    }

    @SuppressWarnings("unchecked")
    public static LessonStep fromMap(Map<String, Object> map) {
        String title = (String) map.get("title");
        String hint  = (String) map.getOrDefault("hint", null);

        List<ContentBlock> blocks = new ArrayList<>();
        Object contentObj = map.get("content");
        if (contentObj instanceof List) {
            for (Object obj : (List<Object>) contentObj) {
                Map<String, Object> b = (Map<String, Object>) obj;
                String type = (String) b.get("type");
                switch (type) {
                    case "text":
                        blocks.add(ContentBlock.text((String) b.get("value")));
                        break;
                    case "code":
                        blocks.add(ContentBlock.code(
                                (String) b.getOrDefault("language", ""),
                                (String) b.get("value")));
                        break;
                    case "link":
                        blocks.add(ContentBlock.link(
                                (String) b.get("label"),
                                (String) b.get("url")));
                        break;
                }
            }
        }

        CompletionCheck check = null;
        if (map.containsKey("check")) {
            check = CompletionCheck.fromMap((Map<String, Object>) map.get("check"));
        }
        return new LessonStep(title, hint, blocks, check);
    }
}
