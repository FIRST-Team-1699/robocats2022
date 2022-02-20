package frc.team1699.subsystems;

public class BallProcessor {
    //them's're the states
    //unites states of ameri-cats
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

    public BallProcessor(final Shooter shooter, final IntakeHopper intakehopp) {
        this.shooter = shooter;
        this.intakeHopp = intakehopp;
    }




    public BallProcessState getWantedState() {
        return wantedState;
    }

    public void setWantedState(BallProcessState wantedState) {
        this.wantedState = wantedState;
    }

}
