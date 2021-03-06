package frc.team1699.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.wpilibj.Joystick;

import frc.robot.Robot;
import frc.team1699.Constants;
import frc.team1699.utils.Gains;
import frc.team1699.utils.sim.PhysicsSim;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

import frc.team1699.utils.sensors.LimeLight;

//public class Shooter implements Subsystem{ 
public class Shooter {

    private Joystick opJoystick;

    private double kMain2TopMult = 3; //3 is good for 4 feet

    private double kMainTestSpd = 5700;

    public int hoodTransition = 0;

    //error variables
    int kErrThreshold = 200  ; // IF THIS IS LESS THAN 100 YOU MIGHT POP BALLS

    public static final int kPIDLoopIDX = 0; //just leave this at 0 its for if u want more than 1 loop
    public static final int kTimeoutMs = 100;

    public final Gains kVelocityPIDGains = new Gains(0.16, 0.0004, 0.4096, 0.03, 1.0, 300); // for top | JAKOB SAYS: to make it more good, double d OR halve p
    public final Gains kMainPIDGains = new Gains(0.16, 0.0004, 0.4096, 0.03, 1.0, 300); // for falcons

    //public final Gains kMainPIDGains = new Gains(0.1, 0.0000, 10.0, 0.06, 1.0, 300); // for falcons

    private double targetVelocityTop = 0.0; //it will tell the motor to spin this fast, ik its so cool

    private double targetVelocityMain = 0.0;

    private final double idle_UnitsPer100ms = 5000.0; //target velocity when its "running"

    private final double shooting_UnitsPer100ms = 20000.0; //the target velocity while shooting


    public boolean isCloseUpperShooting = false;
    private final double kCloseUpperSpeed = 8000;
    private final double kCloseUpperMain2TopMulti = 0;

    /**
        Shawty had them apple bottom jeans (jeans)
        Boots with the fur (with the fur)
        The whole club was lookin' at her
        She hit the floor (she hit the floor)
        Next thing you know
        Shawty got low, low, low, low, low, low, low, low
        @author Flo Rida
    */
    public boolean isLowerShooting = false;
    private final double kLowGoalSpeed = 5000.0;
    private final double kLowGoalMain2TopMulti = 0;


    public boolean shooterAtSpeed = false;
    private int atSpeedTicks = 0;

    private ShooterState currentState, wantedState = ShooterState.UNINITIALIZED;
    private HoodPosition currentPosition;
    public final DoubleSolenoid hoodSolenoid;


    //hoppaStoppa is the flipper in the hopper on a double action solenoid that stops the balls from going into the shooter
    //when it is deployed, the balls can't move through to the shooter
    private final DoubleSolenoid hoppaStoppa;
    public static boolean stopperDeployed = false;

    private final TalonSRX topMotorPort;
    private final TalonSRX topMotorStar;

    private final TalonFX shooterPortFX;
    private final TalonFX shooterStarFX;

