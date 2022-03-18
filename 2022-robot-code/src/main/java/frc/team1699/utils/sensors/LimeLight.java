package frc.team1699.utils.sensors;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class LimeLight {

    private static LimeLight instance;
    private final NetworkTable table;

    private LimeLight() {
        table = NetworkTableInstance.getDefault().getTable("limelight");
    }

    public static LimeLight getInstance() {
        if (instance == null) {
            instance = new LimeLight();
        }
        return instance;
    }

    public double getTV() {
        return table.getEntry("tv").getDouble(0);
    }

    public double getTX() {
        return table.getEntry("tx").getDouble(0);
    }

    public double getTY() {
        return table.getEntry("ty").getDouble(0);
    }

    public void turnOff() {
        table.getEntry("ledMode").setNumber(1);
    }

    public void turnOn() {
        table.getEntry("ledMode").setNumber(3);
    }

    public void blink() {
        table.getEntry("ledMode").setNumber(2);
    }
}