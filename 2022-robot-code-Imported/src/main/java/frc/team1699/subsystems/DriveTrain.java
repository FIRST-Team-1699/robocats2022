package frc.team1699.subsystems;


import edu.wpi.first.wpilibj.Joystick;
import frc.team1699.utils.sensors.LimeLight;
import frc.team1699.utils.Utils;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.music.Orchestra; //NO I WILL NOT DELETE THIS IMPORT, IT IS IMPORTANT TO ME

import java.util.ArrayList;

import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import frc.team1699.Constants;
import frc.robot.Robot;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.util.Units;

public class DriveTrain {

    //Constants TODO Change https://docs.wpilib.org/en/latest/docs/software/examples-tutorials/trajectory-tutorial/entering-constants.html
    private final TalonFX portDrive1, portDrive2, portDrive3, starDrive1, starDrive2, starDrive3;
    private final Joystick joystick;
    private DriveState systemState, wantedState;
    private double portCommand, starCommand;

    private DifferentialDriveKinematics kinematics;


    private final int kRampTotal = 25; // in ticks, 50 ticks in a second, do the math u lazy dummy
    private int rampTicks = 0;
    private boolean isZero = true;
    private boolean isRamping = false;

    public boolean aligned = false;

    //aiming constants
    final double kSteerP = 0.025;
    final double kSteerI = 0.03;
    final double kSteerD = 0.0005;

    final double kDrive = 0.06;

    double autoFwdDemand = 0.0;
    double autoTurnDemand = 0.0;

    private AHRS gyro;
    private PIDController driveToTargetLoop;
    private PIDController autoAimLoop;
    private PIDController autoBalanceLoop;

    // P = .025, I = 0, D = .002
    final double kBalanceP = .025;
    final double kBalanceI = 0;
    final double kBalanceD = 0.001;
    // balancing stuff
    private int balancingTicks = 0;
    private double balancingSpeed = 0.0;

    private final double deadzone = .3;

    public boolean isBalanced = false;

    private RamseteController ramsetinator;

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

        kinematics = new DifferentialDriveKinematics(Units.inchesToMeters(27.0));
        
        driveToTargetLoop = new PIDController(-1.0/60.0, 0, 0);
        driveToTargetLoop.setTolerance(3.0);

        autoAimLoop = new PIDController(kSteerP, kSteerI, kSteerD);

        autoBalanceLoop = new PIDController(kBalanceP, kBalanceI, kBalanceD);
        autoBalanceLoop.setTolerance(1, .325);

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
        ramsetinator = new RamseteController();
    }

    private Trajectory trajectory;
    public void generateTrajectory() {

        // start and end waypoints
        var startPoint = new Pose2d(0, 0,
            Rotation2d.fromDegrees(90));
        var endPoint = new Pose2d(0, 0.3,
            Rotation2d.fromDegrees(90));
    
        // // list of other waypoints
        var interiorWaypoints = new ArrayList<Translation2d>();
        // interiorWaypoints.add(new Translation2d(Units.feetToMeters(14.54), Units.feetToMeters(23.23)));
        // interiorWaypoints.add(new Translation2d(Units.feetToMeters(21.04), Units.feetToMeters(18.23)));

    
        TrajectoryConfig config = new TrajectoryConfig(Units.feetToMeters(12), Units.feetToMeters(12));
        config.setReversed(true);
    
        trajectory = TrajectoryGenerator.generateTrajectory(
            startPoint,
            interiorWaypoints,
            endPoint,
            config);
        
      }



    public void update() {
        // System.out.println(trajectory.toString());
        // System.out.println("Gyro \"pitch\": " + ((int) (gyro.getPitch())));
        // System.out.println("Gyro \"yaw\": " + ((int) (gyro.getYaw())));
        // System.out.println("Gyro \"roll\": " + ((int) gyro.getRoll()));


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
                //System.out.println("manual");
                break;
            case GOAL_TRACKING:

                LimeLight.getInstance().turnOn();
                if (LimeLight.getInstance().getTV() > 0){
                    runArcadeDrive(-(autoAimLoop.calculate(LimeLight.getInstance().getTX())), 0);
                   System.out.println(-(autoAimLoop.calculate(LimeLight.getInstance().getTX())));
                } else {
                    runArcadeDrive(0,0);
                }
            //     if (LimeLight.getInstance().getTV() > 0){
            //         runArcadeDrive(LimeLight.getInstance().getTX()*kSteer, -(LimeLight.getInstance().getTY()-20)*kDrive*0);
            //     } else {
            //         runArcadeDrive(0, 0);
            //   //      System.out.println("no target");
            //     }


                break;
            case AUTONOMOUS:
                runArcadeDrive(autoTurnDemand, autoFwdDemand);
            break;
            case BALANCING:
                //does PID things
                double pitch = gyro.getPitch() - 4;
                double balSpeed = autoBalanceLoop.calculate(pitch);

                // make sure it aint balanced
                if(pitch < 3 && pitch > -3) {
                    runArcadeDrive(0, 0);
                } else {
                    //System.out.println(balSpeed);
                    runArcadeDrive(0, balSpeed);
                }
                // System.out.println(gyro.getYaw());
                break;
            case POSITIONING:
                if (LimeLight.getInstance().getTV() > 0){
                    runArcadeDrive(0, driveToTargetLoop.calculate(LimeLight.getInstance().getDistanceFromTarget(), 45.0));
                    //System.out.println(driveToTargetLoop.calculate(LimeLight.getInstance().getDistanceFromTarget(), 45.0));
                }
                break;
            case SPINNING:
                runArcadeDrive(.8, 0);
                break;
            case SPLINING:
                Trajectory.State goal = trajectory.sample(3.4); // sample the trajectory at 3.4 seconds from the beginning
                //ChassisSpeeds adjustedSpeeds = ramsetinator.calculate(currentRobotPose, goal); TODO make currentrobot position updating
                //https://docs.wpilib.org/en/stable/docs/software/advanced-controls/trajectories/ramsete.html
            
                // the stuff for spline following
                // and kinematics calculations
                // goes in here

                break;
            default:
                break;
        }
    }

    //WPILib Differential Drive
    public void runArcadeDrive(double rotate, double throttle) {
        //System.out.println(rotate);
        double portOutput = 0.0;
        double starOutput = 0.0;
        if (Math.abs(rotate) <= deadzone && systemState == DriveState.MANUAL){
            rotate = 0;   
        }
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
        BALANCING,
        POSITIONING,
        SPINNING,
        SPLINING
    }
}