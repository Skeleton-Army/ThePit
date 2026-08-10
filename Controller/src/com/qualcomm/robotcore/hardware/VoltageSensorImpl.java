package com.qualcomm.robotcore.hardware;

import virtual_robot.lessons.SimState;

public class VoltageSensorImpl implements VoltageSensor{

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Unknown;
    }

    @Override
    public String getDeviceName() {
        return "Virtual Robot Voltage Sensor";
    }

    @Override
    public String getConnectionInfo() {
        return "Virtual Robot Connection";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
    }

    @Override
    public void close() {
    }

    @Override
    public double getVoltage() {
        return SimState.getInstance().getDynamicVoltage();
    }
}
