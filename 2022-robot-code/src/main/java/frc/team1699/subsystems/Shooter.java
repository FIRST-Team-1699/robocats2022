package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.team1699.utils.Utils;
//import frc.team1699.utils.sensors.BeamBreak;
import frc.team1699.utils.sensors.LimitSwitch;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

//TODO Fix
//public class Shooter implements Subsystem{
public class Shooter {

    static final double kDt = 0.05;
    //Max Velocity
    static final double kMaxVelocity = 2.0;
    //Min Velocity
    static final double kMinVelocity = 0.0;
    //Max voltage to be applied
    static final double kMaxVoltage = 12.0;
    //Max voltage when zeroing
    static final double kMaxZeroingVoltage = 4.0;
    //Control loop constants
    static final double Kp = 40.0;
    static final double Kv = 0.01;
 //   private final LimitSwitch beamBreak;
    
    double lastError = 0.0;
    double filteredGoal = 0.0;
    private double goal = 0.0;
    private ShooterState currentState = ShooterState.UNINITIALIZED, wantedState;
    private HoodPosition currentPosition;
    private final DoubleSolenoid hoodSolenoid;
    
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
        this.hoppaStoppa = hoppaStoppa; // HOPPER STOPPER
        this.currentPosition = HoodPosition.DOWN;
        
        starMotor.setInverted(true); //TODO check which one of these should be inverted
        starMotor.setSensorPhase(true); // i think this flips the encoder but frankly im not sure


      //  this.beamBreak = beamBreak;
    }

    public void update() {
        encoderRate = shooterMotorPort.getSelectedSensorVelocity(); ;//TODO see if the encoders are both the right direction so we can average them together.
        switch (currentState) {
            case UNINITIALIZED:
                currentState = ShooterState.RUNNING;
                filteredGoal = encoderRate;
                deployHopperStopper();
                break;

            case RUNNING:
                filteredGoal = goal;
                break;

            case SHOOT:
                filteredGoal = goal;
                if (atGoal()) {
                    
                }
                break;

            case STOPPED:
                filteredGoal = 0.0;
                controllerGroup.set(0.0);
                return;

            default:
                currentState = ShooterState.UNINITIALIZED;
                break;
        }

        final double error = filteredGoal - encoderRate;
        final double vel = (error - lastError) / kDt;
        lastError = error;
        final double voltage = Kp * error + Kv * vel;

        final double maxVoltage = currentState == ShooterState.RUNNING ? kMaxVoltage : kMaxZeroingVoltage;

        if (voltage >= maxVoltage) {
            shooterMotorPort.set(TalonSRXControlMode.Current, Math.min(voltage, maxVoltage));
        } else {
            controllerGroup.set(Math.max(voltage, -maxVoltage));
        }
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(final double goal) {
        this.goal = goal;
    }

    public void setWantedState(final ShooterState wantedState) {
        this.wantedState = wantedState;
        handleStateTransition(wantedState);
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
    public boolean atGoal() {
        //TODO Check tolerance
        return Utils.epsilonEquals(lastError, 0, 10.0);
    }
    
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