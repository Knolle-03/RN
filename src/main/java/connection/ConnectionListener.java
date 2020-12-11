package connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;

public class ConnectionListener extends Thread {

    ServerSocket serverSocket;
    Set<Connection> connections;

    public ConnectionListener(Set<Connection> connections, ServerSocket serverSocket) {
        this.connections = connections;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                System.out.println("Listening for new connections.");
                connections.add(new Connection(serverSocket.accept()));
                System.out.println("New connection established.\n Connections: " + connections);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
