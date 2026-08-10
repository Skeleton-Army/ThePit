package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@TeleOp(name = "02. Voltage Compensation OpMode", group = "Lessons")
public class VoltageCompensationOpMode extends CommandOpMode {
    private MotorEx motor;
    private VoltageSensor battery;

    @Override
    public void initialize() {
        motor = new MotorEx(hardwareMap, "front_left_motor");
        battery = hardwareMap.voltageSensor.iterator().next();

        schedule(new RunCommand(() -> {
            double voltage = battery.getVoltage();

            motor.set(0.5);

            telemetry.addData("Voltage", voltage);
            telemetry.addData("Velocity (ticks/s)", motor.getCorrectedVelocity());
            telemetry.addData("Power", motor.get());
            telemetry.update();
        }));
    }
}
