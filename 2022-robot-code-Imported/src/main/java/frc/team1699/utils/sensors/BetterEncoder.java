package frc.team1699.utils.sensors;

public abstract class BetterEncoder {

    private final int port;

    public BetterEncoder(final int port) {
        this.port = port;
    }

    public abstract double getRate();

    public abstract double get(); //TODO Should return the total could. Might be able to remove abstract and just integrate rate
}