package frc.robot;

import frc.team1699.Constants;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.team1699.subsystems.BallProcessor;
import frc.team1699.subsystems.BallProcessor.BallProcessState;
import frc.team1699.subsystems.AutoBallProcessor;
import frc.team1699.subsystems.AutoBallProcessor.AutoBallProcessState;
import frc.team1699.subsystems.DriveTrain;
import frc.team1699.subsystems.DriveTrain.DriveState;
import frc.team1699.subsystems.IntakeHopper.IntakeStates;
import frc.team1699.subsystems.Shooter.ShooterState;
import frc.team1699.subsystems.IntakeHopper;
import frc.team1699.subsystems.Climber;
import frc.team1699.subsystems.Shooter;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.sensors.LimitSwitch;
import frc.team1699.utils.sensors.LimeLight;
import frc.team1699.utils.LEDController;
import frc.team1699.utils.Utils;
import frc.team1699.utils.LEDController.LEDColors;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;


public class Robot extends TimedRobot {

    private AHRS gyro;
    private Joystick driveJoystick, opJoystick;
    public PneumaticsModuleType CTREPCM = PneumaticsModuleType.CTREPCM;
    private DriveTrain driveTrain;
    private Climber climber;
    private IntakeHopper intakeHopp;
    private Shooter shooter;
    private BallProcessor ballProcessor;
    private AutoBallProcessor autoBallProcessor;
    private TalonSRX intakeHoppTalon, hoodTalonPort, hoodTalonStar, hopperTalon;
    private TalonFX shooterPortFx, shooterStarFx;
    private TalonFX portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2;
    private Compressor compressor;
    private DoubleSolenoid intakeSolenoid, hopperStopper, shooterAngleSolenoid, climberSolenoidPort;
    //private AdaFruitBeamBreak intakeBreak, hopperBreak;
    private LimitSwitch shooterBreak;
    private int adjTicks = 0;
    double autotarget = 68000.0;

    private int llAutoBuffer = 0;

    private boolean moveDone;
    private boolean linedUp;
    public static boolean inAuto;




    //DISABLE AUTO
    private boolean do2BallAuto = false;

    // STARTING HEADING
    public static int startingYaw;
    public static int testYaw = 0;

    // LEDS
    private LEDController tempController;
    public static ColorSensorV3 colorSensor = new ColorSensorV3(I2C.Port.kOnboard);
    public static ColorMatch colorMatcher = new ColorMatch();
    private final Color kBlueTarget = new Color(0.15, 0.45, 0.34);
    private final Color kRedTarget = new Color(0.4, 0.4, 0.2);
    public static ColorMatchResult detectedColor;
    public static final int ledLength = 44;

