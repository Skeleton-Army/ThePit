package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.skeletonarmy.marrow.settings.Settings;

public class DriveSubsystem extends SubsystemBase {

    private MotorEx frontLeft;
    private MotorEx frontRight;
    private MotorEx backLeft;
    private MotorEx backRight;

    private double speedLimit = 1.0;

    public DriveSubsystem(HardwareMap hardwareMap) {
        frontLeft = new MotorEx(hardwareMap, "front_left_motor");
        frontRight = new MotorEx(hardwareMap, "front_right_motor");
        backLeft = new MotorEx(hardwareMap, "back_left_motor");
        backRight = new MotorEx(hardwareMap, "back_right_motor");

        speedLimit = Settings.get("max_speed", 0.8);
    }

    public void drive(double forward, double strafe, double turn) {
        frontLeft.set((forward + strafe + turn) * speedLimit);
        frontRight.set((forward - strafe - turn) * speedLimit);
        backLeft.set((forward - strafe + turn) * speedLimit);
        backRight.set((forward + strafe - turn) * speedLimit);
    }

    public void stop() {
        frontLeft.set(0);
        frontRight.set(0);
        backLeft.set(0);
        backRight.set(0);
    }

    @Override
    public void periodic() {
    }
}
