package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.controller.PIDController;
import com.seattlesolvers.solverslib.controller.wpilibcontroller.SimpleMotorFeedforward;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public class Flywheel extends SubsystemBase {
    public final MotorEx motor;
    private final VoltageSensor battery;
    public final double targetRPM;

    public final PIDController pid;
    public final SimpleMotorFeedforward feedforward;

    public Flywheel(com.qualcomm.robotcore.hardware.HardwareMap hardwareMap) {
        motor = new MotorEx(hardwareMap, "motor", FlywheelConfig.MOTOR_TYPE);
        battery = hardwareMap.voltageSensor.iterator().next();

        pid = new PIDController(FlywheelConfig.kP, FlywheelConfig.kI, FlywheelConfig.kD);
        feedforward = new SimpleMotorFeedforward(FlywheelConfig.kS, FlywheelConfig.kV);

        targetRPM = FlywheelConfig.TARGET_RPM;
    }

    public double getRPM() {
        return motor.getCorrectedVelocity() * 60.0 / motor.getCPR();
    }

    @Override
    public void periodic() {
        double voltage = battery.getVoltage();

        double targetVelocity = targetRPM * motor.getCPR() / 60.0;
        double currentVelocity = motor.getCorrectedVelocity();

        double pidOutput = pid.calculate(currentVelocity, targetVelocity);
        double feedforwardOutput = feedforward.calculate(targetVelocity);

        double desiredVoltage = pidOutput + feedforwardOutput;

        motor.set(desiredVoltage / voltage);
    }
}
