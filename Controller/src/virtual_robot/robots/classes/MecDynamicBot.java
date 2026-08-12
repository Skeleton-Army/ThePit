package virtual_robot.robots.classes;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.GyroSensorImpl;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import virtual_robot.controller.BotConfig;
import virtual_robot.controller.Filters;
import virtual_robot.controller.VirtualField;
import virtual_robot.dyn4j.Dyn4jUtil;
import virtual_robot.dyn4j.FixtureData;
import virtual_robot.dyn4j.Slide;
import virtual_robot.game_elements.classes.WobbleGoal;

import java.util.HashMap;

@BotConfig(name = "MecDynamic Bot", filename = "mec_dynamic_bot")
public class MecDynamicBot extends DynamicMecanumBase {

    private DcMotorExImpl armMotor = null;
    private ServoImpl handServo = null;

    @FXML
    private Group armGroup;
    @FXML
    private Rectangle arm;
    @FXML
    private Rectangle hand;
    @FXML
    private Group leftFingerGroup;
    @FXML
    private Rectangle leftProximalPhalanx;
    @FXML
    private Rectangle leftDistalPhalanx;
    @FXML
    private Group rightFingerGroup;
    @FXML
    private Rectangle rightProximalPhalanx;
    @FXML
    private Rectangle rightDistalPhalanx;

    Translate armTranslateTransform;
    Translate leftFingerTranslateTransform;
    Translate rightFingerTranslateTransform;

    private double armTranslation = 0;
    private double fingerPos = 0;

    private Body armBody;
    private Body leftFingerBody;
    private Body rightFingerBody;
    Slide armSlide;
    Slide leftFingerSlide;
    Slide rightFingerSlide;

    private CategoryFilter ARM_FILTER = new CategoryFilter(Filters.ARM, WobbleGoal.WOBBLE_HANDLE_CATEGORY | Filters.WALL);

    public MecDynamicBot() {
        super();
    }

    public void initialize() {
        super.initialize();
        hardwareMap.setActive(true);

        armMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "arm_motor");
        armMotor.setActualPositionLimits(0, 2240);
        armMotor.setPositionLimitsEnabled(true);

        handServo = (ServoImpl) hardwareMap.servo.get("hand_servo");

        hardwareMap.setActive(false);

        armTranslateTransform = new Translate(0, 0);
        armGroup.getTransforms().add(armTranslateTransform);

        leftFingerTranslateTransform = new Translate(0, 0);
        leftFingerGroup.getTransforms().add(leftFingerTranslateTransform);

        rightFingerTranslateTransform = new Translate(0, 0);
        rightFingerGroup.getTransforms().add(rightFingerTranslateTransform);

