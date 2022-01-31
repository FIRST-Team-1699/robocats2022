package frc.team1699.utils;

public class Utils {

    //From 254
    //Returns true if two number are very close to each other
    public static boolean epsilonEquals(double a, double b, double epsilon) {
        return (a - epsilon <= b) && (a + epsilon >= b);
    }
}
