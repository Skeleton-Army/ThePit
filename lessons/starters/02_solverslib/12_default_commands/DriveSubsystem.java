package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

public class DriveSubsystem extends SubsystemBase {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    public DriveSubsystem(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotor.class, "front_left_motor");
        frontRight = hardwareMap.get(DcMotor.class, "front_right_motor");
        backLeft = hardwareMap.get(DcMotor.class, "back_left_motor");
        backRight = hardwareMap.get(DcMotor.class, "back_right_motor");

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void drive(double forward, double strafe, double turn) {
        frontLeft.setPower(forward + strafe + turn);
        frontRight.setPower(forward - strafe - turn);
        backLeft.setPower(forward - strafe + turn);
        backRight.setPower(forward + strafe - turn);
    }

    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }

    @Override
    public void periodic() {
    }
}
