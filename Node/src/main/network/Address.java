package main.network;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class Address implements Serializable {

    private final String IP;
    private final int port;
    private LocalDateTime dateOfCreation;

    public Address(String IP, int port) {
        this(IP,port,LocalDateTime.now());
    }
    public Address(String IP, int port, LocalDateTime dateOfCreation) {
        this.IP = IP;
        this.port = port;
        this.dateOfCreation = dateOfCreation;
    }

    public LocalDateTime getDate() {
        return dateOfCreation;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    /**
     * @return true if outdated, false otherwise
     */

    public boolean isOutdated() {
        //Default TTL is 10 minutes
        return hasDated(60);
    }

    public boolean isAlmostOutdated() {
        //Default TTL is 10 minutes
        return hasDated(30);
    }

    public boolean hasDated(double maxDeltaSeconds) {
        long differenceTillNow = dateOfCreation.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        return differenceTillNow >= maxDeltaSeconds;
    }

    public void refreshTimeStamp() {
        dateOfCreation = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return IP + "," + port + "," + dateOfCreation.toString();
    }
}