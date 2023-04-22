package chat.client;

import chat.logging.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A text-based interface for a key-value store client
 * to interact with the server.
 */
public class ClientTextInterface {
    /**
     * A client implementation instance.
     */
    private final Client client;

    /**
     * Create a new ClientInterface.
     *
     * @param client - client implementation to use.
     */
    public ClientTextInterface(Client client) {
        this.client = client;
        this.connect();
    }

    /**
     * Establish connection with the server.
     */
    private void connect() {
        try {
            this.client.connect();
        } catch (Exception e) {
            Logger.logError("Could not connect to the key-value store replica. Check if it is running and if you have a network connection.");
            System.exit(1);
        }

        Logger.logMessage("Connected to replica.");
    }

    /**
     * Run a command on the server by sending it using a client and print the response.
     *
     * @param command      - the command to run
     * @param printCommand - Whether to print the command in the argument. Useful when commands are not entered
     *                     interactively by the user, for example during pre-population. Helps to show the user
     *                     what commands are being run.
     */
    public void runCommand(String command, boolean printCommand) {
        try {
            if (printCommand) {
                System.out.printf("> %s\n", command);
            }

            String response = this.client.send(command);
            Logger.logMessage(String.format("$ %s", response));
        } catch (SocketTimeoutException e) {
            Logger.logError(String.format("Client timed out after %dms, reconnecting...", client.timeout));
            this.connect();
        } catch (IOException e) {
            Logger.logError("Something went wrong while executing your command at the replica. Please try again.");
        }
    }

    /**
     * Start the client interface.
     */
    public void start() {
        System.out.println("\nType 'help' to view documentation, or 'quit' to quit the command interface.");

        // Start the read-evaluate-print loop
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            try {
                String command = in.nextLine();
                if (command.equalsIgnoreCase("quit")) {
                    try {
                        this.client.disconnect();
                        return;
                    } catch (IOException e) {
                        Logger.logError("Something went wrong while terminating the client.");
                        System.exit(1);
                    }
                } else if (command.equalsIgnoreCase("help")) {
                    showHelp();
                    continue;
                }

                this.runCommand(command, false);
            } catch (NoSuchElementException e) {
                // Thrown when user hits Ctrl + D
                Logger.logMessage("Goodbye!");
                System.exit(0);
            }
        }
    }

    /**
     * Shows the help information on how to use the client and commands.
     */
    private static void showHelp() {
        System.out.println("Following are the available commands: (all case-insensitive)");

        System.out.println("\n - GET <key>");
        System.out.println("\t - Retrieves a value by its corresponding key");

        System.out.println(" - PUT <key> <value>");
        System.out.println("\t - Adds a key-value pair to the store");


        System.out.println(" - DELETE <key>");
        System.out.println("\t - Deletes a key-value pair from the store using its key");

        System.out.println("\nUse 'help' to view this documentation, or 'quit' to quit the command interface.");
    }
}
