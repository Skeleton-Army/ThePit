package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMUNew;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "04. PID Turn OpMode", group = "Lessons")
public class PidTurnOpMode extends CommandOpMode {
    private MotorEx frontLeft;
    private MotorEx frontRight;
    private MotorEx backLeft;
    private MotorEx backRight;
    private BNO055IMUNew imu;
    private GamepadEx driver;
    private double targetHeading;
    private double kP;
    private double kI;
    private double kD;
    private double integral;
    private double prevError;

    @Override
    public void initialize() {
        frontLeft = new MotorEx(hardwareMap, "front_left_motor");
        frontRight = new MotorEx(hardwareMap, "front_right_motor");
        backLeft = new MotorEx(hardwareMap, "back_left_motor");
        backRight = new MotorEx(hardwareMap, "back_right_motor");
        imu = hardwareMap.get(BNO055IMUNew.class, "imu");

        driver = new GamepadEx(gamepad1);
        driver.getGamepadButton(GamepadKeys.Button.DPAD_LEFT)
            .whenPressed(() -> targetHeading += 90);
        driver.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
            .whenPressed(() -> targetHeading -= 90);

        kP = 0.1;
        kI = 0.0;
        kD = 0.0;
        targetHeading = 90;

        schedule(new RunCommand(() -> {
            double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);

            // Error: how many degrees are we off?
            double error = targetHeading - heading;

            // Wrapping: keep error in [-180, 180] to always take the shortest turn
            while (error > 180) error -= 360;
            while (error < -180) error += 360;

            // I: accumulate error over time to push through friction
            integral += error;

            // Windup guard: reset integral when error crosses zero (we overshot)
            if (Math.signum(error) != Math.signum(prevError)) {
                integral = 0;
            }

            // D: rate of error change — brakes as we approach the target
            double derivative = error - prevError;
            prevError = error;

            // Combined PID output
            double power = kP * error + kI * integral + kD * derivative;

            // Point turn: left backward, right forward for positive heading change
            frontLeft.set(-power);
            backLeft.set(-power);
            frontRight.set(power);
            backRight.set(power);

            telemetry.addData("Heading", heading);
            telemetry.addData("Target", targetHeading);
            telemetry.addData("Error", error);
            telemetry.addData("Power", power);
            telemetry.addData("kP", kP);
            telemetry.addData("kI", kI);
            telemetry.addData("kD", kD);
            telemetry.update();
        }));
    }
}
