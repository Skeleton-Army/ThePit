package virtual_robot.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import virtual_robot.lessons.Lesson;
import virtual_robot.lessons.LessonLoader;
import virtual_robot.lessons.LessonProgress;
import virtual_robot.lessons.OpModeHotReloader;
import virtual_robot.lessons.Topic;
import virtual_robot.lessons.ui.LessonMenuScreen;
import virtual_robot.lessons.ui.LessonPanel;

import java.util.List;

/**
 * Alternative entry point that shows the lesson menu before the simulator.
 * Change the run configuration's main class to this instead of VirtualRobotApplication.
 */
public class ThePitApplication extends Application {

    private VirtualRobotController controllerHandle;
    private OpModeHotReloader hotReloader;

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<Topic> topics     = LessonLoader.loadTopics();
        LessonProgress progress = LessonProgress.load();

        LessonMenuScreen menu = new LessonMenuScreen(topics, progress);
        menu.show(primaryStage, selectedLesson ->
                launchSimulator(primaryStage, selectedLesson, topics, progress));
    }

    private void launchSimulator(Stage primaryStage, Lesson selectedLesson,
                                  List<Topic> topics, LessonProgress progress) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("virtual_robot.fxml"));
            Parent root = loader.load();
            controllerHandle = loader.getController();

            primaryStage.setTitle("Virtual Robot - The Pit");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
            controllerHandle.setConfig(null);

            // ── Hot-reload: watch out/production/TeamCode for .class changes ──
            hotReloader = new OpModeHotReloader(
                    controllerHandle,
                    null /* no extra callback needed */);
            hotReloader.start();

            if (selectedLesson != null) {
                LessonPanel panel = new LessonPanel(selectedLesson, progress, topics);
                panel.show(primaryStage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (hotReloader != null) hotReloader.stop();
        if (controllerHandle == null) return;
        if (controllerHandle.executorService != null
                && !controllerHandle.executorService.isShutdown()) {
            controllerHandle.executorService.shutdownNow();
        }
        if (controllerHandle.gamePadExecutorService != null
                && !controllerHandle.gamePadExecutorService.isShutdown()) {
            controllerHandle.gamePadExecutorService.shutdownNow();
        }
        if (controllerHandle.gamePadHelper != null) {
            controllerHandle.gamePadHelper.quit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