    @Override
    public void robotInit() {
        tempController = new LEDController(ledLength, 1);
        tempController.start();

        colorMatcher.addColorMatch(kBlueTarget);
        colorMatcher.addColorMatch(kRedTarget);
        colorMatcher.setConfidenceThreshold(.9);

        gyro = new AHRS();
        //Setup joystick
        driveJoystick = new Joystick(0);
        opJoystick = new Joystick(1);

        //Setup port drive motors. they get follower'd in the drivetrain class
        portDriveMaster = new TalonFX(Constants.kPortDrivePort1);
        portDriveFollower1 = new TalonFX(Constants.kPortDrivePort2);
        portDriveFollower2 = new TalonFX(Constants.kPortDrivePort3);
/*
        portDriveMaster.setNeutralMode(NeutralMode.Brake);
        portDriveFollower1.setNeutralMode(NeutralMode.Brake);
        portDriveFollower2.setNeutralMode(NeutralMode.Brake);
*/
        //Setup starboard drive motors
        starDriveMaster = new TalonFX(Constants.kStarDrivePort1);
        starDriveFollower1 = new TalonFX(Constants.kStarDrivePort2);
        starDriveFollower2 = new TalonFX(Constants.kStarDrivePort3);
        /*
        starDriveMaster.setNeutralMode(NeutralMode.Brake);
        starDriveFollower1.setNeutralMode(NeutralMode.Brake);
        starDriveFollower2.setNeutralMode(NeutralMode.Brake);
        */
        setNeutralMode(NeutralMode.Coast);
        portDriveMaster.configOpenloopRamp(0.1);
        starDriveMaster.configOpenloopRamp(0.1);
        
        //Setup drive train
        driveTrain = new DriveTrain(portDriveMaster, portDriveFollower1, portDriveFollower2, starDriveMaster, starDriveFollower1, starDriveFollower2, driveJoystick);

        //Setup intake motor
        intakeHoppTalon = new TalonSRX(Constants.kIntakeHoppPort);

        //Setup shooter motors
        hoodTalonPort = new TalonSRX(Constants.kPortBackHoodMotor);
        hoodTalonStar = new TalonSRX(Constants.kStarBackHoodMotor);

        shooterPortFx = new TalonFX(Constants.kPortShooterPort);
        shooterStarFx = new TalonFX(Constants.kStarShooterPort);

        //Setup solenoids
        intakeSolenoid = new DoubleSolenoid(Constants.kIntakeSolenoidModulePort, CTREPCM, Constants.kIntakeSolenoidForwardPort, Constants.kIntakeSolenoidReversePort);
        hopperStopper = new DoubleSolenoid(Constants.kFlopperSolenoidModulePort, CTREPCM, Constants.kFlopperSolenoidForwardPort, Constants.kFlopperSolenoidReversePort);
        shooterAngleSolenoid = new DoubleSolenoid(Constants.kShooterAngleSolenoidModulePort, CTREPCM, Constants.kShooterAngleSolenoidForwardPort, Constants.kShooterAngleSolenoidReversePort);
        climberSolenoidPort = new DoubleSolenoid(Constants.kPortClimberModulePort, CTREPCM, Constants.kPortClimberForwardPort, Constants.kPortClimberReversePort);
        //Setup sensors 
        //LOL WE DONT HAVE ANY SENSORS


        //Setup ball transfer thingies
        intakeHopp = new IntakeHopper(intakeSolenoid, intakeHoppTalon);
        shooter = new Shooter(hoodTalonPort, hoodTalonStar, shooterAngleSolenoid, hopperStopper, shooterStarFx, shooterPortFx);
        ballProcessor = new BallProcessor(shooter, intakeHopp);
        autoBallProcessor = new AutoBallProcessor(shooter, intakeHopp);

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
      detectedColor = colorMatcher.matchClosestColor(colorSensor.getColor());
      if(detectedColor.color == kBlueTarget && detectedColor.confidence > .9) {
        tempController.solidColor(LEDColors.BLUE);
      } else if(detectedColor.color == kRedTarget && detectedColor.confidence > .9) {
        tempController.solidColor(LEDColors.RED);
      } else {
        tempController.rainbow();
      }
      System.out.println(detectedColor.confidence);
    }

    @Override
    public void autonomousInit() {
        startingYaw = (int)gyro.getYaw();
        System.out.println(startingYaw);
        if (do2BallAuto){
            setNeutralMode(NeutralMode.Brake);
            portDriveMaster.setSelectedSensorPosition(0.0);
            moveDone = false;
            linedUp = false;
            inAuto = true;
            LimeLight.getInstance().turnOff();

            // shooter.toggleSolenoid(shooterAngleSolenoid);

            shooter.deployHopperStopper();

            shooter.hoodSolenoid.set(DoubleSolenoid.Value.kForward); // hood up
            System.out.println("hood up in auto (init one)");
            driveTrain.setWantedState(DriveState.AUTONOMOUS);
            autoBallProcessor.setProcessorState(AutoBallProcessState.COLLECTING);
        } else {
            autoBallProcessor.setProcessorState(AutoBallProcessState.LOADED);
            driveTrain.setWantedState(DriveState.AUTONOMOUS);
        }
    }

    private boolean onRamp = false;

