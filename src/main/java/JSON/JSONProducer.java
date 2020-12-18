package JSON;

import client.Client;
import connection.Connection;

import java.io.IOException;
import java.net.Socket;
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
                    if (c.getReader().ready()) {
                        Socket sendingSocket = c.getSocket();
                        String msg = c.getReader().readLine();
                        System.out.println("New msg: " + msg + " on socket: " + c.getSocket());

                        incomingJSON.put(msg);

                        new JSONConsumer(client, sendingSocket).start();
                    }
                } catch (IOException e) {
                    System.out.println(ANSI_GREEN_BACKGROUND + "Reader of connection: " + c + " is not available." + ANSI_RESET);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println(ANSI_GREEN_BACKGROUND + "Unable to put new message in queue." + ANSI_RESET);
                    e.printStackTrace();
                }
            }
        }
    }
}
