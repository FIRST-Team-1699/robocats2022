package frc.team1699.utils.logger;

import edu.wpi.first.wpilibj.DriverStation;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.sql.Timestamp;
import java.time.Instant;
import java.io.FileWriter;
import java.lang.Exception;
import java.io.IOException;

public class Logger implements Runnable {

    private static Logger instance;

    public Logger getInstance() throws Exception {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private static final String LOG_FORMAT = "[" + DriverStation.getInstance().getMatchTime() + "] - %s";

    private final List<Loggable> alwaysLogged;
    private final Queue<String> toLog;
    private boolean running = false;
    private Thread thread;
    private int ticks = 0;
    private final FileWriter writer;

    private Logger() throws Exception {
        alwaysLogged = new ArrayList<>();
        toLog = new ConcurrentLinkedQueue<>();
        writer = new FileWriter("/home/lvuser/logs/" + Timestamp.from(Instant.now()).toString() + ".txt");
    }

    @Override
    public void run() {
        while (running) {
            //Writes to the log every 20 ticks
            if (ticks % 20 == 0) {
                //Log
                try {
                    for (Loggable l : alwaysLogged) {
                        writer.write(String.format(LOG_FORMAT, l.getLogOutput()));
                    }
                    while (toLog.peek() != null) {
                        writer.write(String.format(LOG_FORMAT, toLog.poll()));
                    }
                    writer.flush(); //We only want to flush where the writer has stuff in it
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ticks++;
        }
    }

    /**
     * Writes a message to the log once
     * @param message The message to log
     */
    public synchronized void log(String message) {
        toLog.add(message);
    }

    /**
     * Writes a message to the log continuously
     * @param loggable What is to be logged
     */
    public synchronized void addContinuousMessage(Loggable loggable){
        alwaysLogged.add(loggable);
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        close();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
