package virtual_robot.lessons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LessonProgress {
    private final Set<String> completed = new HashSet<>();
    private final Map<String, Integer> currentStep = new HashMap<>();

    private static final Path PROGRESS_DIR  = Paths.get(System.getProperty("user.home"), ".thepit");
    private static final Path PROGRESS_FILE = PROGRESS_DIR.resolve("progress.json");

    public static LessonProgress load() {
        LessonProgress p = new LessonProgress();
        if (!Files.exists(PROGRESS_FILE)) return p;
        try {
            p.fromJson(new String(Files.readAllBytes(PROGRESS_FILE), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    @SuppressWarnings("unchecked")
    private void fromJson(String json) {
        Map<String, Object> root = (Map<String, Object>) JsonParser.parse(json);
        if (root.containsKey("completed")) {
            for (Object id : (List<Object>) root.get("completed")) completed.add((String) id);
        }
        if (root.containsKey("inProgress")) {
            Map<String, Object> ip = (Map<String, Object>) root.get("inProgress");
            for (Map.Entry<String, Object> e : ip.entrySet()) {
                currentStep.put(e.getKey(), (int)(double)(Double) e.getValue());
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(PROGRESS_DIR);
            StringBuilder sb = new StringBuilder("{\n  \"completed\": [");
            String[] ids = completed.toArray(new String[0]);
            for (int i = 0; i < ids.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append('"').append(ids[i]).append('"');
            }
            sb.append("],\n  \"inProgress\": {");
            String[] keys = currentStep.keySet().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) sb.append(',');
                sb.append("\n    \"").append(keys[i]).append("\": ").append(currentStep.get(keys[i]));
            }
            if (keys.length > 0) sb.append('\n');
            sb.append("  }\n}");
            Files.write(PROGRESS_FILE, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCompleted(String lessonId) { return completed.contains(lessonId); }

    public void markCompleted(String lessonId) {
        completed.add(lessonId);
        currentStep.remove(lessonId);
        save();
    }

    public int getCurrentStep(String lessonId) {
        return currentStep.getOrDefault(lessonId, 0);
    }

    public void setCurrentStep(String lessonId, int step) {
        currentStep.put(lessonId, step);
        save();
    }

    public boolean isLessonUnlocked(List<Topic> topics, Topic topic, Lesson lesson) {
        for (Topic t : topics) {
            if (t == topic) break;
            for (Lesson l : t.lessons) {
                if (!isCompleted(l.id)) return false;
            }
        }
        for (Lesson l : topic.lessons) {
            if (l == lesson) return true;
            if (!isCompleted(l.id)) return false;
        }
        return true;
    }
}
