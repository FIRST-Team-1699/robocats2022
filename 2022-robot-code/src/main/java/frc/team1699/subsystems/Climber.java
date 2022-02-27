package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Climber {

    //define me some pistons
    DoubleSolenoid portPiston, starPiston;

    //construct me some climber
    public Climber(DoubleSolenoid portPiston, DoubleSolenoid starPiston){
        this.portPiston = portPiston;
        this.starPiston = starPiston;
    }

    //makes the climber extend
    public void climberUp(){
        portPiston.set(DoubleSolenoid.Value.kForward);
        starPiston.set(DoubleSolenoid.Value.kForward);
    }
    //makes the climber retract
    public void climberDown(){
        portPiston.set(DoubleSolenoid.Value.kReverse);
        starPiston.set(DoubleSolenoid.Value.kReverse);
    }
}
