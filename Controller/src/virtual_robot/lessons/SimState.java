package virtual_robot.lessons;

import com.qualcomm.robotcore.hardware.*;
import virtual_robot.controller.VirtualBot;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimState {
    private static final SimState INSTANCE = new SimState();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private double robotX;
    private double robotY;
    private double robotHeading;
    private final Map<String, Double>  motorPowers    = new HashMap<>();
    private final Map<String, Integer> encoderTicks   = new HashMap<>();
    private final Map<String, Double>  servoPositions = new HashMap<>();
    private final Set<String>          deviceNames    = new HashSet<>();
    private boolean opModeRunning;

    private SimState() {}

    public static SimState getInstance() { return INSTANCE; }

    /**
     * Called once per physics tick from VirtualRobotController.singlePhysicsCycle().
     */
    public void update(VirtualBot bot, HardwareMap hardwareMap,
                       boolean initialized, boolean running) {
        lock.writeLock().lock();
        try {
            opModeRunning = running;

            if (bot != null) {
                robotX       = bot.getX();
                robotY       = bot.getY();
                robotHeading = bot.getHeadingRadians();
            }

            if (hardwareMap == null || !initialized) return;

            // Collect device names — keySet() has no active-check
            deviceNames.clear();
            deviceNames.addAll(hardwareMap.dcMotor.keySet());
            deviceNames.addAll(hardwareMap.servo.keySet());
            deviceNames.addAll(hardwareMap.crservo.keySet());
            deviceNames.addAll(hardwareMap.colorSensor.keySet());
            deviceNames.addAll(hardwareMap.gyroSensor.keySet());

            // Motor state — DeviceMapping.get() returns null when not yet active
            motorPowers.clear();
            encoderTicks.clear();
            for (String name : hardwareMap.dcMotor.keySet()) {
                try {
                    DcMotor m = hardwareMap.dcMotor.get(name);
                    if (m != null) {
                        motorPowers.put(name, m.getPower());
                        encoderTicks.put(name, m.getCurrentPosition());
                    }
                } catch (Exception ignored) {}
            }

            servoPositions.clear();
            for (String name : hardwareMap.servo.keySet()) {
                try {
                    Servo s = hardwareMap.servo.get(name);
                    if (s != null) servoPositions.put(name, s.getPosition());
                } catch (Exception ignored) {}
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getRobotX() {
        lock.readLock().lock(); try { return robotX; } finally { lock.readLock().unlock(); }
    }
    public double getRobotY() {
        lock.readLock().lock(); try { return robotY; } finally { lock.readLock().unlock(); }
    }
    public double getRobotHeading() {
        lock.readLock().lock(); try { return robotHeading; } finally { lock.readLock().unlock(); }
    }
    public boolean isOpModeRunning() {
        lock.readLock().lock(); try { return opModeRunning; } finally { lock.readLock().unlock(); }
    }
    public Map<String, Double> getMotorPowers() {
        lock.readLock().lock(); try { return new HashMap<>(motorPowers); } finally { lock.readLock().unlock(); }
    }
    public Map<String, Integer> getEncoderTicks() {
        lock.readLock().lock(); try { return new HashMap<>(encoderTicks); } finally { lock.readLock().unlock(); }
    }
    public Map<String, Double> getServoPositions() {
        lock.readLock().lock(); try { return new HashMap<>(servoPositions); } finally { lock.readLock().unlock(); }
    }
    public Set<String> getDeviceNames() {
        lock.readLock().lock(); try { return new HashSet<>(deviceNames); } finally { lock.readLock().unlock(); }
    }
}
