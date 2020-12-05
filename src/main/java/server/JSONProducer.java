package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

import static server.utils.ThreadColors.*;


public class JSONProducer extends Thread {

    Socket socket;
    LinkedBlockingQueue<String> unformattedJSON;


    public JSONProducer(Socket socket, LinkedBlockingQueue<String> unformattedJSON) {
        this.socket = socket;
        this.unformattedJSON = unformattedJSON;
        //System.out.println("init: " + this.getClass());
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                while (true) {
                    System.out.println();
                    try {
                        String incomingMessage = input.readLine();
                        System.out.println(ANSI_GREEN + "Received client string: " + incomingMessage + ANSI_RESET);
                        unformattedJSON.put(incomingMessage);
                        System.out.println(ANSI_GREEN + "Added new JSON string to the queue. Current size: " + unformattedJSON.size() + ANSI_RESET);
                    } catch (SocketException e) {
                        // TODO add socket handling
                        System.out.println(ANSI_GREEN + "Lost connection to client." + ANSI_RESET);
                        return;
                    }
                }

            } catch (InterruptedException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
