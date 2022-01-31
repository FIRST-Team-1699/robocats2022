package frc.team1699.utils.controllers.talon;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import frc.team1699.utils.controllers.BetterSpeedController;

public class TalonWrapper extends BetterSpeedController{

	private final TalonSRX talon;

	public TalonWrapper(final int port){
		this.talon = new TalonSRX(port);
	}

	public void set(final double percent){
		talon.set(ControlMode.PercentOutput, percent);
	}

	public double get(){
		return talon.getMotorOutputPercent();
	}
}
