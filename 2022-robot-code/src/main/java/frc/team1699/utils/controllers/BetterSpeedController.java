package frc.team1699.utils.controllers;

import com.ctre.phoenix.motorcontrol.can.BaseTalon;

public abstract class BetterSpeedController {

    public BetterSpeedController() {

    }

    public abstract void set(final double percent);

    public abstract double get();

    public BaseTalon getTalon() {
        return null;
    }
}