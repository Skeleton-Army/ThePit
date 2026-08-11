package virtual_robot.robots.classes;

import com.qualcomm.hardware.CommonOdometry;
import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.bosch.BNO055IMUNew;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriverInternal;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.firstinspires.ftc.robotcore.external.matrices.GeneralMatrixF;
import org.firstinspires.ftc.robotcore.external.matrices.MatrixF;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import virtual_robot.config.Config;
import virtual_robot.controller.Filters;
import virtual_robot.controller.VirtualBot;
import virtual_robot.controller.VirtualField;
import virtual_robot.controller.VirtualRobotController;
import virtual_robot.util.AngleUtils;

public abstract class DynamicMecanumBase extends VirtualBot {

    public final MotorType MOTOR_TYPE;
    private DcMotorExDynImpl[] motors = null;
    private BNO055IMUImpl imu = null;
    BNO055IMUNew imuNew = null;
    private CommonOdometry odo = CommonOdometry.getInstance();
    private GoBildaPinpointDriverInternal goBildaPinpointDriverInternal = null;
    private VirtualRobotController.ColorSensorImpl colorSensor = null;
    private VirtualRobotController.DistanceSensorImpl[] distanceSensors = null;

    protected double gearRatioWheel = 1.0;

    private final double wheelRadiusMeters = 2.0 / VirtualField.INCHES_PER_METER;
    private final double wheelInertia = 0.236 * (9.0 / 16.0) * wheelRadiusMeters * wheelRadiusMeters;
    private final double wheelCircumferenceMeters = Math.PI * 2.0 * wheelRadiusMeters;
    private final double wlAverageMeters = 15.0 / VirtualField.INCHES_PER_METER;

    private MatrixF motorTorqueToRobotForces;

    public DynamicMecanumBase() {
        super();
        MOTOR_TYPE = Config.DEFAULT_DRIVE_MOTOR_TYPE;
    }

    public DynamicMecanumBase(MotorType driveMotorType) {
        super();
        MOTOR_TYPE = driveMotorType;
    }

    public void initialize() {
        super.initialize();
        hardwareMap.setActive(true);
        motors = new DcMotorExDynImpl[]{
                (DcMotorExDynImpl) hardwareMap.get(DcMotorEx.class, "back_left_motor"),
                (DcMotorExDynImpl) hardwareMap.get(DcMotorEx.class, "front_left_motor"),
                (DcMotorExDynImpl) hardwareMap.get(DcMotorEx.class, "front_right_motor"),
                (DcMotorExDynImpl) hardwareMap.get(DcMotorEx.class, "back_right_motor")
        };
        distanceSensors = new VirtualRobotController.DistanceSensorImpl[]{
                hardwareMap.get(VirtualRobotController.DistanceSensorImpl.class, "front_distance"),
                hardwareMap.get(VirtualRobotController.DistanceSensorImpl.class, "left_distance"),
                hardwareMap.get(VirtualRobotController.DistanceSensorImpl.class, "back_distance"),
                hardwareMap.get(VirtualRobotController.DistanceSensorImpl.class, "right_distance")
        };
        imu = hardwareMap.get(BNO055IMUImpl.class, "imu");
        imuNew = hardwareMap.get(BNO055IMUNew.class, "imu");
        colorSensor = (VirtualRobotController.ColorSensorImpl) hardwareMap.colorSensor.get("color_sensor");
        goBildaPinpointDriverInternal = hardwareMap.get(GoBildaPinpointDriverInternal.class, "pinpoint");
        hardwareMap.setActive(false);

        GeneralMatrixF K = new GeneralMatrixF(4, 4, new float[]{
                1, -1, (float) wlAverageMeters, 1,
                -1, -1, (float) wlAverageMeters, -1,
                -1, 1, (float) wlAverageMeters, 1,
                1, 1, (float) wlAverageMeters, -1
        });

        MatrixF M = GeneralMatrixF.diagonalMatrix(new VectorF(
                (float) chassisBody.getMass().getMass(), (float) chassisBody.getMass().getMass(),
                (float) chassisBody.getMass().getInertia(), (float) chassisBody.getMass().getMass()
        ));

        MatrixF temp1 = K.multiplied(M.inverted().multiplied(K.transposed()))
                .multiplied((float) (wheelInertia / wheelRadiusMeters));
        MatrixF temp2 = GeneralMatrixF.identityMatrix(4)
                .multiplied((float) wheelRadiusMeters);

        motorTorqueToRobotForces = K.transposed().multiplied((temp1.added(temp2)).inverted());
    }

    protected void createHardwareMap() {
        hardwareMap = new HardwareMap();
        String[] motorNames = new String[]{"back_left_motor", "front_left_motor", "front_right_motor", "back_right_motor"};
        for (int i = 0; i < 4; i++) {
            hardwareMap.put(motorNames[i], new DcMotorExDynImpl(MOTOR_TYPE, motorController0, i));
        }
        String[] distNames = new String[]{"front_distance", "left_distance", "back_distance", "right_distance"};
        for (String name : distNames) hardwareMap.put(name, controller.new DistanceSensorImpl());
        hardwareMap.put("imu", new BNO055IMUImpl(this, 10));
        hardwareMap.put("imu", new BNO055IMUNew(this, 10));
        hardwareMap.put("color_sensor", controller.new ColorSensorImpl());
        hardwareMap.put("pinpoint", new GoBildaPinpointDriverInternal());
    }

