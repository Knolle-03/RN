package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final RoutingTable routingTable = new RoutingTable();

    private static ExecutorService pool = Executors.newWorkStealingPool();


    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(0);

        System.out.println("Server IP: " + InetAddress.getLocalHost());
        System.out.println("Listening on port: " + listener.getLocalPort());



        while (true) {
            System.out.println("[Server] waiting for client to connect.");
            Socket client = listener.accept();
            System.out.println("[Server] connected to client.");


        }

    }

    public static void sendTable(String ip, int port) {

    }



}
