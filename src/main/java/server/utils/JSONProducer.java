package server.utils;

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
    BufferedReader input;
    String currentMsg;

    public JSONProducer(Socket socket, LinkedBlockingQueue<String> unformattedJSON) {
        this.socket = socket;
        this.unformattedJSON = unformattedJSON;
        //System.out.println("init: " + this.getClass());
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                
                while (true) {
                    try {


                        currentMsg = input.readLine();
                        System.out.println(ANSI_GREEN + "Received client string: " + currentMsg + ANSI_RESET);

                        if (currentMsg != null) {
                            unformattedJSON.put(currentMsg);
                            System.out.println(ANSI_GREEN + "Added new JSON string to the queue. Current size: " + unformattedJSON.size() + ANSI_RESET);
                        } else {
                            break;
                            //System.out.println("JSON was ill-formed. (JSON: " + currentMsg + ")");
                        }
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
