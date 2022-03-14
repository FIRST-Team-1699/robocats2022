package frc.team1699.subsystems;

import frc.team1699.Constants;
import edu.wpi.first.wpilibj.DriverStation;
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

import org.w3c.dom.views.DocumentView;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team1699.utils.sensors.LimitSwitch;
import frc.team1699.utils.sensors.LimeLight;
import frc.team1699.subsystems.Autos;

/**
* autonomoussy
*/
public class Autos {

    double autotarget = 68000.0; // a somewhat arbitrary distance that we know gets off the tarmac
    boolean moveDone;

    //set up my thingies
    private BallProcessor ballProcessor;
    private DriveTrain driveTrain;
    private IntakeHopper intakeHopp;
    private Shooter shooter;

    public PneumaticsModuleType CTREPCM = PneumaticsModuleType.CTREPCM;
    private DoubleSolenoid shooterAngleSolenoid;

    private TalonFX portDriveMaster, starDriveMaster;
    

    public Autos(BallProcessor ballProcessor, DriveTrain driveTrain, IntakeHopper intakeHopp, Shooter shooter, TalonFX portDriveMaster, TalonFX starDriveMaster){

        this.ballProcessor = ballProcessor;
        this.driveTrain = driveTrain;
        this.intakeHopp = intakeHopp;
        this.shooter = shooter;

        this.portDriveMaster = portDriveMaster;
        this.starDriveMaster = starDriveMaster;

        shooterAngleSolenoid = new DoubleSolenoid(Constants.kShooterAngleSolenoidModulePort, CTREPCM, Constants.kShooterAngleSolenoidForwardPort, Constants.kShooterAngleSolenoidReversePort);
    }
    
    public void twoBallInit(){
        portDriveMaster.setSelectedSensorPosition(0.0);
        moveDone = false;
        LimeLight.getInstance().turnOn();

        shooter.toggleSolenoid(shooterAngleSolenoid);

        driveTrain.setWantedState(DriveState.AUTONOMOUS);
        ballProcessor.setProcessorState(BallProcessState.COLLECTING);
    }
    
    public void twoBallPeriodic(){
        


        shooter.hoodUp(); //you would think this would be easy
        
        

        double forward = 0.0, turn = 0.0;
        if (!moveDone) {

            if (portDriveMaster.getSelectedSensorPosition() >= autotarget){ //moves until the target has been reached
                moveDone = true;

            } else {
                forward = 0.5;
            }
        }
        if (moveDone) { //once the distance has been reached...
            if (LimeLight.getInstance().getTV() < 1) { //if there is no target yet...
                forward = 0;
                turn = 0.4; //start spinning to look for the goal

            } else { //if it sees the target, shoot.
                driveTrain.setWantedState(DriveState.GOAL_TRACKING);
                ballProcessor.setProcessorState(BallProcessState.LOADED);
                
               ballProcessor.startShooting();
            }

        }

        driveTrain.setAutoDemand(forward, turn);

        shooter.update();
        intakeHopp.update();
        ballProcessor.update();
        driveTrain.update();
    }

    public void sitThenGoInit(){
        portDriveMaster.setSelectedSensorPosition(0.0);
        driveTrain.setWantedState(DriveState.AUTONOMOUS);
    }

    public void sitThenGoPeriodic(){
        double forward = 0.0, turn = 0.0;

        if (DriverStation.getMatchTime() <= 5){           //this will wait until there is 5 seconds left in auto and make it go forward
            if (portDriveMaster.getSelectedSensorPosition() < autotarget){
                forward = 0.5;
            } else {
                forward = 0;
            }
        }
        driveTrain.setAutoDemand(forward, turn);
        driveTrain.update();
    }
}
