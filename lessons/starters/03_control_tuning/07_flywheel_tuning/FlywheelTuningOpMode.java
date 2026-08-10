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
            double[] velo = flywheel.motor.getVeloCoefficients();
            double[] ff = flywheel.motor.getFeedforwardCoefficients();
            telemetry.addData("Target RPM", flywheel.targetRPM);
            telemetry.addData("Current RPM", currentRPM);
            telemetry.addData("Power", flywheel.motor.get());
            telemetry.addData("kP", velo[0]);
            telemetry.addData("kI", velo[1]);
            telemetry.addData("kD", velo[2]);
            telemetry.addData("kS", ff[0]);
            telemetry.addData("kV", ff[1]);
            telemetry.update();
        }));
    }
}
