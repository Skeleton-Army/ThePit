package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@TeleOp(name = "08. Loop Times OpMode", group = "Lessons")
public class LoopTimesOpMode extends CommandOpMode {
    private MotorEx frontLeft;
    private MotorEx frontRight;
    private MotorEx backLeft;
    private MotorEx backRight;
    private ElapsedTime timer;

    @Override
    public void initialize() {
        frontLeft = new MotorEx(hardwareMap, "front_left_motor");
        frontRight = new MotorEx(hardwareMap, "front_right_motor");
        backLeft = new MotorEx(hardwareMap, "back_left_motor");
        backRight = new MotorEx(hardwareMap, "back_right_motor");

        timer = new ElapsedTime();
        timer.reset();

        schedule(new RunCommand(() -> {
            telemetry.addData("Loop time ms", timer.milliseconds());
            timer.reset();
            telemetry.update();
        }));
    }
}
