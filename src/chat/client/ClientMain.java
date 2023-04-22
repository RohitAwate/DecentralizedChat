package chat.client;

/**
 * Entrypoint for the client.
 */
public class ClientMain {
    public static void main(String[] args) {
        // Check if the required command line arguments are provided
        if (args.length != 2) {
            System.err.println("Expected arguments: <host> <port>");
            System.exit(1);
        }

        // Read the args
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int clientTimeout = 1000; // milliseconds

        // Create a client instance based on the desired transport to use
        Client client = new RMIClient(host, port, clientTimeout);

        // Create a client interface instance and pass in the client to it
        ClientTextInterface cli = new ClientTextInterface(client);

        // Pre-population commands.
        // 5 PUT, 5 GET and 5 DELETE ops.
        String[] preCommands = {
                "put hello world",
                "put joe biden",
                "put donald trump",
                "put barack obama",
                "put tom cruise",

                "get hello",
                "get joe",
                "get donald",
                "get barack",
                "get tom",

                "delete hello",
                "delete joe",
                "delete donald",
                "delete barack",
                "delete tom"
        };

        // Run the pre-population commands
        for (String command : preCommands) {
            cli.runCommand(command, true);
        }

        // Start the client interface for user to interact
        cli.start();
    }
}
