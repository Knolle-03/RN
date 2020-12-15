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

// for getters and setter
@Data
public class Client {
    // own routing info
    private String myIP;
    private int myPort;
    private String myName;
    // table with all available routing info
    private Set<RoutingEntry> routingTable = new HashSet<>();
    // storage for incoming JSOnm strings
    private LinkedBlockingQueue<String> incomingJSON = new LinkedBlockingQueue<>();
    // storage for all open connections
    private LinkedBlockingQueue<Connection> connections = new LinkedBlockingQueue<>();
    // listening serverSocket
    private ServerSocket serverSocket;
    // reader for client input
    private BufferedReader keyboard;
    // writer for output
    private PrintWriter out;
    // time to live for outgoing messages
    private int startTTL = 20;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    // second reader for client input (whoops)
    private Scanner scanner = new Scanner(System.in);


    public Client(int myServerPort) {
        myPort = myServerPort;
    }

    // start client
    public void start() {
        init();
        listen(myPort);
        getMyInfo();
        // can actually be completed without an exception (closeConnection())
        while (true) whatsNext();
    }

    // start thread to listen for new connections
    public void listen(int myServerPort) {
        try {
            serverSocket = new ServerSocket(myServerPort);
            threadPool.execute(new ConnectionListener(connections, serverSocket));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // start message Producer and client input
    private void init() {
        threadPool.execute(new JSONProducer(this));
        keyboard = new BufferedReader(new InputStreamReader(System.in));
    }

    // send message to client
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
            String message = keyboard.readLine();
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
            System.out.println(ANSI_BLUE_BACKGROUND + "keyboard.readline(); failed." + ANSI_RESET);
            e.printStackTrace();
        }
    }


    // establish new connection
    private void connect() {
        String address = getNewAddress();
        String[] split = address.split(":");
        try {
            Socket socket = new Socket(split[0], Integer.parseInt(split[1]));
            connections.add(new Connection(socket));
            sendRoutingInfo(socket);
        } catch (ConnectException e) {
            System.out.println(ANSI_BLUE_BACKGROUND + "Given address could not be reached. Try again." + ANSI_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(ANSI_BLUE_BACKGROUND + "Wrong format." + ANSI_RESET);
        }
        System.out.println(ANSI_BLUE_BACKGROUND + "ready for new Messages." + ANSI_RESET);
    }

    // print "pretty" routing table
    private void showRoutingTable() {
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(routingTable));
    }

    // close own connection
    public void closeConnection() {
        try {
            for (Connection connection : connections) {
                connection.getSocket().close();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(ANSI_BLUE_BACKGROUND + "Failed closing connections." + ANSI_RESET);
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    // set own info
    public void getMyInfo() {
        try {
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                myIP = socket.getLocalAddress().getHostAddress();
            }
            //myIP = InetAddress.getLocalHost().getHostAddress();
            System.out.print("Enter your username: ");
            myName = keyboard.readLine();
            System.out.println(ANSI_BLUE + "Own IP: " + myIP + "\nOwn socket port: " + myPort + ANSI_RESET);
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

    // looping menu
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
