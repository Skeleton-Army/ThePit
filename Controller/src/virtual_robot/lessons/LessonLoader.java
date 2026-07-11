package virtual_robot.lessons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LessonLoader {
    static final String LESSONS_DIR = "lessons";

    public static List<Topic> loadTopics() {
        List<Topic> topics = new ArrayList<>();
        Path lessonsPath = Paths.get(LESSONS_DIR);
        if (!Files.exists(lessonsPath)) return topics;

        List<Path> topicDirs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lessonsPath)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry) && !name.equals("starters")) {
                    topicDirs.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return topics;
        }
        topicDirs.sort(Comparator.comparing(p -> p.getFileName().toString()));

        for (Path dir : topicDirs) {
            Topic t = loadTopic(dir);
            if (t != null) topics.add(t);
        }
        return topics;
    }

    @SuppressWarnings("unchecked")
    private static Topic loadTopic(Path topicDir) {
        Path topicJsonPath = topicDir.resolve("topic.json");
        if (!Files.exists(topicJsonPath)) return null;

        Map<String, Object> topicMap;
        try {
            topicMap = (Map<String, Object>) JsonParser.parse(readFile(topicJsonPath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String topicId      = topicDir.getFileName().toString();
        String title        = (String) topicMap.getOrDefault("title", topicId);
        String description  = (String) topicMap.getOrDefault("description", "");

        List<Path> lessonFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(topicDir, "*.json")) {
            for (Path f : stream) {
                if (!f.getFileName().toString().equals("topic.json")) lessonFiles.add(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        lessonFiles.sort(Comparator.comparing(p -> p.getFileName().toString()));

        List<Lesson> lessons = new ArrayList<>();
        for (Path file : lessonFiles) {
            try {
                Map<String, Object> map = (Map<String, Object>) JsonParser.parse(readFile(file));
                lessons.add(Lesson.fromMap(map));
            } catch (Exception e) {
                System.err.println("Failed to load lesson: " + file);
                e.printStackTrace();
            }
        }
        return new Topic(topicId, title, description, lessons);
    }

    static String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
