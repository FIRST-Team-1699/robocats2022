package frc.team1699.subsystems;

import frc.team1699.subsystems.IntakeHopper.IntakeStates;
import frc.team1699.subsystems.Shooter.ShooterState;

public class BallProcessor {
    public enum BallProcessState {
        INIT, //started
        EMPTY, //no balls and no hopper spin
        COLLECTING, //sucking in balls
        RETRACTING, //spin hopper for a bit then turn it off and bring up intake
        LOADED, //1 or 2 balls and hopper is off
        SHOOTING   //retracts the hopper stopper and pushes balls to shoot them
    }
    private final Shooter shooter;
    private final IntakeHopper intakeHopp;
    private BallProcessState currentState = BallProcessState.INIT;

    private int shootingTicks = 0;
    private final int maxShootingTicks = 50; //the maximum time that the hopper should try to push into the shooter

    private int ballsInMe = 1;//TODO make it count the balls if theres a limit switch

    private int retractingTicks = 0;

    public BallProcessor(final Shooter shooter, final IntakeHopper intakeHopp) {
        this.shooter = shooter;
        this.intakeHopp = intakeHopp;
    }

    //woah look its the update method! it gets run e'ry periodic update!
    public void update() {

        switch(currentState){
            case INIT:
                currentState = BallProcessState.LOADED;
            break;

            case EMPTY:
                intakeHopp.setWantedState(IntakeStates.STORED);
                shooter.setWantedState(ShooterState.RUNNING);
            break;

            case COLLECTING:
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
                    currentState = BallProcessState.LOADED;
                }
            break;

            case LOADED:
            //TODO know how many balls are inside??
            break;

            case SHOOTING: //TODO use drivtrain to do aiming
                
                //stop feeding if it's been going for too long (ie. if its empty)
                if (shootingTicks >= maxShootingTicks) { //TODO check number
                    //checks if the hopper has been trying to push a ball into the hopper for over a second
                    currentState = BallProcessState.EMPTY;

                    shootingTicks = 0;
                    break;
                }

                if (shooter.shooterAtSpeed) { //this only occurs if the shooter is at speed for some time

                    //starts feeding
                    intakeHopp.setWantedState(IntakeStates.RUNHOP); //push balls into the shooter
                    shootingTicks ++;

                } else { //still spinning up
                    intakeHopp.setWantedState(IntakeStates.STORED);
                    shootingTicks = 0;
                }
                break;
        }
    }
    public void startShooting(){
        currentState = BallProcessState.SHOOTING;
        shootingTicks = 0;
        shooter.setWantedState(ShooterState.SHOOT);
        //TODO make the drivetrain start aiming
    }

}
