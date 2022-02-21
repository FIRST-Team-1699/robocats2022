package frc.team1699.utils.controllers;

import java.util.Arrays;
import java.util.List;

import com.ctre.phoenix.motorcontrol.can.BaseTalon;

import java.util.ArrayList;

public class SpeedControllerGroup{

	private boolean followerReversed = false;
	private BetterSpeedController master;
	private List<BetterSpeedController> controllers;

	public SpeedControllerGroup(final BetterSpeedController master){
		this.master = master;
		controllers = new ArrayList<>();
	}

	public SpeedControllerGroup(final BetterSpeedController master, BetterSpeedController ... controllers){
		this.master = master;
		this.controllers = new ArrayList<>();
		this.controllers.addAll(Arrays.asList(controllers));
	}

	public void set(final double percent){
		this.master.set(percent);
		for(BetterSpeedController controller : controllers){
			if (followerReversed){
				controller.set(-1*percent);
			} else {
				controller.set(percent);
			}
		}
	}

	public double get(){
		return this.master.get();
	}
	public BetterSpeedController getMaster(){
		return master;
	}

	//a simple method to make all followers spin backwards. this is for the 2022 shooter design specifically
	public void setFollowerReversed() {
		followerReversed = true;
	}

	//TODO Figure out how to generalize talon motion control

}
