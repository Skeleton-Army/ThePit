package virtual_robot.lessons;

import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
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
    private double robotHeadingDegrees;
    private final Map<String, Double>  motorPowers    = new HashMap<>();
    private final Map<String, Integer> encoderTicks   = new HashMap<>();
    private final Map<String, Double>  servoPositions = new HashMap<>();
    private final Map<String, Double>  sensorValues   = new HashMap<>();
    private double voltage;
    private double elapsedTime;
    private final Set<String>          deviceNames    = new HashSet<>();
    private boolean opModeRunning;

    private long startTimeNanos;
    private boolean started;

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

            if (running && !started) {
                startTimeNanos = System.nanoTime();
                started = true;
            } else if (!running) {
                started = false;
            }

            if (running) {
                elapsedTime = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0;
            }

            if (bot != null) {
                robotX       = bot.getX();
                robotY       = bot.getY();
                robotHeading = bot.getHeadingRadians();
                robotHeadingDegrees = Math.toDegrees(robotHeading);
            }

            if (hardwareMap == null || !initialized) return;

            // Collect device names, keySet() has no active-check
            deviceNames.clear();
            deviceNames.addAll(hardwareMap.dcMotor.keySet());
            deviceNames.addAll(hardwareMap.servo.keySet());
            deviceNames.addAll(hardwareMap.crservo.keySet());
            deviceNames.addAll(hardwareMap.colorSensor.keySet());
            deviceNames.addAll(hardwareMap.gyroSensor.keySet());
            deviceNames.addAll(hardwareMap.voltageSensor.keySet());

            // Motor state, DeviceMapping.get() returns null when not yet active
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

            sensorValues.clear();
            for (String name : hardwareMap.colorSensor.keySet()) {
                try {
                    ColorSensor cs = hardwareMap.colorSensor.get(name);
                    if (cs != null) sensorValues.put(name + "_red", (double) cs.red());
                } catch (Exception ignored) {}
            }
            for (String name : hardwareMap.gyroSensor.keySet()) {
                try {
                    GyroSensor gs = hardwareMap.gyroSensor.get(name);
                    if (gs != null) sensorValues.put(name + "_heading", gs.getHeading());
                } catch (Exception ignored) {}
            }

            try {
                VoltageSensor vs = hardwareMap.voltageSensor.iterator().next();
                voltage = vs.getVoltage();
            } catch (Exception ignored) {}
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
    public double getRobotHeadingDegrees() {
        lock.readLock().lock(); try { return robotHeadingDegrees; } finally { lock.readLock().unlock(); }
    }
    public double getVoltage() {
        lock.readLock().lock(); try { return voltage; } finally { lock.readLock().unlock(); }
    }
    public double getElapsedTime() {
        lock.readLock().lock(); try { return elapsedTime; } finally { lock.readLock().unlock(); }
    }
    public double getSensorValue(String key) {
        lock.readLock().lock(); try { return sensorValues.getOrDefault(key, 0.0); } finally { lock.readLock().unlock(); }
    }
    public Map<String, Double> getSensorValues() {
        lock.readLock().lock(); try { return new HashMap<>(sensorValues); } finally { lock.readLock().unlock(); }
    }
}
