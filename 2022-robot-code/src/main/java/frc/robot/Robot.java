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
import frc.team1699.utils.controllers.SpeedControllerGroup;
import frc.team1699.utils.controllers.falcon.BetterFalcon;
import frc.team1699.utils.controllers.talon.BetterTalon;
//import frc.team1699.utils.sensors.AdaFruitBeamBreak;
import frc.team1699.utils.sensors.LimitSwitch;
import frc.team1699.utils.sensors.TalonEncoder;

import java.io.IOException;
import java.nio.file.Path;

public class Robot extends TimedRobot {

    private Joystick driveJoystick, opJoystick;
    public static final PneumaticsModuleType CTREPCM;
    private DriveTrain driveTrain;
    private IntakeHopper intakeHopp;
    private Shooter shooter;
    private BallProcessor ballProcessor;
    private BetterTalon intakeHoppTalon, shooterTalonPort, shooterTalonStar, hopperTalon, talon1, talon4;
    private BetterFalcon portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2;
    private SpeedControllerGroup portDriveGroup, starDriveGroup;
    private Compressor compressor;
    private DoubleSolenoid intakeSolenoid, hopperStopper, shooterAngleSolenoid, climberSolenoidPort, climberSolenoidStar;
    //private AdaFruitBeamBreak intakeBreak, hopperBreak;
    private LimitSwitch shooterBreak;
    private TalonEncoder portShooterEncoder, starShooterEncoder;

    @Override
    public void robotInit() {
        //Setup joystick
        driveJoystick = new Joystick(0);
        opJoystick = new Joystick(1);

        //TODO Fix ports
        //Setup port drive motors
        portDriveMaster = new BetterFalcon(Constants.kPortDrivePort1);
        portDriveFollower1 = new BetterFalcon(Constants.kPortDrivePort2);
        portDriveFollower2 = new BetterFalcon(Constants.kPortDrivePort3);
        portDriveGroup = new SpeedControllerGroup(portDriveMaster, portDriveFollower1, portDriveFollower2);

        //Setup starboard drive motors
        starDriveMaster = new BetterFalcon(Constants.kStarDrivePort1);
        starDriveFollower1 = new BetterFalcon(Constants.kStarDrivePort2);
        starDriveFollower2 = new BetterFalcon(Constants.kStarDrivePort3);
        starDriveGroup = new SpeedControllerGroup(starDriveMaster, starDriveFollower1, starDriveFollower2);

        //Setup drive train
        driveTrain = new DriveTrain(portDriveGroup, starDriveGroup, driveJoystick);

        //Setup intake motor
        intakeHoppTalon = new BetterTalon(Constants.kIntakeHoppPort);

        //Setup shooter motors
        shooterTalonPort = new BetterTalon(16);
        shooterTalonStar = new BetterTalon(12);


        //Setup solenoids
        intakeSolenoid = new DoubleSolenoid(Constants.kIntakeSolenoidModulePort, CTREPCM, Constants.kIntakeSolenoidForwardPort, Constants.kIntakeSolenoidReversePort);
        hopperStopper = new DoubleSolenoid(Constants.kFlopperSolenoidModulePort, CTREPCM, Constants.kFlopperSolenoidForwardPort, Constants.kFlopperSolenoidReversePort);
        shooterAngleSolenoid = new DoubleSolenoid(Constants.kShooterAngleSolenoidModulePort, CTREPCM, Constants.kShooterAngleSolenoidForwardPort, Constants.kShooterAngleSolenoidReversePort);
        climberSolenoidPort = new DoubleSolenoid(Constants.kPortClimberModulePort, CTREPCM, Constants.kPortClimberForwardPort, Constants.kPortClimberReversePort);
        climberSolenoidStar = new DoubleSolenoid(Constants.kStarClimberModulePort, CTREPCM, Constants.kStarClimberForwardPort, Constants.kStarClimberReversePort);

        //Setup sensors
//        intakeBreak = new AdaFruitBeamBreak(0);
//        hopperBreak = new AdaFruitBeamBreak(2);
       // intakeBreak = null;
      //  hopperBreak = null;
     //   shooterBreak = new LimitSwitch(1);

        //Setup ball transfer
        SpeedControllerGroup shooterGroup = new SpeedControllerGroup(shooterTalonPort, shooterTalonStar);
        portShooterEncoder = new TalonEncoder(shooterTalonPort);
        intakeHopp = new IntakeHopper(intakeSolenoid, intakeHoppTalon);
        shooter = new Shooter(shooterGroup, portShooterEncoder, shooterBreak);
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
        if(driveJoystick.getTriggerPressed()){
            driveTrain.setWantedState(DriveTrain.DriveState.GOAL_TRACKING);
        }else if(driveJoystick.getTriggerReleased()){
            driveTrain.setWantedState(DriveTrain.DriveState.MANUAL);
        }

        if(opJoystick.getRawButtonPressed(7)){
            ballProcessor.setWantedState(BallProcessor.BallProcessState.COLLECTING);
        }else if(opJoystick.getRawButtonPressed(8)){
            ballProcessor.setWantedState(BallProcessor.BallProcessState.SHOOTING);
        }else if(opJoystick.getRawButtonPressed(9)){
            ballProcessor.setWantedState(BallProcessor.BallProcessState.EMPTY);
        }else if(opJoystick.getRawButtonPressed(10)){
            ballProcessor.setWantedState(BallProcessor.BallProcessState.RETRACTING);
        }

        if(opJoystick.getRawButtonPressed(2)){
            toggleSolenoid(shooterAngleSolenoid);
        }

        driveTrain.update();
        intakeHopp.update();
        shooter.update(portShooterEncoder.get());
        ballProcessor.update();
    }

    @Override
    public void testPeriodic() {
        runTest();
    }

    private void runTest(){
        if(driveJoystick.getTrigger()){
            intakeHoppTalon.set(-0.5);
        }else{
            intakeHoppTalon.set(0.0);
        }

        if(driveJoystick.getRawButton(2)){
            shooterTalonPort.set(0.65);
        }else{
            shooterTalonPort.set(0.0);
        }

        if(driveJoystick.getRawButton(3)){
            talon1.set(0.5);
        }else{
            talon1.set(0.0);
        }

        if(driveJoystick.getRawButton(4)){
            shooterTalonStar.set(0.55);
        }else{
            shooterTalonStar.set(0.0);
        }

        if(driveJoystick.getRawButton(5)){
            hopperTalon.set(-0.5);
        }else{
            hopperTalon.set(0.0);
        }

        if(driveJoystick.getRawButton(6)){
            talon4.set(0.5);
        }else{
            talon4.set(0.0);
        }
        
        // Buttons linked to stuff goes here
        //ex:
        // if(driveJoystick.getRawButtonPressed(7)){
        //     toggleSolenoid(intakeSolenoid);
        // }

    }

    private void toggleSolenoid(final DoubleSolenoid solenoid){
        if(solenoid.get() == DoubleSolenoid.Value.kForward){
            solenoid.set(DoubleSolenoid.Value.kReverse);
        }else{
            solenoid.set(DoubleSolenoid.Value.kForward);
        }
    }
}