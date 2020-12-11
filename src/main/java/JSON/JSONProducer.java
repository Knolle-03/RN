package JSON;

import client.Client;
import connection.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class JSONProducer extends Thread {

    private Client client;
    private Set<Connection> connections;
    private LinkedBlockingQueue<String> incomingJSON;
    private ExecutorService threadPool;

    public JSONProducer(Client client) {
        this.client = client;
        this.connections = client.getConnections();
        this.incomingJSON = client.getIncomingJSON();
        this.threadPool = client.getThreadPool();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            for (Connection c : connections) {

                try {
                    // test if connection still exists
//                    if (!c.getReader().ready() && c.getReader().read() == -1) {
//                        System.out.println("Client left.");
//                        connections.remove(c);
//                        continue;
//                    }
                    if (c.getReader().ready()) {
                        Socket sendingSocket = c.getSocket();
                        String msg = c.getReader().readLine();
                        incomingJSON.put(msg);
                        new JSONConsumer(client, sendingSocket).start();
                    }
                } catch (IOException e) {
                    System.out.println("Reader of connection: " + c + " is not available.");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("Unable to put new message in queue.");
                    e.printStackTrace();
                }
            }
        }
    }
}
