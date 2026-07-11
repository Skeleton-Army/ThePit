package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "First Motor Op", group = "Lessons")
public class FirstMotorOp extends LinearOpMode {

    // Step 1: declare your motor field here
    DcMotor leftMotor;

    @Override
    public void runOpMode() {

        // Step 1: get the motor from the hardware map.
        // Uncomment the line below and fill in the device name.
        // leftMotor = hardwareMap.get(DcMotor.class, "front_left_motor");

        waitForStart();

        while (opModeIsActive()) {

            // Step 2: set motor power.
            // leftMotor.setPower(0.5);

            // Step 3: read the encoder.
            // int ticks = leftMotor.getCurrentPosition();
            // telemetry.addData("Encoder", ticks);
            // telemetry.update();

            // Step 4: stop the motor when done.
            // leftMotor.setPower(0);
        }
    }
}
