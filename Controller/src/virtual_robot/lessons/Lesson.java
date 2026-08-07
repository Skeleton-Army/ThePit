package virtual_robot.lessons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lesson {
    public final String id;
    public final String title;
    public final String topicId;
    public final String starterOpMode;
    public final List<String> starterFiles;
    public final List<LessonStep> steps;

    private Lesson(String id, String title, String topicId, String starterOpMode,
                   List<String> starterFiles, List<LessonStep> steps) {
        this.id = id;
        this.title = title;
        this.topicId = topicId;
        this.starterOpMode = starterOpMode;
        this.starterFiles = starterFiles;
        this.steps = steps;
    }

    @SuppressWarnings("unchecked")
    public static Lesson fromMap(Map<String, Object> map, String topicId) {
        String id    = (String) map.get("id");
        String title = (String) map.get("title");
        String starterOpMode = (String) map.getOrDefault("starterOpMode", null);

        List<String> starterFiles = new ArrayList<>();
        if (map.containsKey("starterFiles")) {
            for (Object f : (List<Object>) map.get("starterFiles")) {
                starterFiles.add((String) f);
            }
        }

        List<LessonStep> steps = new ArrayList<>();
        if (map.containsKey("steps")) {
            for (Object stepObj : (List<Object>) map.get("steps")) {
                steps.add(LessonStep.fromMap((Map<String, Object>) stepObj));
            }
        }
        return new Lesson(id, title, topicId, starterOpMode, starterFiles, steps);
    }
}
