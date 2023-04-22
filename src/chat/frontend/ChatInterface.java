package chat.frontend;

import chat.backend.ChatPeer;
import chat.backend.ChatPeerImpl;
import chat.logging.Logger;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ChatInterface {
    private ChatPeer engine;

    public ChatInterface(String displayName, int selfPort) throws IOException {
        Runnable serverThread = () -> {
            try {
                this.engine = new ChatPeerImpl(displayName, selfPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        new Thread(serverThread).start();
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
                    // TODO: Implement this
                    // this.client.disconnect();
                    return;
                } else if (command.equalsIgnoreCase("help")) {
                    // TODO
                    System.out.println("Help not implemented");
                    continue;
                }

                System.out.println(command);
            } catch (NoSuchElementException e) {
                // Thrown when user hits Ctrl + D
                Logger.logInfo("Goodbye!");
                System.exit(0);
            }
        }
    }

    // Entry point for replica server.
    public static void main(String[] args) {
        // Check if the required command line arguments are provided
        if (args.length != 2) {
            System.err.println("Expected arguments: <display-name> <port>");
            System.exit(1);
        }

        String displayName = args[0];
        int selfPort = Integer.parseInt(args[1]);

        Logger.setOwner(displayName, selfPort);

        // Create a server instances
        try {
            new ChatInterface(displayName, selfPort).start();
        } catch (IOException e) {
            Logger.logError("Could not start chat engine: " + e.getMessage());
        }
    }
}
