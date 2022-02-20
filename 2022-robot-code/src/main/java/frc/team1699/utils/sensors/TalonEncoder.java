package frc.team1699.utils.sensors;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import frc.team1699.utils.controllers.talon.BetterTalon;

public class TalonEncoder extends BetterEncoder{

    private final BetterTalon talon;

    public TalonEncoder(final BetterTalon talon){
        super(0);
        this.talon = talon;
        talon.getTalon().configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute); //TODO Check correct sensor
    }

    @Override
    public double getRate() {
        return talon.getTalon().getSelectedSensorVelocity();
    }

    @Override
    public double get() {
        return talon.getTalon().getSelectedSensorPosition();
    }
}