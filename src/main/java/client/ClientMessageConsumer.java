package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.MessageWrapper;
import server.routing.RoutingTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

public class ClientMessageConsumer extends Thread {

    Socket socket;
    String JSON;
    MessageWrapper messageWrapper;
    Client client;


    public ClientMessageConsumer(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                JSON = input.readLine();
                // TODO: Rewrite if client is supposed to have it's own up-to-date routing table.
                //System.out.println(JSON);
                if (JSON.contains("hopCount")) {
                    System.out.println("Got new Routing infos. Creating new ClientRoutingConsumer thread.");
                    RoutingTable info = new GsonBuilder().setPrettyPrinting().create().fromJson(JSON, RoutingTable.class);
                    System.out.println(info);
                    new ClientRoutingConsumer(client, info);
                } else {
                    messageWrapper = new Gson().fromJson(JSON, MessageWrapper.class);
                    System.out.println(ANSI_CYAN + "Received new Message from " + messageWrapper.getFromName() + ": " + ANSI_CYAN + messageWrapper.getMessage()+ ANSI_RESET);
                }
            } catch (IOException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {

                } catch (IllegalMonitorStateException ex) {
                    ex.printStackTrace();
                    System.out.println("Lost connection to Server. Please reconnect.");
                    System.exit(1);
                }
            }
        }
    }
}
