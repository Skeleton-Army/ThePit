package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.motors.Motor.RunMode;

public class Flywheel extends SubsystemBase {
    public final MotorEx motor;
    private final VoltageSensor battery;
    public final double targetRPM;

    public Flywheel(com.qualcomm.robotcore.hardware.HardwareMap hardwareMap) {
        motor = new MotorEx(hardwareMap, "motor", FlywheelConfig.MOTOR_TYPE);
        battery = hardwareMap.voltageSensor.iterator().next();

        motor.setRunMode(RunMode.VelocityControl);
        motor.setVeloCoefficients(FlywheelConfig.kP, FlywheelConfig.kI, FlywheelConfig.kD);
        motor.setFeedforwardCoefficients(FlywheelConfig.kS, FlywheelConfig.kV);
        targetRPM = FlywheelConfig.TARGET_RPM;
    }

    public double getRPM() {
        return motor.getCorrectedVelocity() * 60.0 / motor.getCPR();
    }

    @Override
    public void periodic() {
        double voltage = battery.getVoltage();
        motor.set(14.0 / voltage);
    }
}
