package frc.robot;

import frc.team1699.Constants;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.team1699.subsystems.BallProcessor;
import frc.team1699.subsystems.BallProcessor.BallProcessState;
import frc.team1699.subsystems.DriveTrain;
import frc.team1699.subsystems.DriveTrain.DriveState;
import frc.team1699.subsystems.IntakeHopper.IntakeStates;
import frc.team1699.subsystems.IntakeHopper;
import frc.team1699.subsystems.Climber;
import frc.team1699.subsystems.Shooter;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.sensors.LimitSwitch;
import frc.team1699.utils.sensors.LimeLight;


public class Robot extends TimedRobot {

    private Joystick driveJoystick, opJoystick;
    public PneumaticsModuleType CTREPCM = PneumaticsModuleType.CTREPCM;
    private DriveTrain driveTrain;
    private Climber climber;
    private IntakeHopper intakeHopp;
    private Shooter shooter;
    private BallProcessor ballProcessor;
    private TalonSRX intakeHoppTalon, shooterTalonPort, shooterTalonStar, hopperTalon;
    private TalonFX portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2;
    private Compressor compressor;
    private DoubleSolenoid intakeSolenoid, hopperStopper, shooterAngleSolenoid, climberSolenoidPort;
    //private AdaFruitBeamBreak intakeBreak, hopperBreak;
    private LimitSwitch shooterBreak;
    private int adjTicks = 0;
    double autotarget = 68000.0;
    private boolean moveDone;
    private boolean linedUp;

    @Override
    public void robotInit() {

        //Setup joystick
        driveJoystick = new Joystick(0);
        opJoystick = new Joystick(1);

        //Setup port drive motors. they get follower'd in the drivetrain class
        portDriveMaster = new TalonFX(Constants.kPortDrivePort1);
        portDriveFollower1 = new TalonFX(Constants.kPortDrivePort2);
        portDriveFollower2 = new TalonFX(Constants.kPortDrivePort3);

        portDriveMaster.setNeutralMode(NeutralMode.Brake);
        portDriveFollower1.setNeutralMode(NeutralMode.Brake);
        portDriveFollower2.setNeutralMode(NeutralMode.Brake);

        //Setup starboard drive motors
        starDriveMaster = new TalonFX(Constants.kStarDrivePort1);
        starDriveFollower1 = new TalonFX(Constants.kStarDrivePort2);
        starDriveFollower2 = new TalonFX(Constants.kStarDrivePort3);

        starDriveMaster.setNeutralMode(NeutralMode.Brake);
        starDriveFollower1.setNeutralMode(NeutralMode.Brake);
        starDriveFollower2.setNeutralMode(NeutralMode.Brake);

        portDriveMaster.configOpenloopRamp(0.1);
        starDriveMaster.configOpenloopRamp(0.1);
        

        //Setup drive train
        driveTrain = new DriveTrain(portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2, driveJoystick);

        //Setup intake motor
        intakeHoppTalon = new TalonSRX(Constants.kIntakeHoppPort);

        //Setup shooter motors
        shooterTalonPort = new TalonSRX(Constants.kPortShooterPort);
        shooterTalonStar = new TalonSRX(Constants.kStarShooterPort);


        //Setup solenoids
        intakeSolenoid = new DoubleSolenoid(Constants.kIntakeSolenoidModulePort, CTREPCM, Constants.kIntakeSolenoidForwardPort, Constants.kIntakeSolenoidReversePort);
        hopperStopper = new DoubleSolenoid(Constants.kFlopperSolenoidModulePort, CTREPCM, Constants.kFlopperSolenoidForwardPort, Constants.kFlopperSolenoidReversePort);
        shooterAngleSolenoid = new DoubleSolenoid(Constants.kShooterAngleSolenoidModulePort, CTREPCM, Constants.kShooterAngleSolenoidForwardPort, Constants.kShooterAngleSolenoidReversePort);
        climberSolenoidPort = new DoubleSolenoid(Constants.kPortClimberModulePort, CTREPCM, Constants.kPortClimberForwardPort, Constants.kPortClimberReversePort);
        //Setup sensors 
        //LOL WE DONT HAVE ANY SENSORS


        //Setup ball transfer thingies
        intakeHopp = new IntakeHopper(intakeSolenoid, intakeHoppTalon);
        shooter = new Shooter(shooterTalonPort, shooterTalonStar, shooterAngleSolenoid, hopperStopper);
        ballProcessor = new BallProcessor(shooter, intakeHopp);

        climber = new Climber(climberSolenoidPort);
        
        portDriveMaster.configFactoryDefault();
        portDriveMaster.setSelectedSensorPosition(0.0);

        CameraServer.startAutomaticCapture();

        LimeLight.getInstance().turnOn();


    }

