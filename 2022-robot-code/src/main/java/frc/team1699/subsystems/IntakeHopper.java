package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.team1699.utils.controllers.talon.BetterTalon;

public class IntakeHopper implements Subsystem {

    public static final double kIntakeSpeed = 0.5; //TODO Give value

    //Start the system in an uninitialized state and set a wanted state
    private IntakeStates currentState = null;
    private IntakeStates wantedState;

    private final DoubleSolenoid solenoid;
    private final BetterTalon speedController;
    private final DoubleSolenoid hoppaStoppa;

    public IntakeHopper(final DoubleSolenoid solenoid, final BetterTalon speedController, final DoubleSolenoid hoppaStoppa) {
        wantedState = IntakeStates.STORED;
        this.solenoid = solenoid;
        this.speedController = speedController;
        this.hoppaStoppa = hoppaStoppa;
    }

    public void update() {
        //We do not have to change states
        if (currentState == wantedState) {
            return;
        }

        //TODO Might need to wait for wheels to stop and solenoid to deploy
        if (wantedState == IntakeStates.STORED) {
            //Store intake and turn off intake wheels
            solenoid.set(DoubleSolenoid.Value.kReverse); //TODO Check direction
            speedController.set(0.0);
            currentState = wantedState;
        } else if (wantedState == IntakeStates.DEPLOYED) {
            //Deploy intake and turn on intake wheels
            solenoid.set(DoubleSolenoid.Value.kForward); //TODO Check direction
            speedController.set(kIntakeSpeed);
            currentState = wantedState;
        } else if (wantedState == IntakeStates.RUNHOP){
            //if u want the hopper to run while the intake arm is up (indexing i guess??)
            solenoid.set(DoubleSolenoid.Value.kReverse); //TODO Check direction
            speedController.set(kIntakeSpeed);
            currentState = wantedState;
        } else if (wantedState == IntakeStates.BACKDRIVE){
            solenoid.set(DoubleSolenoid.Value.kReverse); //TODO Check direction
            speedController.set(-kIntakeSpeed);
        }
    }

    public IntakeStates getWantedState() {
        return wantedState;
    }

    public void setWantedState(final IntakeStates wantedState) {
        this.wantedState = wantedState;
    }

    enum IntakeStates {
        DEPLOYED,
        STORED,
        RUNHOP,
        BACKDRIVE
    }
}