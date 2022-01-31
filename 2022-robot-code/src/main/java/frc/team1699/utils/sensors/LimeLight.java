package frc.team1699.utils.sensors;

import edu.wpi.first.networktables.NetworkTableInstance;

public class LimeLight {

    private static LimeLight instance;
    private final NetworkTableInstance table;

    private LimeLight() {
        table = NetworkTableInstance.getDefault();
    }

    public static LimeLight getInstance() {
        if (instance == null) {
            instance = new LimeLight();
        }
        return instance;
    }

    public double getTX() {
        return table.getTable("limelight").getEntry("tx").getDouble(0);
    }

    public double getTY() {
        return table.getTable("limelight").getEntry("ty").getDouble(0);
    }

    public void turnOff() {
        table.getTable("limelight").getEntry("ledMode").setNumber(1);
    }

    public void turnOn() {
        table.getTable("limelight").getEntry("ledMode").setNumber(3);
    }

    public void blink() {
        table.getTable("limelight").getEntry("ledMode").setNumber(2);
    }
}