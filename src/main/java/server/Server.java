package server;

import server.routing.RoutingEntry;
import server.routing.RoutingTable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.utils.ThreadColors.*;


public class Server {

    private RoutingTable routingTable;
    private ExecutorService workerPool = Executors.newFixedThreadPool(10);
    private Scanner sc;
    private String initialNeighbour;
    private String userName;
    private Queue<String> messages = new ArrayBlockingQueue<>(100);

    // concurrent?
    private List<Socket> directNeighbours = new ArrayList<>(); //Collections.synchronizedList(new ArrayList<>());

    public Server() {
        getClientInfo();
        initServer();
        initRoutingTable();
    }

    public static void sendTable(String ip, int port) {

    }

    public void getClientInfo() {
        sc = new Scanner(System.in);
        System.out.println("What is your username?");
        userName = sc.nextLine();
        System.out.println("What is your neighbour's IP and Port? Format: <IP>:<PORT>");
        initialNeighbour = sc.nextLine();
        System.out.println("Name: " + userName);

    }

    public void initServer() {
        try (ServerSocket newConnectionListener = new ServerSocket(0)){
            InetAddress address = InetAddress.getLocalHost();
            System.out.println(ANSI_BLUE + "Server init successful. Connection: " + address + ":" + newConnectionListener.getLocalPort());

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initRoutingTable() {
        String[] details = initialNeighbour.split(":");
        routingTable.addEntry(Map.entry(initialNeighbour, new RoutingEntry(details[0], Integer.parseInt(details[1]), "", 0, Integer.parseInt(details[1]))));
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {


            while (true) {
                System.out.println("Listening for new Clients.");
                Socket neighbour = serverSocket.accept();
                directNeighbours.add(neighbour);
                new MessageProducer(neighbour, messages).start();
                System.out.println("Created new MessageProducer.");
            }
        } catch (IOException e) {
            System.out.println("Server exception: "  + e.getMessage());
        }
    }



}