    @Override
    public void autonomousPeriodic() {
        if (do2BallAuto) {
            // shooter.hoodUp(); // you would think this would be easy     
          //  System.out.println("We are in auto");
            double forward = 0.0, turn = 0.0;
            if (!moveDone) {
 //          System.out.println("cool im in not done");
                if (portDriveMaster.getSelectedSensorPosition() >= autotarget){
                //    System.out.println("cool");
                    
                    moveDone = true;
                } else {
                //    System.out.println("cool i should be moving???????");
                    forward = 0.5;
                }
            }
            if (moveDone) {

                if (llAutoBuffer < 35) { //buffer before limelight turns on while we turning for a bit
                    //set how long said bit is by changing the number!!!!!!!!!!!!!!!!!!!!!!!!!
                    LimeLight.getInstance().turnOff();
                    forward = 0;
                    turn = -0.4;
                    llAutoBuffer++;
                } else {
                    LimeLight.getInstance().turnOn();
                
                if (LimeLight.getInstance().getTV() < 1) {
                    forward = 0;
                    turn = -0.4;

                } else {
                    
                    turn = 0.0;

                    driveTrain.setWantedState(DriveState.GOAL_TRACKING);
                    
                    
              //      System.out.println("someting");
                    
                    if (Utils.epsilonEquals(LimeLight.getInstance().getTX(), 0.0, 3.0) && LimeLight.getInstance().getTV() > 0){
                        autoBallProcessor.startShooting();
                      //  System.out.println("oh no u gotta fix the shooter");
                    }// else {
                  //      autoBallProcessor.setProcessorState(AutoBallProcessState.LOADED);
                  //  }
                }

            }
        }
         //   System.out.println("Sensor Position = " + portDriveMaster.getSelectedSensorPosition());

          //  System.out.println("Speed = " + forward);
            driveTrain.setAutoDemand(forward, turn);

            shooter.update();
            intakeHopp.update();
            autoBallProcessor.update();
            driveTrain.update();
        } else {
            portDriveMaster.setNeutralMode(NeutralMode.Brake);
            starDriveMaster.setNeutralMode(NeutralMode.Brake);
            double turnVal = 0.0;
            if (!(Math.abs((int) gyro.getYaw() - Robot.startingYaw) < 3)){
                turnVal = .3;
                if(gyro.getYaw() < 0){
                    turnVal = -turnVal;
                }
            } else {
                turnVal = 0;
            }
            System.out.println("Gyro Pitch: " + gyro.getPitch());
            if (gyro.getPitch() - 4.0 > 12.0){
                onRamp = true;
                driveTrain.setWantedState(DriveState.BALANCING);
                // do autobalance
                System.out.println("i wannaa do autobalance");
            } else if (!onRamp) {
                driveTrain.setAutoDemand(-.45, turnVal);
            }
            driveTrain.update();
        }
    }


    @Override
    public void teleopInit() {
        setNeutralMode(NeutralMode.Brake);
        ballProcessor.stopShooting();
        driveTrain.setWantedState(DriveState.MANUAL);
        inAuto = false;
        shooter.hoodSolenoid.set(DoubleSolenoid.Value.kForward);
        LimeLight.getInstance().turnOn();
        startingYaw = (int)gyro.getYaw();
    }

