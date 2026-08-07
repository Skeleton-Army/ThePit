package virtual_robot.lessons.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import virtual_robot.lessons.Lesson;
import virtual_robot.lessons.LessonProgress;
import virtual_robot.lessons.Topic;

import java.util.List;
import java.util.function.Consumer;

public class LessonMenuScreen {

    // Catppuccin Mocha palette
    private static final String BG_BASE    = "#1e1e2e";
    private static final String BG_MANTLE  = "#181825";
    private static final String BG_SURFACE = "#313244";
    private static final String BG_OVERLAY = "#45475a";
    private static final String TEXT_MAIN  = "#cdd6f4";
    private static final String TEXT_SUB   = "#a6adc8";
    private static final String TEXT_MUTED = "#6c7086";
    private static final String ACCENT     = "#89b4fa";
    private static final String GREEN      = "#a6e3a1";
    private static final String YELLOW     = "#f9e2af";
    private static final String MAUVE      = "#cba6f7";

    private final List<Topic> topics;
    private final LessonProgress progress;

    public LessonMenuScreen(List<Topic> topics, LessonProgress progress) {
        this.topics   = topics;
        this.progress = progress;
    }

    public void show(Stage stage, Consumer<Lesson> onSelected) {

        // ── Root ──
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_BASE + ";");

        // ── Header ──
        root.getChildren().add(buildHeader(onSelected));

        // ── Content ──
        VBox content = new VBox(12);
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setStyle("-fx-background-color: " + BG_BASE + ";");

        if (topics.isEmpty()) {
            Label none = new Label("No lessons found. Create topic folders under lessons/.");
            none.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13;");
            content.getChildren().add(none);
        } else {
            for (int i = 0; i < topics.size(); i++) {
                content.getChildren().add(buildTopicCard(topics.get(i), i, onSelected));
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG_BASE + "; -fx-background-color: " + BG_BASE + ";");

        root.getChildren().add(scroll);

        Scene scene = new Scene(root, 520, 620);
        scene.setFill(Color.web(BG_BASE));
        stage.setTitle("The Pit - FTC Learning");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(400);
        stage.show();
    }

    // ── Header banner ───────────────────────────────────────────────────────

    private VBox buildHeader(Consumer<Lesson> onSelected) {
        VBox header = new VBox(8);
        header.setPadding(new Insets(28, 24, 20, 24));
        header.setStyle("-fx-background-color: " + BG_MANTLE + ";");

        Label badge = new Label("FTC SIMULATOR");
        badge.setStyle(
                "-fx-background-color: " + MAUVE + ";" +
                "-fx-text-fill: " + BG_BASE + ";" +
                "-fx-padding: 3 10;" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 10;" +
                "-fx-font-weight: bold;");

        Label title = new Label("The Pit");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.setStyle("-fx-text-fill: " + TEXT_MAIN + ";");

        Label subtitle = new Label("Learn FTC programming, one lesson at a time.");
        subtitle.setStyle("-fx-text-fill: " + TEXT_SUB + "; -fx-font-size: 13;");
        subtitle.setWrapText(true);

        // Stats row
        long completed = countCompleted();
        long total     = topics.stream().mapToLong(t -> t.lessons.size()).sum();
        Label stats = new Label(completed + " / " + total + " lessons completed");
        stats.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-size: 12;");

        Button skipBtn = new Button("Skip to Simulator →");
        skipBtn.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-text-fill: " + TEXT_MAIN + ";" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 12;");
        addHover(skipBtn, BG_SURFACE, BG_OVERLAY);
        skipBtn.setOnAction(e -> onSelected.accept(null));

        header.getChildren().addAll(badge, title, subtitle, stats, skipBtn);

        // thin bottom divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: " + BG_SURFACE + ";");

