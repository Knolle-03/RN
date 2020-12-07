package server;

import client.Client;
import lombok.Data;
import server.routing.RoutingConsumer;
import server.routing.RoutingEntry;
import server.routing.RoutingTable;
import server.utils.JSONConsumer;
import server.utils.JSONProducer;
import server.utils.RoutingInfoThread;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static server.utils.ThreadColors.*;

@Data
public class Server {

    private RoutingTable routingTable = new RoutingTable();
    private String ip;
    private int port;
    private ExecutorService messageWorkerPool = Executors.newFixedThreadPool(10);
    private ExecutorService routingWorkerPool = Executors.newFixedThreadPool(10);
    private ExecutorService JSONValidatorPool = Executors.newFixedThreadPool(10);
    private ExecutorService JSONProducerPool = Executors.newFixedThreadPool(10);
    private Scanner sc;
    private String initialNeighbourName;
    private String initialNeighbourAddress;
    private String userName = "Server_1";
    ServerSocket newConnectionListener;
    private LinkedBlockingQueue<String> unformattedJSON = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<MessageWrapper> messageWrappers = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<RoutingTable> routingTables = new LinkedBlockingQueue<>();
    private Client client;
    private Socket newSocket;

    private int serverSocketPort = 5003;
    // concurrent?
    private List<Socket> directNeighbours = new ArrayList<>(); //Collections.synchronizedList(new ArrayList<>());

    public Server() {
        getClientInfo();
        initServer();
        initRoutingTable();
        startWorkerPools();


        run();
    }

    public Server(String serverName, String serverIP, int serverPort, int serverSocketPort, String userName) {
        this.serverSocketPort = serverSocketPort;
        this.userName = userName;
        getClientInfo();
        initServer();
        initRoutingTable();
        startWorkerPools();

        connectToNewServer(serverName, serverIP, serverPort);
        System.out.println("About to exe run();");
        run();
    }


    public void startWorkerPools() {
        for (int i = 0; i < 10; i++) {
            messageWorkerPool.execute(new MessageConsumer(messageWrappers, routingTable, directNeighbours));
            routingWorkerPool.execute(new RoutingConsumer(routingTable, routingTables, directNeighbours));
            JSONValidatorPool.execute(new JSONConsumer(unformattedJSON, messageWrappers, routingTables));
            JSONProducerPool.execute(new JSONProducer(directNeighbours, unformattedJSON, this));

        }
    }


    public void getClientInfo() {

        sc = new Scanner(System.in);
        System.out.println(ANSI_BLUE + "What is your username?");
        //userName = "Server_1";//sc.nextLine();
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

    public void connectToNewServer(String serverName, String serverIP, int serverPort)  {
        try {
            newSocket = new Socket(serverIP, serverPort);
            printSocketInfo(newSocket);
            directNeighbours.add(newSocket);
            newSocket.setSoTimeout(1);

            routingTable.addEntry(new RoutingEntry(serverIP, serverPort, serverName, 1, newSocket), directNeighbours);
            new RoutingInfoThread(directNeighbours, routingTable).start();



        } catch (IOException e) {
            System.out.println("Connection to server failed.");
            e.printStackTrace();
        }
    }

    public void initServer() {
        try {
            newConnectionListener = new ServerSocket(serverSocketPort);
            //TODO make dynamically

            InetAddress address = InetAddress.getLocalHost();
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                //ip = address.getHostAddress();
                ip = InetAddress.getLocalHost().getHostAddress();
            }

//            String hName = InetAddress.getLocalHost().getHostName();
//            InetAddress addrs[] = InetAddress.getAllByName(hName);
//            for (int i = 0; i < addrs.length; i++) {
//                if (addrs[i].getHostAddress().contains("10.8.0.")) {
//                    ip = addrs[i].getHostAddress();
//                    break;
//                }
//            }
            port = newConnectionListener.getLocalPort();
            System.out.println(ANSI_BLUE + "Server init successful. Connection: " + ip + ":" + port + ANSI_RESET);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initRoutingTable() {
        routingTable = new RoutingTable(ip, port, userName, directNeighbours);
        System.out.println(routingTable.getJSONTable());
        new RoutingInfoThread(directNeighbours, routingTable).start();
    }

    public void run() {
        try {
            while (true) {
                System.out.println(ANSI_BLUE + "Listening for new Clients." + ANSI_RESET);
                Socket neighbour = newConnectionListener.accept();
                printSocketInfo(neighbour);
                directNeighbours.add(neighbour);
                neighbour.setSoTimeout(1);
                routingTable.addEntry(new RoutingEntry(neighbour.getInetAddress().getHostAddress(), neighbour.getPort(), "", 1, neighbour), directNeighbours);


                System.out.println("direct neighbours in run(): " + directNeighbours);
                //JSONProducer msgPrd = new JSONProducer(neighbour, unformattedJSON, this);
                //msgPrd.setName("Message Producer for : " + neighbour.getInetAddress() + ":" + neighbour.getPort());
                //JSONProducerPool.execute(msgPrd);
                //System.out.println(ANSI_BLUE + "Created new MessageProducer " + msgPrd.getName() + ANSI_RESET);
            }
        } catch (IOException e) {
            System.out.println(ANSI_CYAN + "Server exception: "  + e.getMessage() + ANSI_RESET);
        }
    }


    public static void main(String[] args) {
        //Server server = new Server();
        Server server = new Server("Server_1", "10.8.0.3", 5003, 5004, "Server_2");
    }


    public void printSocketInfo(Socket socket) {
        System.out.println("Local socket port: " + socket.getLocalPort());
        System.out.println("Remote socket port: " + socket.getPort());
        System.out.println("Local Address: " + socket.getLocalAddress());
        System.out.println("Local socket address: " + socket.getLocalSocketAddress());
        System.out.println("InetAddress: " + socket.getInetAddress());
    }


}