    public synchronized void updateStateAndSensors(double millis) {
        double xMeters = chassisBody.getTransform().getTranslationX();
        double yMeters = chassisBody.getTransform().getTranslationY();
        x = xMeters * VirtualField.PIXELS_PER_METER;
        y = yMeters * VirtualField.PIXELS_PER_METER;
        headingRadians = chassisBody.getTransform().getRotationAngle();

        Vector2 velocityMetersPerSec = chassisBody.getLinearVelocity();
        double vxMetersPerSec = velocityMetersPerSec.x;
        double vyMetersPerSec = velocityMetersPerSec.y;
        double angularVelocityRadiansPerSec = chassisBody.getAngularVelocity();

        double sinHd = Math.sin(headingRadians);
        double cosHd = Math.cos(headingRadians);

        double vxRobot = vxMetersPerSec * cosHd + vyMetersPerSec * sinHd;
        double vyRobot = -vxMetersPerSec * sinHd + vyMetersPerSec * cosHd;

        double[] wSpd = new double[4];
        wSpd[0] = vxRobot - vyRobot + angularVelocityRadiansPerSec * wlAverageMeters;
        wSpd[1] = -vxRobot - vyRobot + angularVelocityRadiansPerSec * wlAverageMeters;
        wSpd[2] = -vxRobot + vyRobot + angularVelocityRadiansPerSec * wlAverageMeters;
        wSpd[3] = vxRobot + vyRobot + angularVelocityRadiansPerSec * wlAverageMeters;
        for (int i = 0; i < 4; i++) {
            wSpd[i] *= 60.0 / (wheelCircumferenceMeters * MOTOR_TYPE.MAX_RPM);
        }

        float[] tauArray = new float[4];
        for (int i = 0; i < 4; i++) {
            tauArray[i] = (float) motors[i].update(millis, wSpd[i]);
        }
        VectorF tauVec = new VectorF(tauArray);

        VectorF forcesRobot = motorTorqueToRobotForces.multiplied(tauVec);

        double fx = forcesRobot.get(0) * cosHd - forcesRobot.get(1) * sinHd;
        double fy = forcesRobot.get(0) * sinHd + forcesRobot.get(1) * cosHd;
        double torque = forcesRobot.get(2);

        Vector2 force = new Vector2(fx, fy);

        chassisBody.applyForce(force);
        chassisBody.applyTorque(torque);

        Vector2 accelMetersPerSecSqr = force.quotient(chassisBody.getMass().getMass());
        double angularAccelRadiansPerSecSqr = torque / chassisBody.getMass().getInertia();

        imu.updateHeadingRadians(headingRadians);
        imuNew.updateHeadingRadians(headingRadians);
        odo.update(
                new Pose2D(DistanceUnit.METER, xMeters, yMeters, AngleUnit.RADIANS, headingRadians),
                new Pose2D(DistanceUnit.METER, velocityMetersPerSec.x, velocityMetersPerSec.y, AngleUnit.RADIANS, angularVelocityRadiansPerSec),
                new Pose2D(DistanceUnit.METER, accelMetersPerSecSqr.x, accelMetersPerSecSqr.y, AngleUnit.RADIANS, angularAccelRadiansPerSecSqr)
        );

        colorSensor.updateColor(x, y);

        goBildaPinpointDriverInternal.update();

        final double piOver2 = Math.PI / 2.0;
        for (int i = 0; i < 4; i++) {
            double sensorHeading = AngleUtils.normalizeRadians(headingRadians + i * piOver2);
            distanceSensors[i].updateDistance(x - halfBotWidth * Math.sin(sensorHeading),
                    y + halfBotWidth * Math.cos(sensorHeading), sensorHeading);
        }
    }

    public synchronized void updateDisplay() {
        super.updateDisplay();
    }

    public void powerDownAndReset() {
        for (int i = 0; i < 4; i++) motors[i].stopAndReset();
        imu.close();
        odo.update(
                new Pose2D(DistanceUnit.METER, x / VirtualField.PIXELS_PER_METER, y / VirtualField.PIXELS_PER_METER, AngleUnit.RADIANS, headingRadians),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0)
        );
        goBildaPinpointDriverInternal.update();
        chassisBody.setAngularVelocity(0);
        chassisBody.setLinearVelocity(0, 0);
    }

    public void setUpChassisBody() {
        chassisBody = new Body();
        chassisBody.setUserData(this);
        double botWidthMeters = botWidth / VirtualField.PIXELS_PER_METER;
        chassisFixture = chassisBody.addFixture(
                new org.dyn4j.geometry.Rectangle(botWidthMeters, botWidthMeters), 47.84, 0, 0);
        chassisRectangle = (org.dyn4j.geometry.Rectangle) chassisFixture.getShape();
        chassisFixture.setFilter(Filters.CHASSIS_FILTER);
        chassisBody.setMass(MassType.NORMAL);
        chassisBody.setLinearDamping(1);
        chassisBody.setAngularDamping(1);
        world.addBody(chassisBody);
    }

    @Override
    public synchronized void positionWithMouseClick(MouseEvent arg) {
        super.positionWithMouseClick(arg);
        odo.update(
                new Pose2D(DistanceUnit.METER, x / VirtualField.PIXELS_PER_METER, y / VirtualField.PIXELS_PER_METER, AngleUnit.RADIANS, headingRadians),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0),
                new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0)
        );
        odo.setPosition(new Pose2D(DistanceUnit.METER, 0, 0, AngleUnit.RADIANS, 0));
        goBildaPinpointDriverInternal.internalUpdate(false, false);
        goBildaPinpointDriverInternal.resetEncoders();
    }
}
