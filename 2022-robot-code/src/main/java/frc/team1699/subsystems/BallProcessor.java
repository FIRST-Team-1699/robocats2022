package frc.team1699.subsystems;

import frc.team1699.utils.controllers.SpeedControllerGroup;

public class BallProcessor {

    public enum BallProcessState {
        INIT, //started
        EMPTY, //no balls and no hopper spin
        COLLECTING, //sucking in balls
        RETRACTING, //spin hopper for a bit then turn it off and bring up intake
        LOADED, //1 or 2 balls and hopper is off
        AIM, //aims with the limelight and also spins up motors
        SHOOTING   //retracts the hopper stopper and pushes balls to shoot them
    }
    private final Shooter shooter;
    private final IntakeHopper intakeHopp;
    private BallProcessState wantedState;
    private BallProcessState currentState;

    public BallProcessor(final Shooter shooter, final IntakeHopper intakeHopp) {
        this.shooter = shooter;
        this.intakeHopp = intakeHopp;
    }

    //woah look its the update method! it gets run e'ry periodic update!
    public void update() {
        //yeah so if ur already doing the thing u wanna do, u dont gotta change!
        if (currentState == wantedState) {
            return;
        }
        switch(wantedState){
            case INIT:
            break;

            case EMPTY:
            break;

            case COLLECTING:
            break;

            case RETRACTING:

            case LOADED:
            break;

            case AIM:
            break;

            case SHOOTING:
            break;
        }
    }

    public BallProcessState getWantedState() {
        return wantedState;
    }

    public void setWantedState(BallProcessState wantedState) {
        this.wantedState = wantedState;
    }

}
