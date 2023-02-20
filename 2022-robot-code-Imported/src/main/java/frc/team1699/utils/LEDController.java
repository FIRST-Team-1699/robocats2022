package frc.team1699.utils;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.Robot;

public class LEDController {
    private static int rainbowFirstPixelHue = 50;
    private static int baconIterations = 0;
    private static int americaIterations = 0;

    private AddressableLED leds;
    private AddressableLEDBuffer ledBuffer;

    public LEDController(int ledLength, int port) {
        leds = new AddressableLED(port);
        ledBuffer = new AddressableLEDBuffer(ledLength);
        leds.setLength(Robot.ledLength);
    }

    public void start() {
        leds.start();
    }

    public void stop() {
        leds.stop();
    }

    public void baconColors() {
        if(baconIterations % 20 > 10) {
            for(int i = 0; i < Robot.ledLength; i += 2) {
                ledBuffer.setRGB(i, 0, 0, 255);
                if(i + 1 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 1, 250, 220, 0);
                }
            }
        } else {
            for(int i = 0; i < Robot.ledLength; i += 2) {
                ledBuffer.setRGB(i, 250, 220, 0);
                if(i + 1 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 1, 0, 0, 255);
                }
            }
        }
        leds.setData(ledBuffer);
        baconIterations++;
    }

    public void rainbow() {
        for (int i = 0; i < Robot.ledLength; i++) {
            final int hue = (rainbowFirstPixelHue + (i * 180 / Robot.ledLength)) % 180;
            ledBuffer.setHSV(i, hue, 255, 128);
        }
        rainbowFirstPixelHue += 3;
        rainbowFirstPixelHue %= 180;
        leds.setData(ledBuffer);
    }

    public void bus() {
        
    }

    public void solidColor(LEDColors targetColor) {
        int wantedR = 0;
        int wantedG = 0;
        int wantedB = 0;
        switch (targetColor) {
            case BLUE:
                wantedB = 250;
            break;
            case GREEN:
                wantedR = 124;
                wantedG = 252;
            break;
            case PINK:
                wantedR = 255;
                wantedG = 20;
                wantedB = 14;
            break;
            case PURPLE:
                wantedB = 250;
                wantedR = 220;
            break;
            case RED:
                wantedR = 250;
            break;
            case WHITE:

            break;
            case YELLOW:
                wantedR = 250;
                wantedG = 220;
            break;
            default:

            break;
            
        }
        for (int i = 0; i < Robot.ledLength; i++) {
            ledBuffer.setRGB(i, wantedR, wantedG, wantedB);
        }
        leds.setData(ledBuffer);
    }

    public void AMERICA() {
        if(americaIterations % 30 < 10) {
            for(int i = 0; i < Robot.ledLength; i += 3) {
                ledBuffer.setRGB(i, 255, 0, 0);
                if(i + 1 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 1, 255, 255, 255);
                }
                if(i + 2 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 2, 0, 0, 255);
                }
            }
        } else if(americaIterations % 30 < 20) {
            for(int i = 0; i < Robot.ledLength; i += 3) {
                ledBuffer.setRGB(i, 255, 255, 255);
                if(i + 1 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 1, 0, 0, 255);
                }
                if(i + 2 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 2, 255, 0, 0);
                }
            }
        } else {
            for(int i = 0; i < Robot.ledLength; i += 3) {
                ledBuffer.setRGB(i, 0, 0, 255);
                if(i + 1 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 1, 255, 0, 0);
                }
                if(i + 2 < Robot.ledLength) {
                    ledBuffer.setRGB(i + 2, 255, 255, 255);
                }
            }
        }
        leds.setData(ledBuffer);
        americaIterations++;    
    }

    public void redGold() {
        for(int i = 0; i < Robot.ledLength; i += 4) {
            ledBuffer.setRGB(i, 255, 0, 0);
            ledBuffer.setRGB(i + 1, 255, 0, 0);
            ledBuffer.setRGB(i + 2, 255, 0, 0);
            ledBuffer.setRGB(i + 3, 160, 126, 3);
        }
        leds.setData(ledBuffer);
    }

    public enum LEDColors {
        RED,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE,
        PINK,
        WHITE
    }
}
