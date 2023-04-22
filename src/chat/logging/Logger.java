package chat.logging;

import chat.server.Operation;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Provides convenience methods to log messages and errors along with a timestamp.
 */
public class Logger {
    /**
     * Log an error to STDERROR.
     */
    public static void logError(String error) {
        log(error, System.err);
    }

    /**
     * Log a message to STDOUT.
     */
    public static void logMessage(String msg) {
        log(msg, System.out);
    }

    // Helper method
    private static void log(String msg, PrintStream stream) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new java.util.Date());
        stream.printf("%s: %s\n", timeStamp, msg);
    }
}
