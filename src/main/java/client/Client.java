package client;

import JSON.JSONProducer;
import connection.Connection;
import connection.ConnectionListener;
import message.MessageWrapper;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import routing.RoutingEntry;
import utils.Utils;
import static utils.Utils.ThreadColors.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


@Data
public class Client {

    private String myIP;
    private int myPort;
    private String myName;

    private Set<RoutingEntry> routingTable = new HashSet<>();
    private LinkedBlockingQueue<String> incomingJSON = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<MessageWrapper> messages = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Set<RoutingEntry>> newRoutingInfos = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Connection> connections = new LinkedBlockingQueue<>();
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader keyboard;
    private PrintWriter out;
    private String message;

    private int startTTL = 20;
    private Gson gson = new Gson();
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Scanner scanner = new Scanner(System.in);


    public Client(int myServerPort) {
        myPort = myServerPort;
    }

    public void start() {
        init();
        listen(myPort);
        getMyInfo();
        while (true) whatsNext();
    }

    public void listen(int myServerPort) {
        try {
            serverSocket = new ServerSocket(myServerPort);
            threadPool.execute(new ConnectionListener(connections, serverSocket));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        threadPool.execute(new JSONProducer(this));
        keyboard = new BufferedReader(new InputStreamReader(System.in));
    }

    private void sendMessage() {
        String name = getReceiverName();
        String[] split = getNewAddress().split(":");

        Socket socket = Utils.getSocketToSend(routingTable, split[0], Integer.parseInt(split[1]), name);
        if (socket == null) {
            System.out.println("The given client is not reachable.");
            return;
        }

        try {
            System.out.println("Enter the message: ");
            message = keyboard.readLine();
            MessageWrapper mw = new MessageWrapper("0", split[0], Integer.parseInt(split[1]), name, myIP, myPort, myName, startTTL, System.currentTimeMillis() / 1000L, message);

            try {
                boolean open = Utils.isConnectionOpen(socket);
                System.out.println("Open in send: " + open);
                if (!open){
                    System.out.println(ANSI_BLUE_BACKGROUND + "Server no longer available" + ANSI_RESET);
                    boolean updated = Utils.removeCorrespondingEntries(getRoutingTable(), socket);
                    if (updated) Utils.propagateRoutingTable(getRoutingTable());
                } else {
                    System.out.println("Sending msg: " + message);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    System.out.println("Socket: " + socket);
                    out.println(new Gson().toJson(mw));
                }
            } catch (IOException e){
                System.out.println(ANSI_BLUE_BACKGROUND + "Server no longer available" + ANSI_RESET);
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println(ANSI_CYAN + "keyboard.readline(); failed." + ANSI_RESET);
            e.printStackTrace();
        }
    }

    private void connect() {
        String address = getNewAddress();
        String[] split = address.split(":");
        try {
            socket = new Socket(split[0], Integer.parseInt(split[1]));
            connections.add(new Connection(socket));
            sendRoutingInfo(socket);
        } catch (ConnectException e) {
            System.out.println("Given address could not be reached. Try again.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Wrong format.");
        }
        System.out.println(ANSI_CYAN + "ready for new Messages." + ANSI_RESET);
    }


    private void showRoutingTable() {
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(routingTable));
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(ANSI_BLACK + "Failed closing connection to server." + ANSI_RESET);
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public void getMyInfo() {
        try {
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                myIP = socket.getLocalAddress().getHostAddress();
            }
            //myIP = InetAddress.getLocalHost().getHostAddress();
            System.out.print("Enter your username: ");
            myName = keyboard.readLine();
            System.out.println(ANSI_CYAN + "Own IP: " + myIP + "\nOwn socket port: " + myPort + ANSI_RESET);
            RoutingEntry entry = new RoutingEntry(myIP, myPort, myName, 0, null);
            routingTable.add(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRoutingInfo(Socket socket) {
        try {
            String routingInfo = new Gson().toJson(routingTable);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(routingInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void whatsNext() {
        try {
            System.out.println("<1> Send message\n<2> establish new connection\n<3> show connections\n<4> quit");
            String choice = keyboard.readLine().trim();

            switch (choice) {
                case "1" -> sendMessage();
                case "2" -> connect();
                case "3" -> showRoutingTable();
                case "4" -> closeConnection();
                default -> System.out.println("Try again.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNewAddress(){
        System.out.print("<IP>:<PORT> : ");
        return scanner.nextLine().trim();
    }

    private String getReceiverName() {
        System.out.println("Who do you want to send the message to?");
        return scanner.nextLine().trim();
    }



}