    @Override
    public void teleopPeriodic() {
        double pitch = gyro.getPitch() - 4;
        if(pitch < 3 && pitch > -3) {
            driveTrain.isBalanced = true;
        } else {
            driveTrain.isBalanced = false;
        }
        SmartDashboard.putBoolean("Is balanced", driveTrain.isBalanced);
        System.out.println("Pitch: " + gyro.getPitch());
        LimeLight.getInstance().turnOn();
        if (LimeLight.getInstance().getTV() > 0){
            System.out.println(LimeLight.getInstance().getDistanceFromTarget() + " inches away from the target");
        }
        //AUTO AIM
        if (driveJoystick.getRawButton(2)){
            LimeLight.getInstance().turnOn();
            driveTrain.setWantedState(DriveState.GOAL_TRACKING);
        }
        if (driveJoystick.getRawButtonReleased(2)){
       //     LimeLight.getInstance().turnOff();
            driveTrain.setWantedState(DriveState.MANUAL);
        }

        //ENABLE LIMELIGHT
        if (opJoystick.getRawButtonPressed(1)){
            LimeLight.getInstance().turnOn();
        }
        if (opJoystick.getRawButtonReleased(1)){
         //   LimeLight.getInstance().turnOff();
        }

        //INTAKE
        if (driveJoystick.getTriggerPressed()){
            ballProcessor.setProcessorState(BallProcessState.COLLECTING);
        }
        if (driveJoystick.getTriggerReleased()) {
            ballProcessor.setProcessorState(BallProcessState.LOADED);
        }

        //CLIMB
        // if (opJoystick.getRawButtonPressed(9)) {
        //     climber.climberToggle();
        // }

        //HIGH GOAL SHOOT
        if (opJoystick.getRawButtonPressed(3) || opJoystick.getRawButtonPressed(6)) {
            ballProcessor.startShooting();
        }
        if (opJoystick.getRawButtonReleased(3) || opJoystick.getRawButtonReleased(6)) {
            ballProcessor.stopShooting();
        }
        
        //LOW GOAL SHOOT
        if (opJoystick.getRawButtonPressed(4)){
            ballProcessor.startLowerShooting();
        }
        if (opJoystick.getRawButtonReleased(4)){
            ballProcessor.stopShooting();
        }

        //MANUAL HOOD
        if (opJoystick.getRawButtonPressed(5)) {
            shooter.toggleHood();
        }

        //BACKDRIVE
        if (driveJoystick.getRawButtonPressed(3)){
            ballProcessor.setProcessorState(BallProcessState.PURGING);
        }
        if (driveJoystick.getRawButtonReleased(3)) {
            ballProcessor.setProcessorState(BallProcessState.LOADED);
        }

        // BALANCING
        // if (driveJoystick.getRawButton(11)){
        //     driveTrain.setWantedState(DriveState.BALANCING);
        // }

        // if (driveJoystick.getRawButtonReleased(11)){
        //     driveTrain.setWantedState(DriveState.MANUAL);
        // }

        // POSITIONING FROM APRILTAG
        // if (driveJoystick.getRawButton(12)){
        //     driveTrain.setWantedState(DriveState.POSITIONING);
        // }
        
        // if (driveJoystick.getRawButtonReleased(12)){
        //     driveTrain.setWantedState(DriveState.MANUAL);
        // }

        //makes sure the hopper that can't be stopper'd gets stopper'd
        if (!opJoystick.getRawButton(4) && !opJoystick.getRawButton(3) && !opJoystick.getRawButton(6)){
            hopperStopper.set(DoubleSolenoid.Value.kForward);
        }
        
        // toggles the pipeline between apriltags and limelight
        if (driveJoystick.getRawButtonPressed(6)){
            LimeLight.getInstance().togglePipeline();
        }

        // spinny :)
        if (driveJoystick.getRawButton(7)){
            driveTrain.setWantedState(DriveState.SPINNING);
        }

        if (driveJoystick.getRawButtonReleased(7)){
            driveTrain.setWantedState(DriveState.MANUAL);
        }

        if (driveJoystick.getRawButtonPressed(8)){
            startingYaw = (int)gyro.getYaw();
        }

        if (Constants.theRobotIsJustADrivetrainAndNothingMore){
            ballProcessor.setProcessorState(BallProcessState.IDLE);
        }
        intakeHopp.update();
        ballProcessor.update();
        shooter.update();
        driveTrain.update();
        
      }

    @Override
    public void testPeriodic() {climber.climberDown();
        shooter.setWantedState(ShooterState.STOPPED);
        ballProcessor.idleShooting();
        setNeutralMode(NeutralMode.Coast);
        shooter.update();
        ballProcessor.update();
        LimeLight.getInstance().turnOff();

        System.out.println("Gyro \"pitch\": " + ((int) (gyro.getPitch())));
        System.out.println("Gyro \"yaw\": " + ((int) (gyro.getYaw())));
        System.out.println("Gyro \"roll\": " + ((int) gyro.getRoll()));
    }

    public void setNeutralMode(NeutralMode neutralMode){
        portDriveMaster.setNeutralMode(neutralMode);
        portDriveFollower1.setNeutralMode(neutralMode);
        portDriveFollower2.setNeutralMode(neutralMode);

        starDriveMaster.setNeutralMode(neutralMode);
        starDriveFollower1.setNeutralMode(neutralMode);
        starDriveFollower2.setNeutralMode(neutralMode);
    }
}