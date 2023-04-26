package chat.frontend;

import chat.backend.ChatBackend;
import chat.backend.ChatEngine;
import chat.logging.Logger;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ChatInterface {
	private final ChatBackend backend;

	public ChatInterface(String displayName, int selfPort) throws IOException {
		this.backend = new ChatEngine(displayName, selfPort);
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

				processCommand(command);
			} catch (NoSuchElementException e) {
				// Thrown when user hits Ctrl + D
				Logger.logInfo("Goodbye!");
				System.exit(0);
			}
		}
	}

	private void processCommand(String command) {
		String[] tokens = command.trim().split("\\s+");

		// Check the first token to see which command it is.
		// Dispatch control to respective handler.
		// Syntax is case-insensitive.
		String op = tokens[0];
		switch (op.toLowerCase()) {
			case "join":
				this.joinGroupHandler(tokens);
				break;
			case "create":
				this.createGroupHandler(tokens);
				break;
			default:
				System.out.println("Unrecognized command: " + op);
		}
	}

	private void joinGroupHandler(String[] args) {
		// Error checking for number of tokens
		if (args.length != 4) {
			System.out.println("Invalid syntax for JOIN. Usage: JOIN <ip> <port> <group_name>");
			return;
		}

		String ip = args[1];
		int port = Integer.parseInt(args[2]);
		String groupName = args[3];

//		if (backend.joinGroup(ip, port, groupName)) {
//
//		}
	}

	private void createGroupHandler(String[] tokens) {
		// Error checking for number of tokens
		if (tokens.length != 4) {
			System.out.println("Invalid syntax for JOIN. Usage: JOIN <ip> <port> <group_name>");
			return;
		}

		System.out.println(tokens);
	}
}
