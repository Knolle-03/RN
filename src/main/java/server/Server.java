package server;

import client.Client;
import server.routing.RoutingConsumer;
import server.routing.RoutingEntry;
import server.routing.RoutingTable;
import server.utils.JSONValidator;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static server.utils.ThreadColors.*;


public class Server {

    private RoutingTable routingTable;
    private String ip;
    private int port;
    private ExecutorService messageWorkerPool = Executors.newFixedThreadPool(10);
    private ExecutorService routingWorkerPool = Executors.newFixedThreadPool(10);
    private ExecutorService JSONValidatorPool = Executors.newFixedThreadPool(10);
    private ExecutorService JSONProducerPool = Executors.newCachedThreadPool();
    private Scanner sc;
    private String initialNeighbourName;
    private String initialNeighbourAddress;
    private String userName;
    ServerSocket newConnectionListener;
    private LinkedBlockingQueue<String> unformattedJSON = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<MessageWrapper> messageWrappers = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<RoutingTable> routingTables = new LinkedBlockingQueue<>();
    private Client client;
    // concurrent?
    private List<Socket> directNeighbours = new ArrayList<>(); //Collections.synchronizedList(new ArrayList<>());

    public Server() {
        getClientInfo();
        initServer();
        initRoutingTable();
        for (int i = 0; i < 10; i++) {
            messageWorkerPool.execute(new MessageConsumer(messageWrappers, routingTable, directNeighbours));
            routingWorkerPool.execute(new RoutingConsumer(routingTable, routingTables, directNeighbours));
            JSONValidatorPool.execute(new JSONValidator(unformattedJSON, messageWrappers, routingTables));
        }
        getFirstConnectionInfo();
        connectToNewServer();

        run();
    }


    public void getClientInfo() {

        sc = new Scanner(System.in);
        System.out.println(ANSI_BLUE + "What is your username?");
        userName = "Server_1";//sc.nextLine();
//        System.out.println(ANSI_BLUE + "What is your neighbour's IP and Port? Format: <IP>:<PORT>");
//        initialNeighbour ="10.8.0.2:5000";// sc.nextLine();
        System.out.println(ANSI_BLUE + "Name: " + userName + ANSI_RESET);

    }

    public void getFirstConnectionInfo() {
        System.out.print("Enter the name of the server you want to connect to: ");
        initialNeighbourName = sc.nextLine();
        System.out.println(ANSI_BLUE + "What is the server's IP and Port? Format: <IP>:<PORT>");
        initialNeighbourAddress = sc.nextLine();
    }

    public void connectToNewServer()  {
        String[] split = initialNeighbourAddress.split(":");
        try (Socket socket = new Socket(split[0], Integer.parseInt(split[1]))){
            directNeighbours.add(socket);
            RoutingTable routingTable = new RoutingTable(split[0], Integer.parseInt(split[1]), initialNeighbourName, socket.getPort());
            routingTable.update(routingTable, directNeighbours);

        } catch (IOException e) {
            System.out.println("Connection to server failed.");
            e.printStackTrace();
        }
    }

    public void initServer() {
        try {
            newConnectionListener = new ServerSocket(5001);
            //TODO make dynamically
            /*
            InetAddress address = InetAddress.getLocalHost();
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                //ip = address.getHostAddress();
                ip = InetAddress.getLocalHost().getHostAddress();
            }
            */
            String hName = InetAddress.getLocalHost().getHostName();
            InetAddress addrs[] = InetAddress.getAllByName(hName);
            for (int i = 0; i < addrs.length; i++) {
                if (addrs[i].getHostAddress().contains("10.8.0.")) {
                    ip = addrs[i].getHostAddress();
                    break;
                }
            }
            port = newConnectionListener.getLocalPort();
            System.out.println(ANSI_BLUE + "Server init successful. Connection: " + ip + ":" + port + ANSI_RESET);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initRoutingTable() {
        routingTable = new RoutingTable(ip, port, userName);
        System.out.println(routingTable.getJSONTable());
    }

    public void run() {
        try {
            while (true) {
                System.out.println(ANSI_BLUE + "Listening for new Clients." + ANSI_RESET);
                Socket neighbour = newConnectionListener.accept();
                directNeighbours.add(neighbour);
                JSONProducer msgPrd = new JSONProducer(neighbour, unformattedJSON);
                msgPrd.setName("Message Producer for : " + neighbour.getInetAddress() + ":" + neighbour.getPort());
                JSONProducerPool.execute(msgPrd);
                System.out.println(ANSI_BLUE + "Created new MessageProducer " + msgPrd.getName() + ANSI_RESET);
            }
        } catch (IOException e) {
            System.out.println(ANSI_CYAN + "Server exception: "  + e.getMessage() + ANSI_RESET);
        }
    }



}
