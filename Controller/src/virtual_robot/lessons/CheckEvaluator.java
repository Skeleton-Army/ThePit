package virtual_robot.lessons;

import java.util.Map;

public class CheckEvaluator {
    public static boolean evaluate(CompletionCheck check, SimState state) {
        if (check == null) return false;
        switch (check.type) {
            case OPMODE_RUNNING:
                return state.isOpModeRunning();

            case HARDWARE_MAPPED:
                return state.getDeviceNames().contains(check.name);

            case MOTOR_POWER: {
                Map<String, Double> powers = state.getMotorPowers();
                if (!powers.containsKey(check.name)) return false;
                return Math.abs(powers.get(check.name) - check.target) <= check.tolerance;
            }

            case ENCODER_TICKS: {
                Map<String, Integer> ticks = state.getEncoderTicks();
                if (!ticks.containsKey(check.name)) return false;
                return ticks.get(check.name) >= check.minimum;
            }

            case SERVO_POSITION: {
                Map<String, Double> positions = state.getServoPositions();
                if (!positions.containsKey(check.name)) return false;
                return Math.abs(positions.get(check.name) - check.target) <= check.tolerance;
            }

            case ROBOT_POSITION: {
                double dx = state.getRobotX() - check.x;
                double dy = state.getRobotY() - check.y;
                return Math.sqrt(dx * dx + dy * dy) <= check.tolerance;
            }
        }
        return false;
    }
}
