package virtual_robot.lessons;

import java.util.Map;

public class CompletionCheck {
    public enum Type {
        OPMODE_RUNNING, HARDWARE_MAPPED, MOTOR_POWER, ENCODER_TICKS, SERVO_POSITION,
        ROBOT_POSITION, HEADING, VOLTAGE, SENSOR_VALUE, ELAPSED_TIME
    }

    public final Type type;
    public final String name;
    public final double target;
    public final double tolerance;
    public final double x;
    public final double y;
    public final int minimum;
    public final double minimumValue;
    public final double maximumValue;

    private CompletionCheck(Type type, String name, double target, double tolerance,
                             double x, double y, int minimum, double minimumValue, double maximumValue) {
        this.type = type;
        this.name = name;
        this.target = target;
        this.tolerance = tolerance;
        this.x = x;
        this.y = y;
        this.minimum = minimum;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
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
            case "heading":          type = Type.HEADING;         break;
            case "voltage":          type = Type.VOLTAGE;         break;
            case "sensor_value":     type = Type.SENSOR_VALUE;    break;
            case "elapsed_time":     type = Type.ELAPSED_TIME;    break;
            default: throw new IllegalArgumentException("Unknown check type: " + typeStr);
        }
        String name    = (String) map.getOrDefault("name", "");
        double target  = toDouble(map.get("target"));
        double tolerance = toDouble(map.get("tolerance"));
        double x       = toDouble(map.get("x"));
        double y       = toDouble(map.get("y"));
        int minimum    = map.containsKey("minimum") ? (int) toDouble(map.get("minimum")) : 0;
        double minVal  = toDouble(map.get("minimumValue"));
        double maxVal  = toDouble(map.get("maximumValue"));
        return new CompletionCheck(type, name, target, tolerance, x, y, minimum, minVal, maxVal);
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        return (double)(Double) o;
    }
}
