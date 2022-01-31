package frc.team1699.utils.controllers.talon;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import frc.team1699.utils.controllers.BetterSpeedController;

public class BetterTalon extends BetterSpeedController {

    private final TalonSRX talon;

    public BetterTalon(final int port) {
        this.talon = new TalonSRX(port);
    }

    public void set(final double percent) {
        talon.set(ControlMode.PercentOutput, percent);
    }

    public void set(final ControlMode mode, final double out) {
        talon.set(mode, out);
    }

    public double get() {
        return talon.getMotorOutputPercent();
    }

    public TalonSRX getTalon(){
        return talon;
    }
}