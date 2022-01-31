package frc.team1699.utils.controllers.falcon;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.BaseTalon;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.controllers.BetterSpeedController;

public class BetterFalcon extends BetterSpeedController {

    private final int port;
    private final TalonFX talonFX;
    private ControlMode controlMode;

    public BetterFalcon(final int port) {
        this.port = port;
        this.controlMode = ControlMode.PercentOutput;
        talonFX = new TalonFX(port);
    }

    public BetterFalcon(final int port, final ControlMode controlMode) {
        this.port = port;
        this.controlMode = controlMode;
        talonFX = new TalonFX(port);
    }

    public BetterFalcon(final int port, final boolean inverted) {
        this.port = port;
        this.controlMode = ControlMode.PercentOutput;
        talonFX = new TalonFX(port);
        talonFX.setInverted(inverted);
    }

    public BetterFalcon(final int port, final ControlMode controlMode, final boolean inverted) {
        this.port = port;
        this.controlMode = controlMode;
        talonFX = new TalonFX(port);
        talonFX.setInverted(inverted);
    }

    public void set(final ControlMode controlMode, final double out) {
        talonFX.set(controlMode, out);
    }

    @Override
    public void set(double percent) {
        talonFX.set(controlMode, percent);
    }

    @Override
    public double get() {
        return talonFX.getMotorOutputPercent();
    }

    public double getEncoder() {
        return talonFX.getSensorCollection().getIntegratedSensorPosition(); //TODO check correct or if should be absolute
    }

    public double getEncoderRate() {
        return talonFX.getSensorCollection().getIntegratedSensorVelocity();
    }

    public void resetEncoders() {
        //TODO Implement
    }

    public BaseTalon getTalon() {
        return talonFX;
    }

}