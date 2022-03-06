package frc.team1699.subsystems;


import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.Joystick;
import frc.team1699.utils.sensors.LimeLight;
import frc.team1699.utils.Utils;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.music.Orchestra;//NO I WILL NOT DELETE THIS IMPORT, IT IS IMPORTANT TO ME
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;

public class DriveTrain {

    //Constants TODO Change https://docs.wpilib.org/en/latest/docs/software/examples-tutorials/trajectory-tutorial/entering-constants.html
    private final TalonFX portDrive1, portDrive2, portDrive3, starDrive1, starDrive2, starDrive3;
    private final Joystick joystick;
    private DriveState systemState, wantedState;
    private double portCommand, starCommand;

    private final int kRampTotal = 25; // in ticks, 50 ticks in a second, do the math u lazy dummy
    private int rampTicks = 0;
    private boolean isZero = true;
    private boolean isRamping = false;

    //aiming constants
    final double kSteer = 0.06;
    final double kDrive = 0.26;

    /**
     * yuh i'ssa drivetrain
     * @param portDrive1
     * @param portDrive2
     * @param portDrive3
     * @param starDrive1
     * @param starDrive2
     * @param starDrive3
     * @param joystick
     */
    public DriveTrain(final TalonFX portDrive1, final TalonFX portDrive2, final TalonFX portDrive3, 
                      final TalonFX starDrive1, final TalonFX starDrive2, final TalonFX starDrive3, 
                      final Joystick joystick) {
        
        this.portDrive1 = portDrive1;
        this.portDrive2 = portDrive2;
        this.portDrive3 = portDrive3;
        
        //make the followers
        portDrive2.follow(portDrive1, FollowerType.PercentOutput);
        portDrive3.follow(portDrive1, FollowerType.PercentOutput);
        
        this.starDrive1 = starDrive1;
        this.starDrive2 = starDrive2;
        this.starDrive3 = starDrive3;
        
        //so many followers i should call it hinduism
        starDrive2.follow(starDrive1, FollowerType.PercentOutput);
        starDrive3.follow(starDrive1, FollowerType.PercentOutput);

        this.joystick = joystick;

        wantedState = DriveState.MANUAL;
    }

    public void update() {

       if (systemState == wantedState) {
            runSubsystem();
            return;
        }

        if (wantedState == DriveState.MANUAL) {
            handleManualTransition();
        } else if (wantedState == DriveState.GOAL_TRACKING) {
            handleGoalTrackingTransition();
        }

        systemState = wantedState;
        runSubsystem(); 

        SmartDashboard.putNumber("LimelightX", LimeLight.getInstance().getTX());
        SmartDashboard.putNumber("LimelightY", LimeLight.getInstance().getTY());
    }

    private void handleManualTransition() {
        LimeLight.getInstance().turnOff();
    }

    private void handleGoalTrackingTransition() {
        LimeLight.getInstance().turnOn();

    }

    private void runSubsystem() {
        switch (systemState) {
            case MANUAL:
                runArcadeDrive(joystick.getX(), -joystick.getY(), false);
                break;
            case GOAL_TRACKING:
                //TODO do goal tracking

                runArcadeDrive(LimeLight.getInstance().getTX()*kSteer, LimeLight.getInstance().getTY()*kDrive, false);

                break;
            default:
                break;
        }
    }

    //WPILib Differential Drive
    protected void runArcadeDrive(double throttle, double rotate, boolean doRamp) {
        double portOutput = 0.0;
        double starOutput = 0.0;

        //TODO add deadband
        throttle = Math.copySign(throttle * throttle, throttle);
        rotate = Math.copySign(rotate * rotate, rotate);

        double maxInput = Math.copySign(Math.max(Math.abs(throttle), Math.abs(rotate)), throttle);

        if (throttle >= 0.0) {
            // First quadrant, else second quadrant
            if (rotate >= 0.0) {
                portOutput = maxInput;
                starOutput = throttle - rotate;
            } else {
                portOutput = throttle + rotate;
                starOutput = maxInput;
            }
        } else {
            // Third quadrant, else fourth quadrant
            if (rotate >= 0.0) {
                portOutput = throttle + rotate;
                starOutput = maxInput;
            } else {
                portOutput = maxInput;
                starOutput = throttle - rotate;
            }
        }

        if (Utils.epsilonEquals(portOutput, 0, 0.1) && Utils.epsilonEquals(starOutput, 0, 0.1)){
            isZero = true;
            isRamping = false;
            rampTicks = 0;
            portOutput = 0;
            starOutput = 0;
        } else {
            isZero = false;
        }
        
        if (doRamp && !isZero && !isRamping) { //start ramping
            isRamping = true;
        }
        if (isRamping && rampTicks < kRampTotal) { //activley ramping
            rampTicks ++;
            portOutput = portOutput * (rampTicks / kRampTotal); //multily output by % of ticks completed
            starOutput = starOutput * (rampTicks / kRampTotal);
        }
    
       // System.out.println("port: " + portOutput + " star: " + starOutput);
        portDrive1.set(TalonFXControlMode.PercentOutput, portOutput);
        starDrive1.set(TalonFXControlMode.PercentOutput, starOutput);
    }


    public void setWantedState(final DriveState driveState){
        wantedState = driveState;
    }

    public enum DriveState {
        MANUAL,
        GOAL_TRACKING
    }
}