package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static RoutingTable routingTable;
    private static ExecutorService pool = Executors.newWorkStealingPool();



    public static void main(String[] args) throws IOException {

        //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        List<String> messages = new ArrayList<>();

        ServerSocket listener = new ServerSocket(0);
        System.out.println(listener.getLocalPort());
        Scanner sc = new Scanner(System.in);
        System.out.println("What is your username?");
        String userName = sc.nextLine();
        System.out.println("What is your neighbour's IP and Port? Format: <IP>:<PORT>");
        String neighbour = sc.nextLine();
        System.out.println("Name: " + userName);
        String localIp;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localIp = socket.getLocalAddress().getHostAddress();
        }

        if (localIp.startsWith("10")) {
            System.out.println(localIp + ":" + listener.getLocalPort());
        }


        String JSON = "{\"ip\" : \"10.8.0.2\",\"port\":1115,\"name\":\"asd\",\"hopCount\":0,\"outPort\":-1}";
        routingTable = new RoutingTable(localIp, listener.getLocalPort(), userName);
        routingTable.addEntry(Map.entry("10.8.0.5:645", RoutingEntry.getRoutingEntryFromJSON(JSON)));

        String[] details = neighbour.split(":");
        System.out.println("Details length: " + details.length);
        routingTable.addEntry(Map.entry(neighbour, new RoutingEntry(details[0], Integer.parseInt(details[1]), "", 0, Integer.parseInt(details[1]))));
        System.out.println(routingTable.getJSONTable());
        while (true) {
            System.out.println("[Server] waiting for client to connect.");
            Socket client = listener.accept();
            client.getLocalPort();
            System.out.println("[Server] connected to client.");

        }

    }

    public static void sendTable(String ip, int port) {

    }



}
