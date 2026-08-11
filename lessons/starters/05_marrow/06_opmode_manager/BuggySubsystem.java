package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.skeletonarmy.marrow.OpModeManager;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class BuggySubsystem extends SubsystemBase {

    private MotorEx motor;
    private double currentPower = 0.0;
    private double targetPower = 0.5;
    private double error = 0.0;

    public BuggySubsystem(HardwareMap hardwareMap) {
        motor = new MotorEx(hardwareMap, "front_left_motor");
    }

    public void setTarget(double power) {
        targetPower = power;
    }

    public void runControl() {
        error = targetPower - currentPower;
        if (Math.abs(error) < 0.01) {
            currentPower = targetPower;
            error = 0;
        } else {
            currentPower += error * 0.02;
        }
        motor.set(currentPower);
    }

    @Override
    public void periodic() {
        runControl();

        Telemetry telemetry = OpModeManager.getTelemetry();
        telemetry.addData("Buggy/currentPower", currentPower);
        telemetry.addData("Buggy/targetPower", targetPower);
        telemetry.addData("Buggy/error", error);
    }
}
