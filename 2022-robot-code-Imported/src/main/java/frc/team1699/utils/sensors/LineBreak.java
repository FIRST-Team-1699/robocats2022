package frc.team1699.utils.sensors;

import edu.wpi.first.wpilibj.DigitalInput;

public class LineBreak {
    private DigitalInput sensor;
    
    public LineBreak(int port){
        sensor = new DigitalInput(port);
    }

    public boolean get(){
        return sensor.get();
    }

}
