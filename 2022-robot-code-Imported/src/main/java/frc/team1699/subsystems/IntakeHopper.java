package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

public class IntakeHopper implements Subsystem {

    public static final double kIntakeSpeed = -0.63;

    //Start the system in an uninitialized state and set a wanted state
    private IntakeStates currentState = null;
    private IntakeStates wantedState;

    private final DoubleSolenoid solenoid;
    private final TalonSRX speedController;

    public IntakeHopper(final DoubleSolenoid solenoid, final TalonSRX speedController) {
        wantedState = IntakeStates.STORED;
        this.solenoid = solenoid;
        this.speedController = speedController;
        speedController.setInverted(true);
    }

    public void update() {

        //We do not have to change states
        if (currentState == wantedState) {
            return;
        }

        if (wantedState == IntakeStates.STORED) {

            //Store intake and turn off intake wheels
            solenoid.set(DoubleSolenoid.Value.kReverse);
            speedController.set(TalonSRXControlMode.PercentOutput, 0);

            currentState = wantedState;

        } else if (wantedState == IntakeStates.DEPLOYED) {

            //Deploy intake and turn on intake wheels
            solenoid.set(DoubleSolenoid.Value.kForward);
           // System.out.println("i deploy");
            speedController.set(TalonSRXControlMode.PercentOutput, kIntakeSpeed);

            currentState = wantedState;

        } else if (wantedState == IntakeStates.RUNHOP){

            //if u want the hopper to run while the intake arm is up (indexing i guess??)
            solenoid.set(DoubleSolenoid.Value.kReverse);
            speedController.set(TalonSRXControlMode.PercentOutput, kIntakeSpeed);

            currentState = wantedState;

        } else if (wantedState == IntakeStates.BACKDRIVE){

            //poop out balls
            solenoid.set(DoubleSolenoid.Value.kReverse);
            speedController.set(TalonSRXControlMode.PercentOutput, -kIntakeSpeed);

            currentState = wantedState;
        }
    }

    public IntakeStates getWantedState() {
        return wantedState;
    }

    public void setWantedState(final IntakeStates wantedState) {
        this.wantedState = wantedState;
    }

    public enum IntakeStates {
        DEPLOYED,
        STORED,
        RUNHOP,
        BACKDRIVE
    }
}