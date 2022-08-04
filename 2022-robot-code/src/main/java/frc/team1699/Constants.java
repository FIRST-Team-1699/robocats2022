package frc.team1699;

public class Constants {

    //cool things
    /**i make the robo slo*/
    public static final double coefficientOfSpeedThatGetsMultipliedToMakeTheRobotSlower = 1;

    public static final boolean theRobotIsJustADrivetrainAndNothingMore = true;

    //Wiimote Things
    public static final boolean usingWiimote = false;
    public static final int kWiimotePort = 0;
    //in order to use the wiimote, you must have the companion program running. it does most of the heavy lifting.

    //joysticks
    public static final int kDriveJoystickPort = 0;
    public static final int kOperatorJoystickPort = 1;
    //Motor Constants

    //Port Drive Motor Ports (thats the left side)
    public static final int kPortDrivePort1 = 32;
    public static final int kPortDrivePort2 = 35;
    public static final int kPortDrivePort3 = 34;

    //Starboard Drive Motor Ports (thats the right side)
    public static final int kStarDrivePort1 = 33;
    public static final int kStarDrivePort2 = 36;
    public static final int kStarDrivePort3 = 31;

    //Intake + Hopper Motor Port
    public static final int kIntakeHoppPort = 15;

    //the new wheel thingy motors that are actually the old wheel thingy motors.
    public static final int kPortShooterPort = 37;
    public static final int kStarShooterPort = 38;

    //main shooter motobs
    public static final int kPortBackHoodMotor = 16;
    public static final int kStarBackHoodMotor = 12;

    //Solenoid Ports
    
    //Intake Solenoid
    public static final int kIntakeSolenoidModulePort = 1;
    public static final int kIntakeSolenoidForwardPort = 3;
    public static final int kIntakeSolenoidReversePort = 2;
    
    //Flopper Solenoid
    public static final int kFlopperSolenoidModulePort = 1;
    public static final int kFlopperSolenoidForwardPort = 1;
    public static final int kFlopperSolenoidReversePort = 0;

    //Shooter Angle Solenoid
    public static final int kShooterAngleSolenoidModulePort = 1;
    public static final int kShooterAngleSolenoidForwardPort = 6;
    public static final int kShooterAngleSolenoidReversePort = 7;

    //Climber Solenoid - 1 solenoid 2 pistons
    public static final int kPortClimberModulePort = 1;
    public static final int kPortClimberForwardPort = 5;
    public static final int kPortClimberReversePort = 4;


}