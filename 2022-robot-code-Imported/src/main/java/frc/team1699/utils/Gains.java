package frc.team1699.utils;

public class Gains {

    public final double kP, kI, kD, kF, kPeakOutput;
    public final int kIzone;

    public Gains(double kP, double kI, double kD, double kF, double kPeakOutput, int kIzone) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = kF;
        this.kPeakOutput = kPeakOutput;
        this.kIzone = kIzone;
    }
}