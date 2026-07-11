package virtual_robot.lessons;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import virtual_robot.controller.VirtualRobotController;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Watches the TeamCode source directory for .java file changes, compiles them on the fly,
 * and reloads the compiled OpModes into the simulator's combo box automatically.
 */
public class OpModeHotReloader {

    private static final Path TEAMCODE_SRC = Paths.get("TeamCode", "src");
    private static final Path TEAMCODE_OUT = Paths.get("out", "production", "TeamCode");

    private final VirtualRobotController controller;
    private final ComboBox<Class<?>> cbxOpModes;
    private final Consumer<ClassLoader> onNewClassLoader;
    
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "opmode-hot-reloader");
                t.setDaemon(true);
                return t;
            });

    private volatile URLClassLoader teamCodeLoader;
    private volatile long lastJavaModifiedSnapshot = -1;
    private volatile long lastClassModifiedSnapshot = -1;

    public OpModeHotReloader(VirtualRobotController controller,
                             Consumer<ClassLoader> onNewClassLoader) {
        this.controller       = controller;
        this.cbxOpModes       = controller.getOpModeComboBox();
        this.onNewClassLoader = onNewClassLoader;
    }

    /** Starts the background polling loop. */
    public void start() {
        if (!Files.exists(TEAMCODE_SRC)) {
            System.out.println("[HotReload] TeamCode source dir not found: "
                    + TEAMCODE_SRC.toAbsolutePath() + " — auto-compile disabled.");
            return;
        }

        try {
            Files.createDirectories(TEAMCODE_OUT);
        } catch (IOException e) {
            System.err.println("[HotReload] Failed to create output directory: " + e.getMessage());
        }

        // Take initial snapshots so we don't trigger recompilation on launch
        lastJavaModifiedSnapshot = latestJavaModified();
        lastClassModifiedSnapshot = latestClassModified();

        scheduler.scheduleWithFixedDelay(this::pollAndReload, 1, 1, TimeUnit.SECONDS);
        System.out.println("[HotReload] Watching Java sources in: " + TEAMCODE_SRC.toAbsolutePath());
    }

    /** Stops the watcher. */
    public void stop() {
        scheduler.shutdownNow();
        closeLoader(teamCodeLoader);
    }

    // ─── Polling & Live compilation ──────────────────────────────────────────

    private void pollAndReload() {
        // Skip hot reloading completely if an OpMode is currently running or initialized
        if (controller.getOpModeInitialized()) {
            return;
        }

        boolean recompiled = false;

        // 1. Check for Java file changes and compile them
        long currentJavaMod = latestJavaModified();
        if (currentJavaMod > lastJavaModifiedSnapshot) {
            lastJavaModifiedSnapshot = currentJavaMod;
            System.out.println("[HotReload] Java source change detected. Auto-compiling...");
            
            // Turn on the reloading visual lock / disable START
            controller.setHotReloading(true);
            
            try {
                boolean success = compileJavaFiles();
                if (!success) {
                    System.err.println("[HotReload] Auto-compilation failed! Fix errors in your code.");
                    return;
                }
                System.out.println("[HotReload] Auto-compilation successful.");
                recompiled = true;
            } finally {
                // If we didn't end up reloading classes, release the compile lock
                if (!recompiled) {
                    controller.setHotReloading(false);
                }
            }
        }

        // 2. Check compiled class files for changes and reload
        long currentClassMod = latestClassModified();
        if (currentClassMod > lastClassModifiedSnapshot) {
            lastClassModifiedSnapshot = currentClassMod;
            System.out.println("[HotReload] Class changes detected — reloading OpModes...");
            
            // Ensure flag is set in case class compile happened externally (e.g. IDE built it)
            controller.setHotReloading(true);
            
            try {
                reload();
            } catch (Exception e) {
                System.err.println("[HotReload] Reload failed: " + e.getMessage());
            } finally {
                controller.setHotReloading(false);
            }
        }
    }

    private long latestJavaModified() {
        return latestFileModified(TEAMCODE_SRC, ".java");
    }

    private long latestClassModified() {
        return latestFileModified(TEAMCODE_OUT, ".class");
    }

    private long latestFileModified(Path root, String extension) {
        if (!Files.exists(root)) return 0L;
        try (java.util.stream.Stream<Path> s = Files.walk(root)) {
            return s.filter(p -> p.toString().endsWith(extension))
                    .mapToLong(p -> {
                        try { return Files.getLastModifiedTime(p).toMillis(); }
                        catch (IOException e) { return 0L; }
                    })
                    .max()
                    .orElse(0L);
        } catch (IOException e) {
            return 0L;
        }
    }

    // ─── Java Compiler API invocation with Process fallback ────────────────

    private boolean compileJavaFiles() {
        List<File> javaFiles;
        try (java.util.stream.Stream<Path> s = Files.walk(TEAMCODE_SRC)) {
            javaFiles = s.filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("[HotReload] Failed to scan Java files: " + e.getMessage());
            return false;
        }

        if (javaFiles.isEmpty()) return true;

        String classpath = System.getProperty("java.class.path");
        String outputDir = TEAMCODE_OUT.toAbsolutePath().toString();

        // 1. Try in-process compilation using JavaCompiler API
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null) {
            try {
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
                Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
                List<String> options = Arrays.asList("-d", outputDir, "-classpath", classpath);
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, units);
                boolean success = task.call();
                fileManager.close();
                return success;
            } catch (Exception e) {
                System.err.println("[HotReload] In-process compilation error, falling back: " + e.getMessage());
            }
        }

        // 2. Fallback to external process javac invocation
        return compileExternally(javaFiles, classpath, outputDir);
    }

    private boolean compileExternally(List<File> javaFiles, String classpath, String outputDir) {
        try {
            Path javaHome = Paths.get(System.getProperty("java.home"));
            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            String exeName = isWin ? "javac.exe" : "javac";
            Path javacPath = javaHome.resolve("bin").resolve(exeName);

            // Look in parent dir in case java.home is JRE nested in JDK
            if (!Files.exists(javacPath) && javaHome.getParent() != null) {
                javacPath = javaHome.getParent().resolve("bin").resolve(exeName);
            }

            String javacCmd = Files.exists(javacPath) ? javacPath.toAbsolutePath().toString() : "javac";

            List<String> cmd = new ArrayList<>();
            cmd.add(javacCmd);
            cmd.add("-d");
            cmd.add(outputDir);
            cmd.add("-classpath");
            cmd.add(classpath);
            for (File f : javaFiles) {
                cmd.add(f.getAbsolutePath());
            }

            Process p = new ProcessBuilder(cmd).start();
            boolean finished = p.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("[HotReload] External compiler timed out!");
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (Exception e) {
            System.err.println("[HotReload] External compiler invocation failed: " + e.getMessage());
            return false;
        }
    }

    // ─── Class Loading & ComboBox Updates ────────────────────────────────────

    private void reload() throws Exception {
        URLClassLoader oldLoader = teamCodeLoader;
        URL outputUrl = TEAMCODE_OUT.toUri().toURL();

        /*
         * Child-first URLClassLoader for org.firstinspires.ftc.teamcode
         */
        URLClassLoader newLoader = new URLClassLoader(
                new URL[]{outputUrl},
                Thread.currentThread().getContextClassLoader()) {

            @Override
            protected Class<?> loadClass(String name, boolean resolve)
                    throws ClassNotFoundException {
                if (name.startsWith("org.firstinspires.ftc.teamcode")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> cached = findLoadedClass(name);
                        if (cached != null) return cached;
                        try {
                            Class<?> c = findClass(name);
                            if (resolve) resolveClass(c);
                            return c;
                        } catch (ClassNotFoundException ignored) {
                            // Fallback to parent
                        }
                    }
                }
                return super.loadClass(name, resolve);
            }
        };
        teamCodeLoader = newLoader;

        // Scan using the new loader
        List<Class<?>> found = scanOpModes(newLoader);

        // Switch to JavaFX thread to update combo box
        Platform.runLater(() -> {
            updateComboBox(found);
            if (onNewClassLoader != null) onNewClassLoader.accept(newLoader);
        });

        // Close old loader after delay
        if (oldLoader != null) {
            scheduler.schedule(() -> closeLoader(oldLoader), 2, TimeUnit.SECONDS);
        }
        System.out.println("[HotReload] Loaded " + found.size() + " OpMode(s) from hot reload classloader.");
    }

    private List<Class<?>> scanOpModes(URLClassLoader loader) {
        List<Class<?>> result = new ArrayList<>();
        try (java.util.stream.Stream<Path> s = Files.walk(TEAMCODE_OUT)) {
            s.filter(p -> p.toString().endsWith(".class"))
             .forEach(p -> {
                 String rel = TEAMCODE_OUT.relativize(p).toString()
                         .replace('/', '.').replace('\\', '.')
                         .replace(".class", "");
                 try {
                     Class<?> c = loader.loadClass(rel);
                     if (c.getAnnotation(Disabled.class) != null) return;
                     if (!OpMode.class.isAssignableFrom(c)) return;
                     if (c.getAnnotation(TeleOp.class) != null
                             || c.getAnnotation(Autonomous.class) != null) {
                         result.add(c);
                     }
                 } catch (Throwable ignored) {}
             });
        } catch (IOException e) {
            System.err.println("[HotReload] Scan error: " + e.getMessage());
        }
        return result;
    }

    private void updateComboBox(List<Class<?>> classes) {
        classes.sort(Comparator.comparing(c -> {
            TeleOp t = c.getAnnotation(TeleOp.class);
            if (t != null) return t.group();
            Autonomous a = c.getAnnotation(Autonomous.class);
            return a != null ? a.group() : "";
        }));

        Class<?> prev = cbxOpModes.getValue();
        ObservableList<Class<?>> items = FXCollections.observableArrayList(classes);
        cbxOpModes.setItems(items);

        if (prev != null) {
            String prevName = prev.getName();
            items.stream()
                    .filter(c -> c.getName().equals(prevName))
                    .findFirst()
                    .ifPresent(cbxOpModes::setValue);
        }
        if (cbxOpModes.getValue() == null && !items.isEmpty()) {
            cbxOpModes.setValue(items.get(0));
        }
        System.out.println("[HotReload] ComboBox updated with " + classes.size() + " OpMode(s).");
    }

    private static void closeLoader(URLClassLoader loader) {
        if (loader == null) return;
        try { loader.close(); } catch (IOException ignored) {}
    }
}
