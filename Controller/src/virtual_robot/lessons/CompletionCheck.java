package virtual_robot.lessons;

import java.util.Map;

public class CompletionCheck {
    public enum Type {
        OPMODE_RUNNING, HARDWARE_MAPPED, MOTOR_POWER, ENCODER_TICKS, SERVO_POSITION, ROBOT_POSITION
    }

    public final Type type;
    public final String name;
    public final double target;
    public final double tolerance;
    public final double x;
    public final double y;
    public final int minimum;

    private CompletionCheck(Type type, String name, double target, double tolerance,
                             double x, double y, int minimum) {
        this.type = type;
        this.name = name;
        this.target = target;
        this.tolerance = tolerance;
        this.x = x;
        this.y = y;
        this.minimum = minimum;
    }

    @SuppressWarnings("unchecked")
    public static CompletionCheck fromMap(Map<String, Object> map) {
        String typeStr = (String) map.get("type");
        Type type;
        switch (typeStr) {
            case "opmode_running":   type = Type.OPMODE_RUNNING;  break;
            case "hardware_mapped":  type = Type.HARDWARE_MAPPED; break;
            case "motor_power":      type = Type.MOTOR_POWER;     break;
            case "encoder_ticks":    type = Type.ENCODER_TICKS;   break;
            case "servo_position":   type = Type.SERVO_POSITION;  break;
            case "robot_position":   type = Type.ROBOT_POSITION;  break;
            default: throw new IllegalArgumentException("Unknown check type: " + typeStr);
        }
        String name    = (String) map.getOrDefault("name", "");
        double target  = toDouble(map.get("target"));
        double tolerance = toDouble(map.get("tolerance"));
        double x       = toDouble(map.get("x"));
        double y       = toDouble(map.get("y"));
        int minimum    = map.containsKey("minimum") ? (int) toDouble(map.get("minimum")) : 0;
        return new CompletionCheck(type, name, target, tolerance, x, y, minimum);
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        return (double)(Double) o;
    }
}
