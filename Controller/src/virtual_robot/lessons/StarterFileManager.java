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

        String subfolderName = "lesson_" + lesson.id;
        Path targetDir    = TEAM_CODE_SRC.resolve(subfolderName);
        String targetPkg  = "org.firstinspires.ftc.teamcode." + subfolderName;

        if (Files.exists(targetDir)) return true;

        Path starterDir = STARTERS_DIR.resolve(lesson.id);
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
                Files.write(targetDir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean lessonFolderExists(Lesson lesson) {
        return Files.exists(TEAM_CODE_SRC.resolve("lesson_" + lesson.id));
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
        // Remove trailing extra newline introduced by split
        String result = sb.toString();
        if (result.endsWith("\n\n") && !content.endsWith("\n\n")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
