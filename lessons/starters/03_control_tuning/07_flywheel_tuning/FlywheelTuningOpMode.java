package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.RunCommand;

@TeleOp(name = "07. Flywheel Tuning OpMode", group = "Lessons")
public class FlywheelTuningOpMode extends CommandOpMode {
    private Flywheel flywheel;

    @Override
    public void initialize() {
        flywheel = new Flywheel(hardwareMap);

        schedule(new RunCommand(() -> {
            double currentRPM = flywheel.getRPM();
            double[] coefficients = flywheel.pid.getCoefficients();
            telemetry.addData("Target RPM", flywheel.targetRPM);
            telemetry.addData("Current RPM", currentRPM);
            telemetry.addData("Power", flywheel.motor.get());
            telemetry.addData("kP", coefficients[0]);
            telemetry.addData("kI", coefficients[1]);
            telemetry.addData("kD", coefficients[2]);
            telemetry.addData("kS", flywheel.feedforward.ks);
            telemetry.addData("kV", flywheel.feedforward.kv);
            telemetry.update();
        }));
    }
}
