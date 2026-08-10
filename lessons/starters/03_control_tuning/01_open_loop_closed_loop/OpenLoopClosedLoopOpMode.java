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
    private MotorEx leftMotor;
    private MotorEx rightMotor;

    @Override
    public void initialize() {
        leftMotor = new MotorEx(hardwareMap, "front_left_motor");
        rightMotor = new MotorEx(hardwareMap, "front_right_motor");

        schedule(new SequentialCommandGroup(
            new WaitUntilCommand(() -> getRuntime() > 0),
            new InstantCommand(() -> {
                leftMotor.set(0.3);
                rightMotor.set(0.3);
            }),
            new WaitCommand(2000),
            new InstantCommand(() -> {
                leftMotor.set(0);
                rightMotor.set(0);
            })
        ));

        schedule(new RunCommand(() -> {
            telemetry.addData("Runtime", getRuntime());
            telemetry.addData("Left Position", leftMotor.getCurrentPosition() + " ticks");
            telemetry.addData("Right Position", rightMotor.getCurrentPosition() + " ticks");
            if (leftMotor.get() != 0) {
                telemetry.addData("Mode", "Open Loop — driving at 0.3 power");
            } else {
                telemetry.addData("Mode", "Stopped");
            }
            telemetry.update();
        }));
    }
}
