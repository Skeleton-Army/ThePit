package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

@TeleOp(name = "06. OpModeManager OpMode", group = "Lessons")
public class DebugOpMode extends CommandOpMode {

    private BuggySubsystem buggySubsystem;

    @Override
    public void initialize() {
        buggySubsystem = new BuggySubsystem(hardwareMap);
    }
}
