package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Hardware Map OpMode", group = "Lessons")
public class HardwareMapOp extends OpMode {
    private DcMotor motor;

    @Override
    public void init() {
        // motor = hardwareMap.get(DcMotor.class, "front_left_motor");
    }

    @Override
    public void loop() {
    }
}
