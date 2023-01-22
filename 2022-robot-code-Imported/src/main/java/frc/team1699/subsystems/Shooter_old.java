// package frc.team1699.subsystems;

// import edu.wpi.first.wpilibj.DoubleSolenoid;
// //import frc.team1699.utils.sensors.BeamBreak;
// import com.ctre.phoenix.motorcontrol.can.TalonSRX;
// import com.ctre.phoenix.motorcontrol.InvertType;
// import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
// import frc.team1699.utils.Utils;

// //TODO Fix
// //public class Shooter implements Subsystem{ 
// public class Shooter {

//     //TODO numbers
//     //Control loop constants
//     static final double kP = 0.2;
//     static final double kI = 0;
//     static final double kF = 0;
//     static final double kD = 0.2;

//     //error variables
//     int kErrThreshold = 30; // how many sensor units until its close-enough

//     static final int kPIDLoopIDX = 0; //just leave this at 0 its for if u want more than 1 loop
    
//     private double targetVelocity_UnitsPer100ms = 0.0;

//     private double idle_UnitsPer100ms = 500.0; //target velocity when its "running"
//     private double shooting_UnitsPer100ms = 2000.0; //the target velocity while shooting

//     public boolean shooterAtSpeed = false;
//     private int atSpeedTicks = 0;

//     private ShooterState currentState = ShooterState.UNINITIALIZED, wantedState;
//     private HoodPosition currentPosition;
//     private final DoubleSolenoid hoodSolenoid;

//     private final int kTimeoutMs = 30;
    
//     //hoppaStoppa is the flipper in the hopper on a double action solenoid that stops the balls from going into the shooter
//     //when it is deployed, the balls can't move through to the shooter
//     private final DoubleSolenoid hoppaStoppa;
//     public static boolean stopperDeployed = false;

//     private TalonSRX shooterMotorPort;
//     private TalonSRX shooterMotorStar;
//     private TalonSRXControlMode TalonSRXControlMode;

//     public Shooter(final TalonSRX portMotor, final TalonSRX starMotor, final DoubleSolenoid hoodSolenoid, final DoubleSolenoid hoppaStoppa) {
//         this.shooterMotorPort = starMotor;
//         this.shooterMotorStar = portMotor;
//         this.hoodSolenoid = hoodSolenoid;
//         this.hoppaStoppa = hoppaStoppa; //this is the hopper stopper, the stopper in the hopper. its stops the balls. NO I WILL NOT CHANGE ITS NAME.
//         this.currentPosition = HoodPosition.DOWN;
        

//         starMotor.setInverted(true);
//         portMotor.follow(starMotor);
//         portMotor.setInverted(InvertType.OpposeMaster);
//         starMotor.setSensorPhase(true); //TODO check direction


//         starMotor.configNominalOutputForward(0, kTimeoutMs);
//         starMotor.configNominalOutputReverse(0, kTimeoutMs);
//         starMotor.configPeakOutputForward(1, kTimeoutMs);
//         starMotor.configPeakOutputReverse(-1, kTimeoutMs);

//         starMotor.config_kP(kPIDLoopIDX, kP, kTimeoutMs);
//         starMotor.config_kI(kPIDLoopIDX, kI, kTimeoutMs);
//         starMotor.config_kD(kPIDLoopIDX, kD, kTimeoutMs);
//         starMotor.config_kF(kPIDLoopIDX, kF, kTimeoutMs);

//     }

//     public void update() {
        
//         switch (currentState) {
//             case UNINITIALIZED:

//                 deployHopperStopper();
//                 wantedState = ShooterState.RUNNING;
//                 handleStateTransition(wantedState);
//                 break;

//             case RUNNING:

//                 break;

//             case SHOOT:
//                 //wait until the thingy is up to speed, and then open the hopper    
//                 if (shooterMotorStar.getClosedLoopError() < +kErrThreshold &&  //if the speed is correct
//                     shooterMotorStar.getClosedLoopError() > -kErrThreshold) {
                        
