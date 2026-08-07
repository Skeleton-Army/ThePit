package virtual_robot.lessons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class StarterFileManager {
    private static final Path TEAM_CODE_SRC =
            Paths.get("TeamCode", "src", "org", "firstinspires", "ftc", "teamcode");
    private static final Path STARTERS_DIR = Paths.get("lessons", "starters");

    /**
     * Copies starter files for the lesson into TeamCode if the destination folder
     * does not already exist. Returns true on success or if already copied.
     */
    public static boolean copyStarterFiles(Lesson lesson) {
        if (lesson.starterFiles.isEmpty()) return true;

        String moduleName = moduleName(lesson.topicId);
        String subfolderName = lessonFolderName(lesson.id);
        Path targetDir    = TEAM_CODE_SRC.resolve(moduleName).resolve(subfolderName);
        String targetPkg  = "org.firstinspires.ftc.teamcode." + moduleName + "." + subfolderName;

        if (Files.exists(targetDir)) return true;

        Path starterDir = STARTERS_DIR.resolve(lesson.topicId).resolve(lesson.id);
        if (!Files.exists(starterDir)) {
            System.err.println("Starter directory not found: " + starterDir.toAbsolutePath());
            return false;
        }

        try {
            Files.createDirectories(targetDir);
            for (String filename : lesson.starterFiles) {
                Path src = starterDir.resolve(filename);
                if (!Files.exists(src)) {
                    System.err.println("Starter file not found: " + src.toAbsolutePath());
                    continue;
                }
                String content = new String(Files.readAllBytes(src), StandardCharsets.UTF_8);
                content = setPackageDeclaration(content, targetPkg);
                content = setAnnotationGroup(content, moduleName);
                Files.write(targetDir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean lessonFolderExists(Lesson lesson) {
        String moduleName = moduleName(lesson.topicId);
        return Files.exists(TEAM_CODE_SRC.resolve(moduleName).resolve(lessonFolderName(lesson.id)));
    }

    private static String lessonFolderName(String lessonId) {
        return "lesson_" + lessonId;
    }

    private static String moduleName(String topicId) {
        return topicId.replaceFirst("^\\d+_", "");
    }

    private static String setPackageDeclaration(String content, String packageName) {
        String[] lines = content.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
        for (String line : lines) {
            if (!replaced && line.trim().startsWith("package ")) {
                sb.append("package ").append(packageName).append(";\n");
                replaced = true;
            } else {
                sb.append(line).append("\n");
            }
        }
        if (!replaced) {
            sb.insert(0, "package " + packageName + ";\n\n");
        }
        String result = sb.toString();
        if (result.endsWith("\n\n") && !content.endsWith("\n\n")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String setAnnotationGroup(String content, String moduleName) {
        String displayName = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        return content.replaceAll("group\\s*=\\s*\"Lessons\"", "group = \"" + displayName + "\"");
    }
}
