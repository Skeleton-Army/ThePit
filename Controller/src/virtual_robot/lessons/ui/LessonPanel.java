package virtual_robot.lessons.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import virtual_robot.lessons.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;


public class LessonPanel {
    private final Lesson lesson;
    private final LessonProgress progress;
    private final List<Topic> topics;
    private Stage panelStage;
    private int stepIndex;

    // UI fields updated in-place between steps
    private Label stepTitleLabel;
    private Label stepCountLabel;
    private VBox  contentArea;
    private Button continueButton;
    private Button backButton;
    private Circle completionDot;
    private Label  completionLabel;
    private ProgressBar progressBar;

    private AnimationTimer checkTimer;

    private static final int PANEL_WIDTH  = 400;
    private static final int PANEL_HEIGHT = 740;

    // Catppuccin Mocha palette
    private static final String BG_BASE    = "#1e1e2e";
    private static final String BG_MANTLE  = "#181825";
    private static final String BG_SURFACE = "#313244";
    private static final String BG_OVERLAY = "#45475a";
    private static final String TEXT_MAIN  = "#cdd6f4";
    private static final String TEXT_SUB   = "#a6adc8";
    private static final String ACCENT     = "#89b4fa";   // blue
    private static final String GREEN      = "#a6e3a1";
    private static final String YELLOW     = "#f9e2af";
    private static final String RED        = "#f38ba8";
    private static final String MAUVE      = "#cba6f7";

    public LessonPanel(Lesson lesson, LessonProgress progress, List<Topic> topics) {
        this.lesson    = lesson;
        this.progress  = progress;
        this.topics    = topics;
        this.stepIndex = progress.getCurrentStep(lesson.id);
    }

