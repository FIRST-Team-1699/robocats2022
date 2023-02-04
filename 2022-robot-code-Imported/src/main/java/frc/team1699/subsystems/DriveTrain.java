package frc.team1699.subsystems;


import edu.wpi.first.wpilibj.Joystick;
import frc.team1699.utils.sensors.LimeLight;
import frc.team1699.utils.Utils;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.music.Orchestra; //NO I WILL NOT DELETE THIS IMPORT, IT IS IMPORTANT TO ME
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import frc.team1699.Constants;

import com.kauailabs.navx.frc.AHRS;

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

    public boolean aligned = false;

    //aiming constants
    //final double kSteer = 0.05; THE ORIGINAL VALUE
    final double kSteer = 0.040;
    final double kDrive = 0.06;

    double autoFwdDemand = 0.0;
    double autoTurnDemand = 0.0;

    private AHRS gyro;

    // balancing stuff
    private int balancingTicks = 0;
    private double balancingSpeed = 0.0;

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

        gyro = new AHRS();
        
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

        System.out.println("Gyro \"pitch\": " + ((int) (gyro.getPitch())));
        // System.out.println("Gyro \"yaw\": " + ((int) (gyro.getYaw())));
        // System.out.println("Gyro \"roll\": " + ((int) gyro.getRoll()));

        // // stupid test things
        // if(gyro.getPitch() >= 0 && gyro.getPitch() <= 10) {
        //     Constants.kCoefficientOfSpeedThatGetsMultipliedToMakeTheRobotSlower = 1.0;
        // } else {
        //     Constants.kCoefficientOfSpeedThatGetsMultipliedToMakeTheRobotSlower = 0.3;
        // }


       if (systemState == wantedState) {
            runSubsystem();
            return;
        }

        if (wantedState == DriveState.MANUAL) {
            aligned = false;
            handleManualTransition();
        } else if (wantedState == DriveState.GOAL_TRACKING) {
            aligned = false;
            handleGoalTrackingTransition();
        }


        systemState = wantedState;
        runSubsystem(); 

        SmartDashboard.putNumber("LimelightX", LimeLight.getInstance().getTX());
        SmartDashboard.putNumber("LimelightY", LimeLight.getInstance().getTY());
    }

    private void handleManualTransition() {
        // LimeLight.getInstance().turnOff();
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

                LimeLight.getInstance().turnOn();
                if (LimeLight.getInstance().getTV() > 0){
                    runArcadeDrive(LimeLight.getInstance().getTX()*kSteer, -(LimeLight.getInstance().getTY()-20)*kDrive*0);
                } else {
                    runArcadeDrive(0, 0);
              //      System.out.println("no target");
                }


                break;
            case AUTONOMOUS:
                runArcadeDrive(autoTurnDemand, autoFwdDemand);
            break;
            case BALANCING:
                // double pitch = gyro.getPitch();
                // pitch -= 4.0;
                // if (pitch > 2.5){ // distance from balanced is 5
                //     balancingSpeed = -0.3;
                //     runArcadeDrive(0, balancingSpeed);
                //     System.out.println("balancing backwards");
                // } else if(pitch < -2.5){
                //     balancingSpeed = 0.3;
                //     runArcadeDrive(0, balancingSpeed);
                //     System.out.println("balancing forwards");
                // } else {
                //     runArcadeDrive(0, 0);
                //     System.out.println("balanced i hope");
                // }

                double pitch = gyro.getPitch();
                pitch -= 4.0;
                double balSpeed = -pitch * 0.04;
                if(Math.abs(balSpeed) >= 0.325) {
                    balSpeed = balSpeed / Math.abs(balSpeed) * 0.325;
                }
                if(pitch < 3 && pitch > -3) {
                    runArcadeDrive(0, 0);
                } else {
                    System.out.println(balSpeed);
                    runArcadeDrive(0, balSpeed);
                }
            default:
                break;
        }
    }

    //WPILib Differential Drive
    public void runArcadeDrive(double rotate, double throttle) {
        double portOutput = 0.0;
        double starOutput = 0.0;

        //TODO add deadband
        rotate = Math.copySign(rotate * rotate, rotate);
        throttle = Math.copySign(throttle * throttle, throttle);

        double maxInput = Math.copySign(Math.max(Math.abs(rotate), Math.abs(throttle)), rotate);

        if (rotate >= 0.0) {
            // First quadrant, else second quadrant
            if (throttle >= 0.0) {
                portOutput = maxInput;
                starOutput = rotate - throttle;
            } else {
                portOutput = rotate + throttle;
                starOutput = maxInput;
            }
        } else {
            // Third quadrant, else fourth quadrant
            if (throttle >= 0.0) {
                portOutput = rotate + throttle;
                starOutput = maxInput;
            } else {
                portOutput = maxInput;
                starOutput = rotate - throttle;
            }
        }


       // System.out.println("port: " + portOutput + " star: " + starOutput);
        portDrive1.set(TalonFXControlMode.PercentOutput, portOutput * Constants.kCoefficientOfSpeedThatGetsMultipliedToMakeTheRobotSlower);
        starDrive1.set(TalonFXControlMode.PercentOutput, starOutput * Constants.kCoefficientOfSpeedThatGetsMultipliedToMakeTheRobotSlower);
    }


    public void setWantedState(final DriveState driveState){
        wantedState = driveState;
    }

    public void setAutoDemand(double fwd, double turn){
        autoFwdDemand = fwd;
        autoTurnDemand = turn;
    }

    public enum DriveState {
        MANUAL,
        GOAL_TRACKING,
        AUTONOMOUS,
        BALANCING
    }
}