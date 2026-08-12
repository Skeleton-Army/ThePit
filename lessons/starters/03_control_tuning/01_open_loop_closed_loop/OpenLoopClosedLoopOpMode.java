package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.command.WaitUntilCommand;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@TeleOp(name = "01. Open Loop vs Closed Loop OpMode", group = "Lessons")
public class OpenLoopClosedLoopOpMode extends CommandOpMode {
    private MotorEx motor;

    @Override
    public void initialize() {
        motor = new MotorEx(hardwareMap, "motor");

        schedule(new SequentialCommandGroup(
            new WaitUntilCommand(() -> getRuntime() > 0),
            new InstantCommand(() -> motor.set(0.3)),
            new WaitCommand(2000),
            new InstantCommand(() -> motor.set(0))
        ));

        schedule(new RunCommand(() -> {
            telemetry.addData("Runtime", getRuntime());
            telemetry.addData("Position", motor.getCurrentPosition() + " ticks");
            if (motor.get() != 0) {
                telemetry.addData("Mode", "Open Loop - driving at 0.3 power");
            } else {
                telemetry.addData("Mode", "Stopped");
            }
            telemetry.update();
        }));
    }
}
