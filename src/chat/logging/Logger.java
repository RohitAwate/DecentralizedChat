package chat.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Provides convenience methods to log messages and errors along with a timestamp.
 */
public class Logger {
	private static PrintStream logStream = null;

	public static void setOwner(String displayName, int port) {
		String fileName = String.format("app_data/%s-%d/logs.txt", displayName, port);
		Path path = FileSystems.getDefault().getPath(fileName);
		try {
			Files.createDirectories(path.getParent());
			logStream = new PrintStream(new BufferedOutputStream(
					Files.newOutputStream(path, CREATE, APPEND)
			));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to create log file");
			System.exit(1);
		}
	}

	public static void logError(String msg) {
		if (logStream == null) {
			System.err.println("Log owner not set");
			return;
		}

		log(msg, "ERROR");
	}


	public static void logInfo(String msg) {
		if (logStream == null) {
			System.err.println("Log owner not set");
			return;
		}

		log(msg, "INFO");
	}

	// Helper method
	private static void log(String msg, String level) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new java.util.Date());
		logStream.printf("[%s] %s: %s\n", level, timeStamp, msg);
		logStream.flush();
	}
}
