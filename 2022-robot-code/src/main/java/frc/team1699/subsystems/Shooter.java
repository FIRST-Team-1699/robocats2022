package frc.team1699.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.Gains;
import frc.team1699.utils.sim.PhysicsSim;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

import frc.team1699.utils.sensors.LimeLight;

//public class Shooter implements Subsystem{ 
public class Shooter {

    private double kMain2TopMult = 3; //3 is good for 4 feet

    private double kMainTestSpd = 8000;

    public int hoodTransition = 0;

    //error variables
    int kErrThreshold = 200; // IF THIS IS LESS THAN 100 YOU MIGHT POP BALLS

    public static final int kPIDLoopIDX = 0; //just leave this at 0 its for if u want more than 1 loop
    public static final int kTimeoutMs = 100;

    public final Gains kVelocityPIDGains = new Gains(0.16, 0.0004, 0.4096, 0.03, 1.0, 300); // JAKOB SAYS: to make it more good, double d OR halve p
    
    public final Gains kMainPIDGains = new Gains(0.16, 0.0004, 0.4096, 0.03, 1.0, 300); // for falcons

    private double targetVelocityTop = 0.0; //it will tell the motor to spin this fast, ik its so cool

    private double targetVelocityMain = 0.0;

    private final double idle_UnitsPer100ms = 9000.0; //target velocity when its "running"


    private final double shooting_UnitsPer100ms = 20000.0; //the target velocity while shooting


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

                hoodTransition++; //this is set to 0 in the start shooting method in ballprocessor
                
                if(LimeLight.getInstance().getTV() > 0){
                    if (LimeLight.getInstance().getTY() < 21.0){
                        targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                        targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                    } else {
                        targetVelocityMain = 3676.0;
                        targetVelocityTop = 3676.0 * 3.0;
                    }
                }

                //wait until the thingy is up to speed, and then open the hopper    
                if (shooterPortFX.getClosedLoopError() < +kErrThreshold &&  //if the speed is correct
                    shooterPortFX.getClosedLoopError() > -kErrThreshold &&
                    hoodTransition >= 9) { //will always spin up for at least 9 ms
                    
                    if (atSpeedTicks >= 15) { //if its been at speed for a while
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

                // if(LimeLight.getInstance().getTV() > 0){
                    targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                    targetVelocityMain = calculateMainShooterSpeed(LimeLight.getInstance().getTY());
                // }
                // else{
                //     targetVelocityTop = idle_UnitsPer100ms;
                //     targetVelocityMain = idle_UnitsPer100ms;
                // }
                deployHopperStopper();
                currentState = ShooterState.RUNNING;
                shooterAtSpeed = false;
                atSpeedTicks = 0;
            break;
            case SHOOT:          

            if (LimeLight.getInstance().getTV()<1){
                // if(hoodSolenoid.get() == DoubleSolenoid.Value.kForward){
                //     targetVelocityMain = 17000;
                //     targetVelocityTop = 17000;
                // }else{
                //     targetVelocityMain = shooting_UnitsPer100ms;
                //     targetVelocityTop = shooting_UnitsPer100ms;
                // }

            } else {
                targetVelocityTop = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                targetVelocityMain = calculateTopShooterSpeed(LimeLight.getInstance().getTY());
                if () {

                }
                if (LimeLight.getInstance().getTY() >= -6.0) {
                    System.out.println("im sad");
                    hoodSolenoid.set(DoubleSolenoid.Value.kReverse); //this acts as a boolean for speed calculation
                } else {
                    System.out.println("???");
                    hoodSolenoid.set(DoubleSolenoid.Value.kForward); //hood up
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
        if(LimeLight.getInstance().getTV() < 1.0 && hoodSolenoid.get() == DoubleSolenoid.Value.kReverse) {
            return 0.0;
        }
        return calculateMainShooterSpeed(llY) * kMain2TopMult;

        //2k low goal
       // return ((llY * -163) + 10000);

    }
    //llY means limelight y
    public double calculateMainShooterSpeed(double llY){

        /* the hood position will be determined by the limelight
        // value somehwere else in the code. this will effectivley
        // switch the shooting mode based on the y value, the
        // same value used in this method.
        */

        if(hoodSolenoid.get() == DoubleSolenoid.Value.kForward){
            return 0.694 * (llY * llY) - 17.5 * llY + 4489;

            //linear??? who knows
            //return -41.5 * llY + 4303;
        } else if(LimeLight.getInstance().getTV() < 1.0 && hoodSolenoid.get() == DoubleSolenoid.Value.kReverse) {
            return kMainTestSpd;

            //return -29.5 * llY + 5118;
        } else {
            return -2.33 * (llY * llY) -3.82 * llY + 5182;
        }
        

        //5k on main 0 on upper is low goal PERFECTION
     //   return (llY * -163) + 5000;
    }
}