//                     if (atSpeedTicks >= 10) { //if its been at speed for a while
//                         retractHopperStopper();
//                         shooterAtSpeed = true; //sends a signal to start feeding into the shooter
//                         //this will make the motors slow down, causing them to go back to the speeding up phase
                        
//                     }
//                     atSpeedTicks ++;
                
//                 } else { //its not at speed, we are waiting to spin up.
//                     deployHopperStopper();
//                     shooterAtSpeed = false;
//                     atSpeedTicks = 0;
//                 }
//                 break;

//             case STOPPED:

//                 break;

//             default:
//                 currentState = ShooterState.UNINITIALIZED;
//                 break;
//         }
//         shooterMotorStar.set(TalonSRXControlMode.Velocity, targetVelocity_UnitsPer100ms);
//     }

//     public void setWantedState(final ShooterState wantedState) {
//         this.wantedState = wantedState;
//         handleStateTransition(wantedState);
//     }

//     private void handleStateTransition(final ShooterState wantedState){
//         switch(wantedState){
//             case UNINITIALIZED:
//                 break;
//             case STOPPED:
//                 targetVelocity_UnitsPer100ms = 0.0;
//                 currentState = ShooterState.STOPPED;
//                 shooterAtSpeed = false;
//                 break;
//             case RUNNING:
//                 targetVelocity_UnitsPer100ms = idle_UnitsPer100ms;
//                 deployHopperStopper();
//                 currentState = ShooterState.RUNNING;
//                 shooterAtSpeed = false;
//                 atSpeedTicks = 0;
//                 break;
//             case SHOOT:
//                 targetVelocity_UnitsPer100ms = shooting_UnitsPer100ms;
//                 currentState = ShooterState.SHOOT;
//                 break;
//         }
//     }

//     public ShooterState getCurrentState() {
//         return currentState;
//     }
    
//     //make the hood do stuff
//     public void toggleHood() {
//         hoodSolenoid.toggle();
//         if (currentPosition == HoodPosition.UP){
//             currentPosition = HoodPosition.DOWN;
//         } else {
//             currentPosition = HoodPosition.UP;
//         }
//     }
//     public boolean isHoodUp(){
//         if (currentPosition == HoodPosition.UP){
//             return true;
//         } else {
//             return false;
//         }
//     }  
//     public void hoodUp(){
//         if (isHoodUp()){
//             return;
//         } else {
//             hoodSolenoid.toggle();
//             currentPosition = HoodPosition.UP;
//         }
//     }
//     public void hoodDown() {
//         if (!isHoodUp()){
//             return;
//         } else {
//             toggleSolenoid(hoodSolenoid);
//             currentPosition = HoodPosition.DOWN;
//         }
//     }

//     //methods for the hopper stopper! they public so they can be used in the ball processor
//     public void toggleHopperStopper() {

//         toggleSolenoid(hoppaStoppa);
//         stopperDeployed = !stopperDeployed;
//     }

//     public void deployHopperStopper() {
//         hoppaStoppa.set(DoubleSolenoid.Value.kForward);
//         stopperDeployed = true;
//     }

//     public void retractHopperStopper() {
//         hoppaStoppa.set(DoubleSolenoid.Value.kReverse);
//         stopperDeployed = false;
//     }


//     enum HoodPosition {
//         UP,
//         DOWN
//     }

//     enum ShooterState {
//         UNINITIALIZED,
//         RUNNING,
//         SHOOT,
//         STOPPED
//     }
//     private void toggleSolenoid(final DoubleSolenoid solenoid){
//         if(solenoid.get() == DoubleSolenoid.Value.kForward){
//             solenoid.set(DoubleSolenoid.Value.kReverse);
//         }else{
//             solenoid.set(DoubleSolenoid.Value.kForward);
//         }
//     }
// }