/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.team1699.utils.sensors;

import edu.wpi.first.wpilibj.DigitalInput;

/**
 * Super simple wrapper function, will add addional functions as needed 
 */
public class LimitSwitch {
    private DigitalInput lswitch;
    public LimitSwitch( int port){
        lswitch = new DigitalInput(port);
    }

    public boolean isPressed(){
        return lswitch.get();
    }
    public void printValue(){
        
   // System.out.println("Limit value: " +lswitch.get());
    }
}
