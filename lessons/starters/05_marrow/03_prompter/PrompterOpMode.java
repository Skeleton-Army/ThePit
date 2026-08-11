package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.skeletonarmy.marrow.prompts.Prompter;
import com.skeletonarmy.marrow.prompts.BooleanPrompt;
import com.skeletonarmy.marrow.prompts.OptionPrompt;
import com.seattlesolvers.solverslib.command.CommandOpMode;

@Autonomous(name = "03. Prompter OpMode")
public class PrompterOpMode extends CommandOpMode {

    enum Alliance { RED, BLUE }
    enum StartPosition { LEFT, CENTER, RIGHT }

    private Prompter prompter = new Prompter(this);

    @Override
    public void initialize() {
        prompter
            .prompt("alliance", new OptionPrompt<>("Select Alliance", Alliance.class))
            .prompt("start_pos", new OptionPrompt<>("Start Position", StartPosition.class))
            .prompt("enable_delay", new BooleanPrompt("Enable Start Delay?", false))
                .label("Start Delay")
            .prompt("park", new BooleanPrompt("Enable Parking?", true))
            .prompt("park_location", new OptionPrompt<>("Park Location", 1, 2, 3))
                .label("Park Location")
                .showIf("park", true)
            .onComplete(() -> {
                Alliance alliance = prompter.get("alliance");
                StartPosition startPos = prompter.get("start_pos");
                boolean enableDelay = prompter.get("enable_delay");
                boolean park = prompter.get("park");
                int parkLocation = prompter.getOrDefault("park_location", 0);

                telemetry.addData("Alliance", alliance);
                telemetry.addData("Start Pos", startPos);
                telemetry.addData("Delay", enableDelay);
                telemetry.addData("Park", park);
                telemetry.addData("Park Loc", parkLocation);
                telemetry.update();
            });
    }

    @Override
    public void run() {
        if (!prompter.isCompleted()) {
            prompter.run();
            return;
        }
        super.run();
    }
}
