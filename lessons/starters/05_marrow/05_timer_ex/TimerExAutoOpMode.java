package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.skeletonarmy.marrow.TimerEx;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.ConditionalCommand;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

@Autonomous(name = "05. TimerEx OpMode")
public class TimerExAutoOpMode extends CommandOpMode {

    private TimerEx matchTime = new TimerEx(30);

    @Override
    public void initialize() {
        matchTime.start();

        schedule(new ConditionalCommand(
            new SequentialCommandGroup(
                new InstantCommand(() -> telemetry.addData("Step", "Starting auto")),
                new WaitCommand(1000),
                new InstantCommand(() -> telemetry.addData("Step", "1 second passed")),
                new WaitCommand(1000),
                new InstantCommand(() -> telemetry.addData("Step", "2 seconds passed")),
                new WaitCommand(1000),
                new InstantCommand(() -> telemetry.addData("Step", "3 seconds passed")),
                new InstantCommand(() -> telemetry.addData("Step", "Auto finished"))
            ),
            new InstantCommand(() -> telemetry.addData("Step", "Skipping — parking instead")),
            () -> matchTime.isMoreThan(2)
        ));

        schedule(new RunCommand(() -> {
            telemetry.addData("Time Remaining", matchTime.getRemaining());
            telemetry.update();
        }));
    }
}
