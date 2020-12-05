package client;

import com.google.gson.Gson;
import server.MessageWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientMessageConsumer extends Thread {

    Socket socket;
    String JSON;
    MessageWrapper messageWrapper;


    public ClientMessageConsumer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                JSON = input.readLine();
                // TODO: Rewrite if client is supposed to have it's own up-to-date routing table.
                if (!JSON.contains("hopCount")) continue;

                messageWrapper = new Gson().fromJson(JSON, MessageWrapper.class);
                System.out.println("Received new Message from " + messageWrapper.getFromName() + ": " + messageWrapper.getMessage());
            } catch (IOException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
