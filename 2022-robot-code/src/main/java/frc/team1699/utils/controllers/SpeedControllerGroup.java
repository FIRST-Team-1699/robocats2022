package frc.team1699.utils.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SpeedControllerGroup{

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

	//TODO Need to convert to use voltage instead of percent
	public void set(final double percent){
		this.master.set(percent);
		for(BetterSpeedController controller : controllers){
			controller.set(percent);
		}
	}

	public double get(){
		return this.master.get();
	}

	//TODO Figure out how to generalize talon motion control

}