    public void show(Stage simulatorStage) {
        if (!lesson.starterFiles.isEmpty()) {
            boolean copied = StarterFileManager.copyStarterFiles(lesson);
            if (copied && !StarterFileManager.lessonFolderExists(lesson)) {
                System.out.println("[ThePit] Starter files copied - rebuild (Ctrl+F9) to see the OpMode.");
            }
        }

        panelStage = new Stage();
        panelStage.setTitle("The Pit - " + lesson.title);
        panelStage.setResizable(true);
        panelStage.setMinWidth(PANEL_WIDTH);
        panelStage.setMinHeight(400);
        panelStage.setOnHidden(e -> stopChecking());

        VBox root = buildRoot(simulatorStage);
        Scene scene = new Scene(root, PANEL_WIDTH, PANEL_HEIGHT);
        scene.setFill(Color.web(BG_BASE));
        panelStage.setScene(scene);
        panelStage.show();

        positionNextTo(simulatorStage);
        // Stay glued to the simulator when it moves/resizes
        simulatorStage.xProperty().addListener((o, old, v) -> positionNextTo(simulatorStage));
        simulatorStage.yProperty().addListener((o, old, v) -> positionNextTo(simulatorStage));
        simulatorStage.widthProperty().addListener((o, old, v) -> positionNextTo(simulatorStage));

        // ── Alt-tab sync (one-way, safe) ──
        // When the user alt-tabs to the lesson panel, raise the simulator behind it
        // so both windows surface together. We deliberately do NOT touch the simulator's
        // focus, the simulator to panel direction would call toFront() during button clicks
        // and break them on Windows (toFront steals focus on that platform).
        panelStage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && simulatorStage.isShowing()) {
                panelStage.toFront();
            }
        });

        startChecking();
    }

    private void positionNextTo(Stage sim) {
        panelStage.setX(sim.getX() + sim.getWidth() + 8);
        panelStage.setY(sim.getY());
    }

    // ─── UI construction ────────────────────────────────────────────────────

    private VBox buildRoot(Stage simulatorStage) {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_BASE + ";");

        // ── Top header bar ──
        root.getChildren().add(buildHeader(simulatorStage));

        // ── Step title card ──
        VBox titleCard = new VBox(4);
        titleCard.setPadding(new Insets(14, 16, 10, 16));
        titleCard.setStyle("-fx-background-color: " + BG_MANTLE + ";");

        stepCountLabel = new Label();
        stepCountLabel.setStyle("-fx-text-fill: " + TEXT_SUB + "; -fx-font-size: 11;");

        stepTitleLabel = new Label();
        stepTitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        stepTitleLabel.setStyle("-fx-text-fill: " + ACCENT + ";");
        stepTitleLabel.setWrapText(true);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(5);
        progressBar.setStyle(
                "-fx-accent: " + ACCENT + ";" +
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 3;" +
                "-fx-background-insets: 0;");

        titleCard.getChildren().addAll(stepCountLabel, stepTitleLabel, progressBar);
        root.getChildren().add(titleCard);

        // ── Thin divider ──
        root.getChildren().add(makeDivider());

        // ── Scrollable content area ──
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(14, 16, 14, 16));
        contentArea.setStyle("-fx-background-color: " + BG_BASE + ";");

        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background: " + BG_BASE + "; -fx-background-color: " + BG_BASE + ";");
        VBox.setVgrow(contentScroll, Priority.ALWAYS);
        root.getChildren().add(contentScroll);

        // ── Status / completion footer ──
        root.getChildren().add(makeDivider());
        root.getChildren().add(buildStatusBar());

        renderStep();
        return root;
    }

    /** Dark header with lesson title and back button. */
    private HBox buildHeader(Stage simulatorStage) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BG_MANTLE + ";");

        Button backBtn = new Button("← Back to Menu");
        backBtn.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-text-fill: " + TEXT_SUB + ";" +
                "-fx-cursor: hand;" +
                "-fx-padding: 5 12;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 12;");
        addHoverEffect(backBtn, BG_SURFACE, BG_OVERLAY);
        backBtn.setOnAction(e -> {
            stopChecking();
            panelStage.close();
            Stage menuStage = new Stage();
            LessonMenuScreen menu = new LessonMenuScreen(topics, progress);
            menu.show(menuStage, selected -> {
                menuStage.close();
                if (selected != null) {
                    LessonPanel newPanel = new LessonPanel(selected, progress, topics);
                    newPanel.show(simulatorStage);
                }
            });
        });

        Label lessonTitle = new Label(lesson.title);
        lessonTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lessonTitle.setStyle("-fx-text-fill: " + TEXT_MAIN + ";");
        lessonTitle.setWrapText(false);
        HBox.setHgrow(lessonTitle, Priority.ALWAYS);
        lessonTitle.setMaxWidth(Double.MAX_VALUE);

        // Pill badge
        Label badge = new Label("THE PIT");
        badge.setStyle(
                "-fx-background-color: " + MAUVE + ";" +
                "-fx-text-fill: " + BG_BASE + ";" +
                "-fx-padding: 3 8;" +
                "-fx-background-radius: 20;" +
                "-fx-font-size: 10;" +
                "-fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, lessonTitle, badge);
        return header;
    }

    /** Small coloured status bar at the bottom. */
    private HBox buildStatusBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(10, 16, 14, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: " + BG_MANTLE + ";");

        completionDot = new Circle(6, Color.web(BG_OVERLAY));
        completionLabel = new Label("Waiting...");
        completionLabel.setStyle("-fx-text-fill: " + TEXT_SUB + "; -fx-font-size: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        backButton = new Button("← Back");
        styleButton(backButton, false);
        backButton.setVisible(false);
        backButton.setManaged(false);
        backButton.setOnAction(e -> {
            if (stepIndex > 0) {
                stepIndex--;
                progress.setCurrentStep(lesson.id, stepIndex);
                stopChecking();
                renderStep();
                startChecking();
            }
        });

        continueButton = new Button("Continue →");
        styleButton(continueButton, true);
        continueButton.setVisible(false);
        continueButton.setManaged(false);
        continueButton.setOnAction(e -> {
            stopChecking();
            advance();
        });

        bar.getChildren().addAll(completionDot, completionLabel, spacer, backButton, continueButton);
        return bar;
    }

    private Region makeDivider() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setStyle("-fx-background-color: " + BG_SURFACE + ";");
        return line;
    }

    // ─── Step rendering ─────────────────────────────────────────────────────

    private void renderStep() {
        if (stepIndex >= lesson.steps.size()) {
            showCompletion();
            return;
        }

        LessonStep step = lesson.steps.get(stepIndex);
        double frac = lesson.steps.size() > 0
                ? (double) stepIndex / lesson.steps.size() : 0;

        stepCountLabel.setText("Step " + (stepIndex + 1) + " of " + lesson.steps.size());
        stepTitleLabel.setText(step.title);
        stepTitleLabel.setStyle("-fx-text-fill: " + ACCENT + ";");
        progressBar.setProgress(frac);

        contentArea.getChildren().clear();
        for (ContentBlock block : step.content) {
            contentArea.getChildren().add(renderBlock(block));
        }

        boolean hasCheck = step.check != null;
        backButton.setVisible(stepIndex > 0);
        backButton.setManaged(stepIndex > 0);
        continueButton.setVisible(true);
        continueButton.setManaged(true);
        continueButton.setText("Next →");

        setStatus(false, hasCheck ? "Complete the task or click Next..." : "Read and continue...");
    }

    private Node renderBlock(ContentBlock block) {
        switch (block.type) {
            case TEXT: {
                Label lbl = new Label(block.value);
                lbl.setWrapText(true);
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setStyle("-fx-text-fill: " + TEXT_MAIN + "; -fx-font-size: 13; -fx-line-spacing: 2;");
                return lbl;
            }
            case CODE: {
                TextArea ta = new TextArea(block.value);
                ta.setEditable(false);
                ta.setWrapText(false);
                ta.setFont(Font.font("Monospace", 12));
                ta.setStyle(
                        "-fx-control-inner-background: " + BG_MANTLE + ";" +
                        "-fx-text-fill: " + TEXT_MAIN + ";" +
                        "-fx-background-color: " + BG_MANTLE + ";" +
                        "-fx-border-color: " + BG_SURFACE + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");
                int rows = Math.min(block.value.split("\n").length + 1, 10);
                ta.setPrefRowCount(rows);

                Button copyBtn = new Button("Copy");
                styleButton(copyBtn, false);
                copyBtn.setOnAction(ev -> {
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(block.value);
                    Clipboard.getSystemClipboard().setContent(cc);
                    copyBtn.setText("✓ Copied!");
                    copyBtn.setStyle(copyBtn.getStyle() +
                            "-fx-text-fill: " + GREEN + ";");
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        copyBtn.setText("Copy");
                        styleButton(copyBtn, false);
                    });
                    pause.play();
                });

                VBox box = new VBox(6, ta, copyBtn);
                box.setStyle(
                        "-fx-background-color: " + BG_MANTLE + ";" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: " + BG_SURFACE + ";" +
                        "-fx-border-radius: 8;");
                return box;
            }
            case LINK: {
                Hyperlink link = new Hyperlink("🔗 " + block.label);
                link.setStyle(
                        "-fx-text-fill: " + ACCENT + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-border-color: transparent;");
                link.setOnAction(ev -> {
                    try {
                        Desktop.getDesktop().browse(new URI(block.url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                return link;
            }
        }
        return new Label("?");
    }

    // ─── Check timer ────────────────────────────────────────────────────────

    private void startChecking() {
        stopChecking();
        checkTimer = new AnimationTimer() {
            private long lastCheck = 0;
            @Override
            public void handle(long now) {
                if (now - lastCheck < 250_000_000L) return;
                lastCheck = now;

                if (stepIndex >= lesson.steps.size()) return;
                LessonStep step = lesson.steps.get(stepIndex);
                if (step.check == null) {
                    setStatus(false, "Read and continue...");
                    return;
                }
                boolean done = CheckEvaluator.evaluate(step.check, SimState.getInstance());
                setStatus(done, done ? "✓ Complete!" : "Waiting...");

                if (done) {
                    stop();
                    PauseTransition pause = new PauseTransition(Duration.millis(600));
                    pause.setOnFinished(e -> advance());
                    pause.play();
                }
            }
        };
        checkTimer.start();
    }

    private void advance() {
        stepIndex++;
        progress.setCurrentStep(lesson.id, stepIndex);
        if (stepIndex >= lesson.steps.size()) {
            progress.markCompleted(lesson.id);
            showCompletion();
        } else {
            renderStep();
            startChecking();
        }
    }

    private void showCompletion() {
        stopChecking();
        stepCountLabel.setText("Lesson Complete!");
        stepTitleLabel.setText("🎉 " + lesson.title);
        stepTitleLabel.setStyle("-fx-text-fill: " + GREEN + ";");
        progressBar.setProgress(1.0);
        progressBar.setStyle(
                "-fx-accent: " + GREEN + ";" +
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 3;" +
                "-fx-background-insets: 0;");

        contentArea.getChildren().clear();
        Label msg = new Label(
                "You've completed \"" + lesson.title + "\"!\n\nHead back to the menu to pick your next lesson.");
        msg.setWrapText(true);
        msg.setStyle("-fx-text-fill: " + TEXT_MAIN + "; -fx-font-size: 13;");

        VBox card = new VBox(10, msg);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + GREEN + ";" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;");
        contentArea.getChildren().add(card);

        backButton.setVisible(false);
        backButton.setManaged(false);
        continueButton.setVisible(false);
        continueButton.setManaged(false);
        setStatus(true, "All steps complete!");
        completionLabel.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 12;");
    }

    private void stopChecking() {
        if (checkTimer != null) { checkTimer.stop(); checkTimer = null; }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private void setStatus(boolean done, String text) {
        completionDot.setFill(Color.web(done ? GREEN : BG_OVERLAY));
        completionLabel.setText(text);
        completionLabel.setStyle("-fx-text-fill: " + (done ? GREEN : TEXT_SUB) + "; -fx-font-size: 12;");
    }

    private void styleButton(Button btn, boolean primary) {
        String bg = primary ? ACCENT : BG_SURFACE;
        String fg = primary ? BG_BASE : TEXT_MAIN;
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 16;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 12;");
        addHoverEffect(btn, bg, primary ? "#7aa2f7" : BG_OVERLAY);
    }

    private void addHoverEffect(Button btn, String normalColor, String hoverColor) {
        String baseStyle = btn.getStyle();
        btn.setOnMouseEntered(e -> btn.setStyle(
                baseStyle.replaceAll("-fx-background-color: [^;]+;",
                        "-fx-background-color: " + hoverColor + ";")));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }
}
