package frc.team1699.subsystems;

import frc.team1699.subsystems.IntakeHopper.IntakeStates;
import frc.team1699.subsystems.Shooter.ShooterState;

public class AutoBallProcessor {
    public enum AutoBallProcessState {
        INIT, //started
        EMPTY, //no balls and no hopper spin TODO emtpy and collecting can be 1 in the same
        COLLECTING, //sucking in balls
        RETRACTING, //spin hopper for a bit then turn it off and bring up intake
        LOADED, //1 or 2 balls and hopper is off
        SHOOTING,   //retracts the hopper stopper and pushes balls to shoot them
        PURGING //i frew up :(
    }
    private final Shooter shooter;
    private final IntakeHopper intakeHopp;
    private AutoBallProcessState currentState = AutoBallProcessState.INIT;

    private int shootingTicks = 0;
    private final int maxShootingTicks = 50; //the maximum time that the hopper should try to push into the shooter

    private int retractingTicks = 0;

    public AutoBallProcessor (final Shooter shooter, final IntakeHopper intakeHopp) {
        this.shooter = shooter;
        this.intakeHopp = intakeHopp;
    }

    public void setProcessorState(final AutoBallProcessState currentState) {
        this.currentState = currentState;
    }

    //woah look its the update method! it gets run e'ry periodic update!
    public void update() {

        switch(currentState){
            case INIT:
                currentState = AutoBallProcessState.LOADED;
            break;

            case EMPTY:
                intakeHopp.setWantedState(IntakeStates.STORED);
                shooter.setWantedState(ShooterState.RUNNING);
            break;

            case COLLECTING:
              //  System.out.println("collecting");
                intakeHopp.setWantedState(IntakeStates.DEPLOYED);
            break;

            case RETRACTING:
                //runs the intake mechanism for 0.5 seconds and then retracts it
                if (retractingTicks < 25) {
                    intakeHopp.setWantedState(IntakeStates.DEPLOYED);
                    retractingTicks ++;
                } else {
                    intakeHopp.setWantedState(IntakeStates.STORED);
                    retractingTicks = 0;
                    currentState = AutoBallProcessState.LOADED;
                }
            break;

            case LOADED:
                intakeHopp.setWantedState(IntakeStates.STORED);
                shooter.setWantedState(ShooterState.RUNNING);
            break;

            case SHOOTING:
                
                //stop feeding if it's been going for too long (ie. if its empty)
                if (shootingTicks >= maxShootingTicks) {
                    stopShooting();
                    break;
                }

                if (shooter.shooterAtSpeed) { //this only occurs if the shooter is at speed for some time

                    //starts feeding
                  //  System.out.println("Feeding");
                    intakeHopp.setWantedState(IntakeStates.RUNHOP); //push balls into the shooter
                    shootingTicks ++;

                } else { //still spinning up
                    intakeHopp.setWantedState(IntakeStates.STORED);
                    shootingTicks = 0;
                }
                break;
            
            case PURGING:

                intakeHopp.setWantedState(IntakeStates.BACKDRIVE);
                
            break;
        }
    }
    public void startShooting(){
        currentState = AutoBallProcessState.SHOOTING;
        shootingTicks = 0;
        shooter.hoodTransition = 0;
        shooter.setWantedState(ShooterState.SHOOT);
    }

    public void startLowerShooting(){
        currentState = AutoBallProcessState.SHOOTING;
        shootingTicks = 0;
        shooter.setWantedState(ShooterState.SHOOT);
        //default is 18k
        shooter.setShooterGoal(0.0);
    }
    
    public void stopShooting(){
        currentState = AutoBallProcessState.LOADED;
        shootingTicks = 0;
        shooter.setWantedState(ShooterState.RUNNING);
    }

}