        HashMap<Shape, FixtureData> armMap = new HashMap<>();
        armMap.put(arm, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 2, 1));
        armMap.put(hand, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 1, 2));
        armBody = Dyn4jUtil.createBody(armGroup, this, 9, 9, armMap);
        world.addBody(armBody);
        armSlide = new Slide(chassisBody, armBody, new Vector2(0, 0), new Vector2(0, -1),
                VirtualField.Unit.PIXEL);
        world.addJoint(armSlide);

        HashMap<Shape, FixtureData> leftFingerMap = new HashMap<>();
        leftFingerMap.put(leftProximalPhalanx, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 2, 1));
        leftFingerMap.put(leftDistalPhalanx, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 1, 2));
        leftFingerBody = Dyn4jUtil.createBody(leftFingerGroup, this, 9, 9, leftFingerMap);
        world.addBody(leftFingerBody);
        leftFingerSlide = new Slide(armBody, leftFingerBody, new Vector2(0, 0), new Vector2(-1, 0),
                VirtualField.Unit.PIXEL);
        world.addJoint(leftFingerSlide);

        HashMap<Shape, FixtureData> rightFingerMap = new HashMap<>();
        rightFingerMap.put(rightProximalPhalanx, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 2, 1));
        rightFingerMap.put(rightDistalPhalanx, new FixtureData(ARM_FILTER, 1.0, 0, 0.25, 1, 2));
        rightFingerBody = Dyn4jUtil.createBody(rightFingerGroup, this, 9, 9, rightFingerMap);
        world.addBody(rightFingerBody);
        rightFingerSlide = new Slide(armBody, rightFingerBody, new Vector2(0, 0), new Vector2(-1, 0),
                VirtualField.Unit.PIXEL);
        world.addJoint(rightFingerSlide);
    }

    protected void createHardwareMap() {
        super.createHardwareMap();
        hardwareMap.put("gyro_sensor", new GyroSensorImpl(this));
        hardwareMap.put("arm_motor", new DcMotorExImpl(MotorType.Neverest40, motorController1, 0));
        hardwareMap.put("hand_servo", new ServoImpl());
    }

    public synchronized void updateStateAndSensors(double millis) {
        super.updateStateAndSensors(millis);

        armMotor.update(millis);
        armTranslation = armMotor.getActualPosition() * 50.0 / 2240.0 * (botWidth / 75.0);
        armSlide.setPosition(armTranslation);

        fingerPos = 15 * handServo.getInternalPosition();
        leftFingerSlide.setPosition(fingerPos);
        rightFingerSlide.setPosition(-fingerPos);
    }

    @Override
    public synchronized void updateDisplay() {
        super.updateDisplay();
        armTranslateTransform.setY(-armTranslation);
        leftFingerTranslateTransform.setY(-armTranslation);
        rightFingerTranslateTransform.setY(-armTranslation);
        if (Math.abs(fingerPos - leftFingerTranslateTransform.getX()) > 0.001) {
            leftFingerTranslateTransform.setX(fingerPos);
            rightFingerTranslateTransform.setX(-fingerPos);
        }
    }

    @Override
    public synchronized void positionWithMouseClick(MouseEvent arg) {
        Transform tArmChassis = Dyn4jUtil.multiplyTransforms(Dyn4jUtil.getInverseTransform(chassisBody.getTransform()),
                armBody.getTransform());
        Transform tLeftFingerChassis = Dyn4jUtil.multiplyTransforms(Dyn4jUtil.getInverseTransform(chassisBody.getTransform()),
                leftFingerBody.getTransform());
        Transform tRightFingerChassis = Dyn4jUtil.multiplyTransforms(Dyn4jUtil.getInverseTransform(chassisBody.getTransform()),
                rightFingerBody.getTransform());

        super.positionWithMouseClick(arg);

        Transform tArm = Dyn4jUtil.multiplyTransforms(chassisBody.getTransform(), tArmChassis);
        Transform tLeftFinger = Dyn4jUtil.multiplyTransforms(chassisBody.getTransform(), tLeftFingerChassis);
        Transform tRightFinger = Dyn4jUtil.multiplyTransforms(chassisBody.getTransform(), tRightFingerChassis);
        armBody.setTransform(tArm);
        armBody.setLinearVelocity(0, 0);
        armBody.setAngularVelocity(0);
        armBody.clearAccumulatedForce();
        armBody.clearAccumulatedTorque();
        leftFingerBody.setTransform(tLeftFinger);
        leftFingerBody.setLinearVelocity(0, 0);
        leftFingerBody.setAngularVelocity(0);
        leftFingerBody.clearAccumulatedTorque();
        leftFingerBody.clearAccumulatedForce();
        rightFingerBody.setTransform(tRightFinger);
        rightFingerBody.setLinearVelocity(0, 0);
        rightFingerBody.setAngularVelocity(0);
        rightFingerBody.clearAccumulatedForce();
        rightFingerBody.clearAccumulatedTorque();
    }

    public void powerDownAndReset() {
        super.powerDownAndReset();
        armMotor.stopAndReset();
    }
}
