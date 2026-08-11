package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RetryCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

@Autonomous(name = "07. RetryCommand OpMode")
public class RetryCommandOpMode extends CommandOpMode {

    private int grabAttempts = 0;

    private boolean isGrabbed() {
        return grabAttempts >= 3;
    }

    @Override
    public void initialize() {
        schedule(new SequentialCommandGroup(
            new InstantCommand(() -> telemetry.addData("Status", "Starting auto")),
            new RetryCommand(
                new InstantCommand(() -> {
                    grabAttempts++;
                    telemetry.addData("Grab", "Attempt " + grabAttempts);
                }),
                () -> isGrabbed(),
                5
            ),
            new WaitCommand(500),
            new InstantCommand(() -> telemetry.addData("Status", "Attempt over")),
            new InstantCommand(() -> telemetry.addData("Status", "Auto finished"))
        ));

        schedule(new RunCommand(() -> telemetry.update()));
    }
}
