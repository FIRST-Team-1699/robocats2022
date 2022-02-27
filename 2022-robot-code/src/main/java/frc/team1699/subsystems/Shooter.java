package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.team1699.utils.Utils;
//import frc.team1699.utils.sensors.BeamBreak;
import frc.team1699.utils.sensors.LimitSwitch;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

//TODO Fix
//public class Shooter implements Subsystem{ 
public class Shooter {

    //Control loop constants
    static final int kP = 0;
    static final int kI = 0;
    static final int kD = 0;
    static final int kF = 0;

    //error variables
    int kErrThreshold = 10; // how many sensor units until its close-enough
    int kLoopsToSettle = 5; // how many loops sensor must be close-enough
    int _withinThresholdLoops = 0; //counter for error threshold

    static final int kPIDLoopIDX = 0; //just leave this at 0 its for if u want more than 1 loop
    
    private double targetVelocity_UnitsPer100ms = 0.0;

    private double goal = 0.0;
    private ShooterState currentState = ShooterState.UNINITIALIZED, wantedState;
    private HoodPosition currentPosition;
    private final DoubleSolenoid hoodSolenoid;

    private final int kTimeoutMs = 30;
    
    //hoppaStoppa is the flipper in the hopper on a double action solenoid that stops the balls from going into the shooter
    //when it is deployed, the balls can't move through to the shooter
    private final DoubleSolenoid hoppaStoppa;
    public static boolean stopperDeployed = false;

    private double encoderRate;
    private TalonSRX shooterMotorPort;
    private TalonSRX shooterMotorStar;
    private TalonSRXControlMode TalonSRXControlMode;

    public Shooter(final TalonSRX portMotor, final TalonSRX starMotor, final DoubleSolenoid hoodSolenoid, final DoubleSolenoid hoppaStoppa) {
        this.shooterMotorPort = starMotor;
        this.shooterMotorStar = portMotor;
        this.hoodSolenoid = hoodSolenoid;
        this.hoppaStoppa = hoppaStoppa; //this is the hopper stopper, the stopper in the hopper. its stops the balls. NO I WILL NOT CHANGE ITS NAME.
        this.currentPosition = HoodPosition.DOWN;
        

        starMotor.setInverted(true);
        portMotor.follow(starMotor);
        portMotor.setInverted(InvertType.OpposeMaster);
        starMotor.setSensorPhase(false); //TODO check direction


        starMotor.configNominalOutputForward(0, kTimeoutMs);
        starMotor.configNominalOutputReverse(0, kTimeoutMs);
        starMotor.configPeakOutputForward(1, kTimeoutMs);
        starMotor.configPeakOutputReverse(-1, kTimeoutMs);

        starMotor.config_kP(kPIDLoopIDX, kP, kTimeoutMs);
        starMotor.config_kI(kPIDLoopIDX, kI, kTimeoutMs);
        starMotor.config_kD(kPIDLoopIDX, kD, kTimeoutMs);
        starMotor.config_kF(kPIDLoopIDX, kF, kTimeoutMs);


      //  this.beamBreak = beamBreak;
    }

        /* Checks if the motor has reached the target velocity.*/
    private void checkErr() {
        if (shooterMotorStar.getClosedLoopError() < +kErrThreshold &&
            shooterMotorStar.getClosedLoopError() > -kErrThreshold) {

            ++_withinThresholdLoops;
        } else {
            _withinThresholdLoops = 0;
            }
        }

    public void update() {
        
        switch (currentState) {
            case UNINITIALIZED:
                currentState = ShooterState.RUNNING;

                deployHopperStopper();
                break;

            case RUNNING:

                break;

            case SHOOT:

                break;

            case STOPPED:

                shooterMotorPort.set(TalonSRXControlMode.PercentOutput, 0.0);
                return;

            default:
                currentState = ShooterState.UNINITIALIZED;
                break;
        }
    }

    public void setWantedState(final ShooterState wantedState) {
        this.wantedState = wantedState;
        handleStateTransition(wantedState);
    }

    public boolean reachedGoal() {
        return (_withinThresholdLoops > kLoopsToSettle);
    }

    private void handleStateTransition(final ShooterState wantedState){
        switch(wantedState){
            case UNINITIALIZED:
            case STOPPED:
                break;
            case RUNNING:
                goal = 1.0;
                break;
            case SHOOT:
                goal = 20.0;
                break;
        }
    }

    public ShooterState getCurrentState() {
        return currentState;
    }

    /**
     * Checks if the shooter had reached target velocity
     *
     * @return True if we have reached the target velocity, false otherwise
     */

    
    //make the hood do stuff
    public void toggleHood() {
        hoodSolenoid.toggle();
        if (currentPosition == HoodPosition.UP){
            currentPosition = HoodPosition.DOWN;
        } else {
            currentPosition = HoodPosition.UP;
        }
    }
    public boolean isHoodUp(){
        if (currentPosition == HoodPosition.UP){
            return true;
        } else {
            return false;
        }
    }  
    public void hoodUp(){
        if (isHoodUp()){
            return;
        } else {
            hoodSolenoid.toggle();
            currentPosition = HoodPosition.UP;
        }
    }
    public void hoodDown() {
        if (!isHoodUp()){
            return;
        } else {
            hoodSolenoid.toggle();
            currentPosition = HoodPosition.DOWN;
        }
    }

    //methods for the hopper stopper! they public so they can be used in the ball processor
    public void toggleHopperStopper() {
        hoppaStoppa.toggle();
        stopperDeployed = !stopperDeployed;
    }

    public void deployHopperStopper() {
        hoppaStoppa.set(DoubleSolenoid.Value.kForward);
        stopperDeployed = true;
    }

    public void retractHopperStopper() {
        hoppaStoppa.set(DoubleSolenoid.Value.kReverse);
        stopperDeployed = false;
    }


    enum HoodPosition {
        UP,
        DOWN
    }

    enum ShooterState {
        UNINITIALIZED,
        RUNNING,
        SHOOT,
        STOPPED
    }
}