package frc.robot;

import frc.team1699.Constants;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.team1699.subsystems.BallProcessor;
import frc.team1699.subsystems.DriveTrain;
import frc.team1699.subsystems.IntakeHopper;
import frc.team1699.subsystems.Shooter;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.sensors.LimitSwitch;


public class Robot extends TimedRobot {

    private Joystick driveJoystick, opJoystick;
    public PneumaticsModuleType CTREPCM = PneumaticsModuleType.CTREPCM;
    private DriveTrain driveTrain;
    private IntakeHopper intakeHopp;
    private Shooter shooter;
    private BallProcessor ballProcessor;
    private TalonSRX intakeHoppTalon, shooterTalonPort, shooterTalonStar, hopperTalon;
    private TalonFX portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2;
    private Compressor compressor;
    private DoubleSolenoid intakeSolenoid, hopperStopper, shooterAngleSolenoid, climberSolenoidPort, climberSolenoidStar;
    //private AdaFruitBeamBreak intakeBreak, hopperBreak;
    private LimitSwitch shooterBreak;

    @Override
    public void robotInit() {
        //Setup joystick
        driveJoystick = new Joystick(0);
        opJoystick = new Joystick(1);

        //Setup port drive motors. they get follower'd in the drivetrain class
        portDriveMaster = new TalonFX(Constants.kPortDrivePort1);
        portDriveFollower1 = new TalonFX(Constants.kPortDrivePort2);
        portDriveFollower2 = new TalonFX(Constants.kPortDrivePort3);
        

        //Setup starboard drive motors
        starDriveMaster = new TalonFX(Constants.kStarDrivePort1);
        starDriveFollower1 = new TalonFX(Constants.kStarDrivePort2);
        starDriveFollower2 = new TalonFX(Constants.kStarDrivePort3);

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
        climberSolenoidStar = new DoubleSolenoid(Constants.kStarClimberModulePort, CTREPCM, Constants.kStarClimberForwardPort, Constants.kStarClimberReversePort);

        //Setup sensors 
        //LOL WE DONT HAVE ANY SENSORS


        //Setup ball transfer thingies
        intakeHopp = new IntakeHopper(intakeSolenoid, intakeHoppTalon);
        shooter = new Shooter(shooterTalonPort, shooterTalonStar, shooterAngleSolenoid, hopperStopper);
        ballProcessor = new BallProcessor(shooter, intakeHopp);
    }

    DigitalInput testBreak1 = new DigitalInput(0);
    DigitalInput testBreak2 = new DigitalInput(2);
    @Override
    public void robotPeriodic() {
        System.out.printf("Port 0: %b ---- Port 2: %b\n", testBreak1.get(), testBreak2.get());
    }


    @Override
    public void autonomousInit() {

    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopPeriodic() {

      /*
      
      HEY THIS CODE IS LIKE RANDOM FUNCTIONALITY THAT WE AREN'T GONNA USE BUT I LEAVE IT FOR EXAMPLE

      */
      
      // if(driveJoystick.getTriggerPressed()){
      //       driveTrain.setWantedState(DriveTrain.DriveState.GOAL_TRACKING);
      //   }else if(driveJoystick.getTriggerReleased()){
      //       driveTrain.setWantedState(DriveTrain.DriveState.MANUAL);
      //   }

      //   if(opJoystick.getRawButtonPressed(7)){
      //       ballProcessor.setWantedState(BallProcessor.BallProcessState.COLLECTING);
      //   }else if(opJoystick.getRawButtonPressed(8)){
      //       ballProcessor.setWantedState(BallProcessor.BallProcessState.SHOOTING);
      //   }else if(opJoystick.getRawButtonPressed(9)){
      //       ballProcessor.setWantedState(BallProcessor.BallProcessState.EMPTY);
      //   }else if(opJoystick.getRawButtonPressed(10)){
      //       ballProcessor.setWantedState(BallProcessor.BallProcessState.RETRACTING);
      //   }

      //   if(opJoystick.getRawButtonPressed(2)){
      //       toggleSolenoid(shooterAngleSolenoid);
      //   }

        driveTrain.update();
        intakeHopp.update();
        shooter.update();
        ballProcessor.update();
    }

    @Override
    public void testPeriodic() {
        runTest();
    }

    private void runTest(){

        // Buttons linked to stuff goes here
        //ex:
        // if(driveJoystick.getRawButtonPressed(7)){
        //     toggleSolenoid(intakeSolenoid);
        // }

    }
}