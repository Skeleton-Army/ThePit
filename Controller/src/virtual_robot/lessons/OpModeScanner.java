package virtual_robot.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class OpModeScanner {
    public static Set<Class<?>> scan() {
        Reflections reflections = new Reflections("org.firstinspires.ftc.teamcode");
        Set<Class<?>> result = new HashSet<>();
        result.addAll(reflections.getTypesAnnotatedWith(TeleOp.class));
        result.addAll(reflections.getTypesAnnotatedWith(Autonomous.class));
        result.removeIf(c -> c.getAnnotation(Disabled.class) != null
                || !OpMode.class.isAssignableFrom(c));
        return result;
    }

    public static boolean hasAnyOpMode() {
        Path srcDir = Paths.get("TeamCode", "src");
        if (!Files.exists(srcDir)) return false;
        try (Stream<Path> stream = Files.walk(srcDir)) {
            return stream.filter(p -> p.toString().endsWith(".java"))
                    .anyMatch(p -> {
                        try {
                            String name = p.getFileName().toString();
                            if (name.equals("OpModeScanner.java")) return false;
                            String content = new String(Files.readAllBytes(p));
                            return content.contains("@TeleOp") || content.contains("@Autonomous");
                        } catch (IOException e) {
                            return false;
                        }
                    });
        } catch (IOException e) {
            return false;
        }
    }
}

