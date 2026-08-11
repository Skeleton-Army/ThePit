package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.skeletonarmy.marrow.zones.PolygonZone;
import com.skeletonarmy.marrow.zones.Point;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.button.Trigger;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@TeleOp(name = "02. Zones OpMode", group = "Lessons")
public class ZoneCommandOpMode extends CommandOpMode {

    private final PolygonZone closeLaunchZone = new PolygonZone(
        new Point(0, 141.5),
        new Point(70.75, 70.75),
        new Point(141.5, 141.5)
    );

    private final PolygonZone robotZone = new PolygonZone(18, 18);

    private Follower follower;

    @Override
    public void initialize() {
        follower = Constants.createFollower(hardwareMap);
        follower.startTeleopDrive(true);
        follower.setStartingPose(new Pose(72, 72, 0));

        new Trigger(() -> robotZone.isInside(closeLaunchZone))
            .whileActiveContinuous(new InstantCommand(() ->
                telemetry.addData("Zone", "Inside close launch zone")
            ));
    }

    @Override
    public void run() {
        super.run();

        follower.update();
        follower.setTeleOpDrive(
            -gamepad1.left_stick_y,
            -gamepad1.left_stick_x,
            -gamepad1.right_stick_x,
            true
        );

        robotZone.setPosition(
            follower.getPose().getX(),
            follower.getPose().getY()
        );
        robotZone.setRotation(follower.getPose().getHeading());
    }
}
