package server.utils;

import server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static server.utils.ThreadColors.*;


public class JSONProducer extends Thread {

    private final Server server;
    List<Socket> sockets;
    Socket currSocket;
    LinkedBlockingQueue<String> unformattedJSON;


    public JSONProducer(List<Socket> sockets, LinkedBlockingQueue<String> unformattedJSON, Server server) {
        this.sockets = sockets;
        this.unformattedJSON = unformattedJSON;
        this.server = server;
        //System.out.println("init: " + this.getClass());
    }

    @Override
    public void run() {
        while (true) {
            try {

                while (true) {
                    try {
                        for (Socket socket : sockets) {
                            try {
                                currSocket = socket;
                                System.out.println("Listening on socket: " + currSocket);
                                String input = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                                System.out.println("switching sockets");
                                System.out.println(ANSI_GREEN + "Received client string: " + input + "\nOn port: " + socket.getLocalPort() + ANSI_RESET);

                                unformattedJSON.put(input);
                                System.out.println(ANSI_GREEN + "Added new JSON string to the queue. Current size: " + unformattedJSON.size() + ANSI_RESET);
                            } catch (SocketTimeoutException ignored) {}
                        }

                    } catch (SocketException e) {
                        server.getRoutingTable().remove(currSocket);
                        currSocket.close();
                        server.getRoutingTable().propagateTable(server.getDirectNeighbours());
                        // TODO add socket handling
                        System.out.println(ANSI_GREEN + "Lost connection to client." + ANSI_RESET);
                        return;
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
