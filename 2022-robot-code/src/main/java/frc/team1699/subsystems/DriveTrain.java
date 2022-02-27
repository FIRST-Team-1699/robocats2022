package frc.team1699.subsystems;


import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.Joystick;
import frc.team1699.utils.sensors.LimeLight;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.music.Orchestra;//NO I WILL NOT DELETE THIS IMPORT, IT IS IMPORTANT TO ME
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;

public class DriveTrain {

    //Constants TODO Change https://docs.wpilib.org/en/latest/docs/software/examples-tutorials/trajectory-tutorial/entering-constants.html
    private final TalonFX portDrive1, portDrive2, portDrive3, starDrive1, starDrive2, starDrive3;
    private final Joystick joystick;
    private DriveState systemState, wantedState;
    private double portCommand, starCommand;

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

        //oh hey i dont know which directions these should go
        starDrive1.setInverted(TalonFXInvertType.Clockwise); //TODO check the directions u big dumb idiot
        portDrive1.setInverted(TalonFXInvertType.CounterClockwise); //TODO check the directions u big dumb idiot

        starDrive2.setInverted(TalonFXInvertType.FollowMaster);
        starDrive3.setInverted(TalonFXInvertType.FollowMaster);

        portDrive2.setInverted(TalonFXInvertType.FollowMaster);
        portDrive3.setInverted(TalonFXInvertType.FollowMaster);

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
                runArcadeDrive(joystick.getX(), -joystick.getY());
                break;
            case GOAL_TRACKING:
                //TODO do goal tracking

                break;
            default:
                break;
        }
    }

    //WPILib Differential Drive
    protected void runArcadeDrive(double throttle, double rotate) {
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