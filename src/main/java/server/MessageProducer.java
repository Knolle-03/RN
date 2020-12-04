package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Queue;

public class MessageProducer extends Thread {

    Socket socket;
    Queue<String> messages;


    public MessageProducer(Socket socket, Queue<String> messages) {
        this.socket = socket;
        this.messages = messages;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String incomingMessage = input.readLine();
                    System.out.println("Received client string: " + incomingMessage);
                    messages.add(incomingMessage);
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        System.out.println("Thread interrupted");
//                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
