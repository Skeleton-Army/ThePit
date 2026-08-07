package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

@TeleOp(name = "12. Command Decorators OpMode", group = "Lessons")
public class CommandDecoratorsOpMode extends CommandOpMode {

    private DcMotor motor;

    @Override
    public void initialize() {
        motor = hardwareMap.get(DcMotor.class, "front_left_motor");

        // Example 1: withTimeout() on a RunCommand
        // RunCommand normally runs forever. withTimeout(3000) caps it at 3 seconds.
        schedule(new RunCommand(() -> motor.setPower(0.5))
                .withTimeout(3000));

        // Example 2: asProxy() defers creation until scheduled.
        // Useful for button bindings — creates a fresh command each press.
        // new GamepadEx(gamepad1).getGamepadButton(GamepadKeys.Button.A)
        //     .whenPressed(new InstantCommand(() -> motor.setPower(0.5)).asProxy());

        // Example 3: until() stops a command when a condition is true.
        // The supplier is checked every tick.
        // schedule(new RunCommand(() -> motor.setPower(0.5))
        //     .until(() -> motor.getCurrentPosition() > 1000));
    }
}
