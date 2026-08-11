package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.RunCommand;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@TeleOp(name = "04. Pedro Pathing TeleOp OpMode", group = "Lessons")
public class PedroIntroOpMode extends CommandOpMode {
    private Follower follower;

    @Override
    public void initialize() {
        follower = Constants.createFollower(hardwareMap);
        Pose startPose = new Pose(70.75, 70.75, Math.toRadians(90));
        follower.setPose(startPose);
        follower.startTeleopDrive();

        schedule(new RunCommand(() -> {
            follower.setTeleOpDrive(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x
            );
            follower.update();

            Pose pose = follower.getPose();
            telemetry.addData("x", pose.getX());
            telemetry.addData("y", pose.getY());
            telemetry.addData("heading", Math.toDegrees(pose.getHeading()));
            telemetry.update();
        }));
    }
}