    DigitalInput testBreak1 = new DigitalInput(0);
    DigitalInput testBreak2 = new DigitalInput(2);
    @Override
    public void robotPeriodic() {
      //  System.out.printf("Port 0: %b ---- Port 2: %b\n", testBreak1.get(), testBreak2.get());
    }


    @Override
    public void autonomousInit() {
        portDriveMaster.setSelectedSensorPosition(0.0);
        moveDone = false;
        linedUp = false;
        LimeLight.getInstance().turnOn();

        driveTrain.setWantedState(DriveState.AUTONOMOUS);
        ballProcessor.setProcessorState(BallProcessState.COLLECTING);
    }

    @Override
    public void autonomousPeriodic() {
        
        double forward = 0.0, turn = 0.0;
        if (!moveDone) {
            System.out.println("cool im in not done");
            if (portDriveMaster.getSelectedSensorPosition() >= autotarget){
                System.out.println("cool");


                
                moveDone = true;
            } else {
                System.out.println("cool i should be moving???????");
                forward = 0.5;
            }
        }
        if (moveDone&&!linedUp) {
            if (LimeLight.getInstance().getTV() < 1) {
                forward = 0;
                turn = 0.4;

            } else {
                driveTrain.setWantedState(DriveState.GOAL_TRACKING);
                ballProcessor.setProcessorState(BallProcessState.LOADED);
                
               // ballProcessor.startShooting();
            }

        }
        // if (linedUp) {
        //     driveTrain.setWantedState(DriveState.AUTONOMOUS);
        //     if (adjTicks <= 50){
        //         adjTicks++;
        //         forward = 0.3;
        //         turn = 0.0;
        //     } else {
        //         forward = 0.0;
        //     }
                
        // }
      //  System.out.println(portDriveMaster.getSelectedSensorPosition());

      System.out.println(forward);
      driveTrain.setAutoDemand(forward, turn);

        shooter.update();
        intakeHopp.update();
        ballProcessor.update();
        driveTrain.update();
    }

    @Override
    public void teleopPeriodic() {

        if (driveJoystick.getRawButton(2)){
            driveTrain.setWantedState(DriveState.GOAL_TRACKING);
        } else {
            driveTrain.setWantedState(DriveState.MANUAL);
        }

        if (driveJoystick.getTriggerPressed()){
            ballProcessor.setProcessorState(BallProcessState.COLLECTING);
        }
        if (driveJoystick.getTriggerReleased()) {
            ballProcessor.setProcessorState(BallProcessState.LOADED);
        }

        if (opJoystick.getRawButtonPressed(9)) {
            climber.climberToggle();
        }

        if (opJoystick.getRawButtonPressed(3)) {
            ballProcessor.startShooting();
        }
        if (opJoystick.getRawButtonReleased(3)) {
            ballProcessor.stopShooting();
        }
        if (opJoystick.getRawButtonPressed(4)){
            ballProcessor.startLowerShooting();
        }
        if (opJoystick.getRawButtonReleased(4)){
            ballProcessor.stopShooting();
        }
        if (opJoystick.getRawButtonPressed(11)) {
            shooter.toggleHood();
        }



        ballProcessor.update();
        shooter.update();
        driveTrain.update();
        intakeHopp.update();
      }

    @Override
    public void testPeriodic() {
        runTest();
    }

    private void runTest(){





    }
}