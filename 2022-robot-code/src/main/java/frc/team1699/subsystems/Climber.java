package frc.team1699.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;


public class Climber {

    //define me some pistons
    DoubleSolenoid portPiston, starPiston;

    /**
    *
    * this is an extraordinarily complicated class, and it would
    * take me years to fully explain it to you. the climber has
    * so much specific fuctionality, and it is very complicated.
    * 
    *
    * expect to be debugging this for many hours. good luck.
    *
    */
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
