package shared.packets;

import main.ledger.Blockchain;
import main.node.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static Log instance = null;
    private String WhoWeLog = "";
    private BufferedWriter writer;
    private DateTimeFormatter formatter;

    private Log(String whoWeLog) {
        this.WhoWeLog = whoWeLog;
        String logFileName = whoWeLog + "log.txt";
        try {
            writer = new BufferedWriter(new FileWriter(logFileName, true));
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Log() {
    }


    public static synchronized Log getInstance(String whoWeLog) {
        if (instance == null) {
            instance = new Log(whoWeLog);
        }
        return instance;
    }
    public static synchronized Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    // verbose methode
    public synchronized void writeLog(String message) {
        String logMessage = craftingMessage(message);
        // verbose
        System.out.println(" [ LOG ]         ### " + logMessage);
        writingLog(logMessage);
    }
    // silent methode
    public synchronized void writeLog(String message, String muted) {
        String logMessage = craftingMessage(message);
        writingLog(logMessage);
    }

    public synchronized void writingLog(String logMessage) {
        try {
            writer.write(logMessage);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writingBC(Blockchain bc) {
        try {
            FileOutputStream f = new FileOutputStream(new File("Blockchain.txt"));
            ObjectOutputStream o = new ObjectOutputStream(f);

            // Write objects to file
            o.writeObject(bc);

            o.close();
            f.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    public synchronized Blockchain readBC() {
        Blockchain bc = null;
        try {
            FileInputStream fi = new FileInputStream(new File("Blockchain.txt"));
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects
            bc  = (Blockchain) oi.readObject();
            System.out.println(bc.toString());

            oi.close();
            fi.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bc;

    }

    private String craftingMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        return timestamp + " @" + this.WhoWeLog + " - " + message;
    }

    public synchronized void closeLog() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
