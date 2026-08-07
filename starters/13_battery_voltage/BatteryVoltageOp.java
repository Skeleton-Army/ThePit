package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;

@TeleOp(name = "Battery Voltage OpMode", group = "Lessons")
public class BatteryVoltageOp extends OpMode {
    private VoltageSensor battery;

    @Override
    public void init() {
        // battery = hardwareMap.voltageSensor.iterator().next();
    }

    @Override
    public void loop() {
    }
}
