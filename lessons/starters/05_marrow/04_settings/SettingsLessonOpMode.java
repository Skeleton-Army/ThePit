package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.skeletonarmy.marrow.prompts.BooleanPrompt;
import com.skeletonarmy.marrow.prompts.OptionPrompt;
import com.skeletonarmy.marrow.prompts.ValuePrompt;
import com.skeletonarmy.marrow.settings.SettingsOpMode;

@TeleOp(name = "04. Settings OpMode", group = "Lessons")
public class SettingsLessonOpMode extends SettingsOpMode {

    enum Alliance { RED, BLUE }

    @Override
    public void defineSettings() {
        add("debug_mode", "Debug Mode",
            new BooleanPrompt("Enable debug mode?", false));
        add("max_speed", "Max Speed",
            new ValuePrompt("Max Speed", Double.class, 0.0, 1.0, 0.8, 0.1));
        add("alliance", "Alliance",
            new OptionPrompt("Select Alliance", Alliance.class));
    }
}
