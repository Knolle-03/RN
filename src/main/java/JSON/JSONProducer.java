package JSON;

import client.Client;
import connection.Connection;
import utils.Utils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static utils.Utils.ThreadColors.*;

public class JSONProducer extends Thread {

    private final Client client;
    private final LinkedBlockingQueue<Connection> connections;
    private final LinkedBlockingQueue<String> incomingJSON;

    public JSONProducer(Client client) {
        this.client = client;
        this.connections = client.getConnections();
        this.incomingJSON = client.getIncomingJSON();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            for (Connection c : connections) {
                try {
                    String msg = c.getReader().readLine();
                    System.out.println("....");
                    incomingJSON.put(msg);
                    new JSONConsumer(client, c.getSocket()).start();
                    System.out.println("consumer started.");

                }
                catch (SocketTimeoutException ignored) {}
                catch (IOException | NullPointerException  e) {
                    System.out.println(ANSI_RED_BACKGROUND + "Connection: " + c + " is not available." + ANSI_RESET);
                    if (connections.remove(c)) {
                        Utils.removeCorrespondingEntries(client.getRoutingTable(), c.getSocket());
                        Utils.propagateRoutingTable(client.getRoutingTable());
                        System.out.println("Removed connection: " + c + " and updated routing table");
                    }
                }
                catch (InterruptedException e) {
                    System.out.println(ANSI_RED_BACKGROUND + "Unable to put new message in queue." + ANSI_RESET);
                    e.printStackTrace();
                }
            }
        }
    }
}
