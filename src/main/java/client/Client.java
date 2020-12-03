package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private static final String SERVER_IP = "127.0.0.1";


    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(SERVER_IP, 18849);
        System.out.println(InetAddress.getLocalHost());


        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            System.out.println("> ");
            String command = keyboard.readLine();
            if (command.equals("quit")) break;

            out.println(command);
            String serverResponse = reader.readLine();
            System.out.println(serverResponse);
        }





        socket.close();
        System.exit(0);
    }



}