        VBox wrapper = new VBox(0, header, div);
        return wrapper;
    }

    // ── Topic card (replaces old TitledPane) ────────────────────────────────

    private VBox buildTopicCard(Topic topic, int topicIndex, Consumer<Lesson> onSelected) {
        boolean locked = !isTopicUnlocked(topicIndex);

        // Card container
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + BG_MANTLE + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + (locked ? BG_SURFACE : BG_SURFACE) + ";" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;");

        // Topic header row
        HBox topicHeader = new HBox(10);
        topicHeader.setPadding(new Insets(12, 14, 12, 14));
        topicHeader.setAlignment(Pos.CENTER_LEFT);
        topicHeader.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 10 10 0 0;");

        Label lockIcon = new Label(locked ? "🔒" : "📚");
        lockIcon.setStyle("-fx-font-size: 14;");

        Label topicName = new Label(topic.title);
        topicName.setFont(Font.font("System", FontWeight.BOLD, 13));
        topicName.setStyle("-fx-text-fill: " + (locked ? TEXT_MUTED : TEXT_MAIN) + ";");
        HBox.setHgrow(topicName, Priority.ALWAYS);

        // Completion count badge
        long doneInTopic = topic.lessons.stream()
                .filter(l -> progress.isCompleted(l.id))
                .count();
        String bColor = doneInTopic == topic.lessons.size() ? GREEN : BG_OVERLAY;
        String bText  = doneInTopic + "/" + topic.lessons.size();
        Label countBadge = new Label(bText);
        countBadge.setStyle(
                "-fx-background-color: " + bColor + ";" +
                "-fx-text-fill: " + BG_BASE + ";" +
                "-fx-padding: 2 8;" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 11;" +
                "-fx-font-weight: bold;");

        topicHeader.getChildren().addAll(lockIcon, topicName, countBadge);
        card.getChildren().add(topicHeader);

        if (!locked) {
            // Description
            if (topic.description != null && !topic.description.isEmpty()) {
                Label desc = new Label(topic.description);
                desc.setStyle("-fx-text-fill: " + TEXT_SUB + "; -fx-font-size: 12;");
                desc.setWrapText(true);
                desc.setPadding(new Insets(8, 14, 4, 14));
                card.getChildren().add(desc);
            }

            // Lesson rows
            VBox lessonsBox = new VBox(2);
            lessonsBox.setPadding(new Insets(6, 10, 10, 10));
            for (Lesson lesson : topic.lessons) {
                lessonsBox.getChildren().add(buildLessonRow(lesson, topic, onSelected));
            }
            card.getChildren().add(lessonsBox);
        } else {
            Label lockMsg = new Label("Complete the previous topic to unlock.");
            lockMsg.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12;");
            lockMsg.setPadding(new Insets(10, 14, 12, 14));
            card.getChildren().add(lockMsg);
        }

        return card;
    }

    // ── Lesson row inside a topic card ──────────────────────────────────────

    private HBox buildLessonRow(Lesson lesson, Topic topic, Consumer<Lesson> onSelected) {
        boolean completed  = progress.isCompleted(lesson.id);
        boolean unlocked   = progress.isLessonUnlocked(topics, topic, lesson);
        boolean inProgress = unlocked && !completed && progress.getCurrentStep(lesson.id) > 0;

        Circle dot = new Circle(6);
        if      (completed)   dot.setFill(Color.web(GREEN));
        else if (inProgress)  dot.setFill(Color.web(YELLOW));
        else if (unlocked)    dot.setFill(Color.web(ACCENT));
        else                  dot.setFill(Color.web(TEXT_MUTED));

        String statusText;
        if      (completed)   statusText = "Done";
        else if (inProgress)  statusText = "In Progress";
        else if (unlocked)    statusText = "Start";
        else                  statusText = "Locked";

        Label statusLbl = new Label(statusText);
        statusLbl.setStyle("-fx-text-fill: " +
                (completed ? GREEN : inProgress ? YELLOW : unlocked ? ACCENT : TEXT_MUTED) +
                "; -fx-font-size: 10;");
        statusLbl.setMinWidth(68);

        Label titleLbl = new Label(lesson.title);
        titleLbl.setStyle("-fx-text-fill: " + (unlocked ? TEXT_MAIN : TEXT_MUTED) + "; -fx-font-size: 13;");
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        HBox row = new HBox(10, dot, statusLbl, titleLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: transparent; -fx-background-radius: 7; -fx-cursor: hand;");

        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: " + BG_SURFACE + "; -fx-background-radius: 7; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 7; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> {
            if (unlocked) {
                onSelected.accept(lesson);
            } else {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Skip ahead?");
                confirm.setHeaderText("You haven't completed the previous lesson.");
                confirm.setContentText("Are you sure you want to skip to \"" + lesson.title + "\"? You can come back to earlier lessons later.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        progress.markCompleted(lesson.id);
                        onSelected.accept(lesson);
                    }
                });
            }
        });

        return row;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private boolean isTopicUnlocked(int i) {
        if (i == 0) return true;
        Topic prev = topics.get(i - 1);
        for (Lesson l : prev.lessons) {
            if (!progress.isCompleted(l.id)) return false;
        }
        return true;
    }

    private long countCompleted() {
        return topics.stream()
                .flatMap(t -> t.lessons.stream())
                .filter(l -> progress.isCompleted(l.id))
                .count();
    }

    private void addHover(Button btn, String normal, String hover) {
        String base = btn.getStyle();
        btn.setOnMouseEntered(e -> btn.setStyle(
                base.replaceAll("-fx-background-color: [^;]+;",
                        "-fx-background-color: " + hover + ";")));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}
