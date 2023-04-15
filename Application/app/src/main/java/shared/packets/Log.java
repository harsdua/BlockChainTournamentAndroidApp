package shared.packets;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private static Log instance = null;
    private final String logFileName = "log.txt";
    private BufferedWriter writer;
    private DateTimeFormatter formatter;

    private Log() {
        try {
            writer = new BufferedWriter(new FileWriter(logFileName, true));
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    public synchronized void writeLog(String message) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        String logMessage = timestamp + " - " + message;
        try {
            writer.write(logMessage);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void closeLog() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