    public Shooter(final TalonSRX topMotorPort, final TalonSRX topMotorStar, final DoubleSolenoid hoodSolenoid, final DoubleSolenoid hoppaStoppa, final TalonFX shooterStarFX, final TalonFX shooterPortFX) {
        this.topMotorPort = topMotorPort;
        this.topMotorStar = topMotorStar;

        opJoystick = new Joystick(Constants.kOperatorJoystickPort);

        this.shooterPortFX = shooterPortFX;
        this.shooterStarFX = shooterStarFX;

        this.hoodSolenoid = hoodSolenoid;
        this.hoppaStoppa = hoppaStoppa; //this is the hopper stopper, the stopper in the hopper. its stops the balls. NO I WILL NOT CHANGE ITS NAME.
        this.currentPosition = HoodPosition.DOWN;

        currentState = ShooterState.UNINITIALIZED;

        topMotorPort.configFactoryDefault();
        topMotorStar.configFactoryDefault();

        shooterPortFX.configFactoryDefault();
        shooterStarFX.configFactoryDefault();


        topMotorStar.follow(topMotorPort);
        topMotorStar.setInverted(InvertType.OpposeMaster); //this needs to be reversed from the other motor because of theyre connected


        shooterStarFX.follow(shooterPortFX);
        

        topMotorPort.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, kPIDLoopIDX, kTimeoutMs);
        shooterPortFX.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, kPIDLoopIDX, kTimeoutMs);


        topMotorPort.setSensorPhase(true);
        shooterPortFX.setSensorPhase(true);

        shooterPortFX.setInverted(true);
        shooterStarFX.setInverted(InvertType.OpposeMaster);

        topMotorPort.configNominalOutputForward(0, kTimeoutMs);
        topMotorPort.configNominalOutputReverse(0, kTimeoutMs);
        topMotorPort.configPeakOutputForward(1, kTimeoutMs);
        topMotorPort.configPeakOutputReverse(-1, kTimeoutMs);

        shooterPortFX.configNominalOutputForward(0, kTimeoutMs);
        shooterPortFX.configNominalOutputReverse(0, kTimeoutMs);
        shooterPortFX.configPeakOutputForward(1, kTimeoutMs);
        shooterPortFX.configPeakOutputReverse(-1, kTimeoutMs);

        topMotorPort.config_kF(kPIDLoopIDX, kVelocityPIDGains.kF, kTimeoutMs);
        topMotorPort.config_kP(kPIDLoopIDX, kVelocityPIDGains.kP, kTimeoutMs);
        topMotorPort.config_kI(kPIDLoopIDX, kVelocityPIDGains.kI, kTimeoutMs);
        topMotorPort.config_kD(kPIDLoopIDX, kVelocityPIDGains.kD, kTimeoutMs);

        shooterPortFX.config_kF(kPIDLoopIDX, kMainPIDGains.kF, kTimeoutMs);
        shooterPortFX.config_kP(kPIDLoopIDX, kMainPIDGains.kP, kTimeoutMs);
        shooterPortFX.config_kI(kPIDLoopIDX, kMainPIDGains.kI, kTimeoutMs);
        shooterPortFX.config_kD(kPIDLoopIDX, kMainPIDGains.kD, kTimeoutMs);

        LimeLight.getInstance().turnOn();
    }

    // public void simulationInit() {
    //     PhysicsSim.getInstance().addTalonSRX(shooterMotorPort, 1.5, 7200, true);
    // }

    public void simulationPeriodic() {
        PhysicsSim.getInstance().run();
    }

    public void update() {
    //    System.out.println("Main speed: " + shooterPortFX.getSelectedSensorVelocity() + "\nTop wheel speed: " + topMotorPort.getSelectedSensorVelocity());
       // System.out.println("Main error: "+ shooterPortFX.getClosedLoopError());
        switch (currentState) {
            case UNINITIALIZED:

                deployHopperStopper();
                setWantedState(ShooterState.RUNNING);
            break;

            case RUNNING:
            
            break;
            case SHOOT:

            if (opJoystick.getRawButton(6)){
                isCloseUpperShooting = true;
                hoodSolenoid.set(DoubleSolenoid.Value.kReverse);
            } else {isCloseUpperShooting = false;}

                hoodTransition++; //this is set to 0 in the start shooting method in ballprocessor
                if (Robot.inAuto) {
                    targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                }
                if(LimeLight.getInstance().getTV() > 0 || isLowerShooting || isCloseUpperShooting){
                    if (LimeLight.getInstance().getTY() < 21.0 || isLowerShooting || isCloseUpperShooting){
                        targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                        targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                    } else {
                    //    System.out.println("else statement speed thing");
                        // targetVelocityMain = 3676.0;
                        // targetVelocityTop = 3676.0 * 3.0;
                        targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                        targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                    }
                }

                //wait until the thingy is up to speed, and then open the hopper    
                if (shooterPortFX.getClosedLoopError() < +kErrThreshold &&  //if the speed is correct
                    shooterPortFX.getClosedLoopError() > -kErrThreshold) { //will always spin up for at least 9 cycles
                    
                    if ((!Robot.inAuto && atSpeedTicks >= 15) || (atSpeedTicks >= 50)) { //if its been at speed for a while (longer in auto because of taking time to aim + me being terrible at programming actual solutions.)
                        retractHopperStopper();
                        shooterAtSpeed = true; //sends a signal to start feeding into the shooter
                        //this will make the motors slow down, causing them to go back to the speeding up phase
                    }
                    atSpeedTicks ++;
                
                } else { //its not at speed, we are waiting to spin up.
                    deployHopperStopper();
                    shooterAtSpeed = false;
                    atSpeedTicks = 0;
                }
            break;

            case STOPPED:

            break;

            default:
                currentState = ShooterState.UNINITIALIZED;
            break;
        }

        // System.out.println("top motor speed: " + (topMotorPort.getSelectedSensorVelocity()*600/2048) + "\nmain motor speed: " + shooterPortFX.getSelectedSensorVelocity()*600/4096);
     //   System.out.println("top motor speed: " + (topMotorPort.getSelectedSensorVelocity()*600/2048) + "\nmain motor speed: " + shooterPortFX.getSelectedSensorVelocity()*600/4096);
    //System.out.println("Target: " + calculateTopShooterSpeed(LimeLight.getInstance().getTY() * (600/2048)) + " - Actual: " + targetVelocityTop);
        topMotorPort.set(TalonSRXControlMode.Velocity, targetVelocityTop);
        shooterPortFX.set(TalonFXControlMode.Velocity, targetVelocityMain);
      //  System.out.println("Target: " + targetVelocityTop + " Error: " + shooterMotorPort.getClosedLoopError(kPIDLoopIDX) + " Output: " + shooterMotorPort.getMotorOutputPercent());
    }

    public void setWantedState(final ShooterState wantedState) {
        this.wantedState = wantedState;
        handleStateTransition(wantedState);
    }

    private void handleStateTransition(final ShooterState wantedState){
        switch(wantedState){
            case UNINITIALIZED:
                break;
            case STOPPED:
                targetVelocityTop = 0.0;
                targetVelocityMain = 0.0;
                currentState = ShooterState.STOPPED;
                shooterAtSpeed = false;
                break;
            case RUNNING:

                if(LimeLight.getInstance().getTV() > 0){
                    targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                    targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                }
                else {
                    targetVelocityTop = idle_UnitsPer100ms;
                    targetVelocityMain = idle_UnitsPer100ms;
                }
                deployHopperStopper();
                currentState = ShooterState.RUNNING;
                shooterAtSpeed = false;
                atSpeedTicks = 0;
            break;
            case SHOOT: 
            
            if (opJoystick.getRawButton(6)){
                isCloseUpperShooting = true;
                hoodSolenoid.set(DoubleSolenoid.Value.kReverse);
                
            } else {isCloseUpperShooting = false;}

            if (LimeLight.getInstance().getTV()<1){ //if no target is seen

                if (isLowerShooting){
                    hoodSolenoid.set(DoubleSolenoid.Value.kReverse);
                } else { //this is if you are close and want to upper shoot
                    isCloseUpperShooting = true;
                    hoodSolenoid.set(DoubleSolenoid.Value.kReverse);
                   // System.out.println("close you pp");
                }

            } else { //if yes target is seen
                targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                targetVelocityMain = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                if (Robot.inAuto) {
                    hoodSolenoid.set(DoubleSolenoid.Value.kForward); // hood up
                    System.out.println(targetVelocityMain);
                } else{ 
                    if (LimeLight.getInstance().getTY() >= -8.0 || isLowerShooting) { //close
                    //    System.out.println("im sad");
                        hoodSolenoid.set(DoubleSolenoid.Value.kReverse); //this acts as a boolean for speed calculation
                    } else { //far
                        //System.out.println("???");
                        hoodSolenoid.set(DoubleSolenoid.Value.kForward); //hood up
                    }
                }
            }
                currentState = ShooterState.SHOOT;
            break;
        }
    }

    public ShooterState getCurrentState() {
        return currentState;
    }

    //make the hood do stuff
    public void toggleHood() {
        toggleSolenoid(hoodSolenoid);
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
        if(hoodSolenoid.get() == DoubleSolenoid.Value.kForward){
            toggleSolenoid(hoodSolenoid);
        }
        currentPosition = HoodPosition.UP;
    }
    public void hoodDown() {
        if(hoodSolenoid.get() == DoubleSolenoid.Value.kReverse){
            toggleSolenoid(hoodSolenoid);
        }
        currentPosition = HoodPosition.DOWN;
    }

    //methods for the hopper stopper! they public so they can be used in the ball processor
    public void toggleHopperStopper() {

        toggleSolenoid(hoppaStoppa);
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

    public void setShooterGoal(double targ) {
        targetVelocityTop = targ;
    }


    enum HoodPosition {
        UP,
        DOWN
    }

    public enum ShooterState {
        UNINITIALIZED,
        RUNNING,
        SHOOT,
        STOPPED
    }
    public void toggleSolenoid(final DoubleSolenoid solenoid){
        if(solenoid.get() == DoubleSolenoid.Value.kForward){
            solenoid.set(DoubleSolenoid.Value.kReverse);
        }else{
            solenoid.set(DoubleSolenoid.Value.kForward);
        }
    }

    //this takes the limelight y value to see how fast it shoots
    //we hope it works because if not we have to copy more 254 code
    public double calculateTopShooterSpeed(double llY){

     //   return kMainTestSpd * kMain2TopMult;

        if (isLowerShooting){ //close low goal shooting at a constant speed
          //  System.out.println("trying to shoot");
            return kLowGoalSpeed * kLowGoalMain2TopMulti;
        } else if (isCloseUpperShooting){
            return kCloseUpperSpeed * kCloseUpperMain2TopMulti;
        }

        if(LimeLight.getInstance().getTV() < 1.0 && hoodSolenoid.get() == DoubleSolenoid.Value.kReverse) {
            return 0.0;
        }

        return calculateMainShooterSpeed(llY) * kMain2TopMult;

        //2k low goal
     //  return ((llY * -163) + 10000);

    }
    //llY means limelight y
    public double calculateMainShooterSpeed(double llY){

        //TEST HAHAHAHAHAHA I AM IN PAIN
       // return kMainTestSpd;

        /* the hood position will be determined by the limelight
        // value somehwere else in the code. this will effectivley
        // switch the shooting mode based on the y value, the
        // same value used in this method.
        */

        //lower hub shooting is a constant speed
        if (isLowerShooting){
            return kLowGoalSpeed;
        } else if (isCloseUpperShooting){
            return kCloseUpperSpeed;
        }

        if(hoodSolenoid.get() == DoubleSolenoid.Value.kForward){

      //      System.out.println("fwdshoot");

            return (llY*llY) * 3.31 + (47.8*llY) + 4947;
            //4540 + -6.45x + 1.19x^2
           // return 1.19 * (llY * llY) - 6.45 * llY + 4540;

      //      return 0.694 * (llY * llY) - 17.5 * llY + 4489; bacon (bad)

            //linear??? who knows
            //return -41.5 * llY + 4303;
        } else {

       //     System.out.println("backshoot");

            return 3.36 * (llY * llY) - (105 * llY) + 5392;
        }
        

       // 5k on main 0 on upper is low goal PERFECTION
      // return (llY * -163) + 5000;
    }
}