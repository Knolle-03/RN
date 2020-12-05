package client;

import com.google.gson.Gson;
import server.MessageWrapper;
import server.routing.RoutingEntry;
import server.routing.RoutingTable;

import static server.utils.ThreadColors.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private String myIP;
    private int myPort;
    private String myUsername;
    private RoutingTable myRoutingTable;
    private Socket socket;
    private BufferedReader keyboard;
    private PrintWriter out;
    private String message;
    private String receiverIP;
    private String receiverName;
    private int receiverPort;
    private int startTTL = 20;
    private Gson gson = new Gson();
    private ExecutorService service;



    public Client(String SERVER_IP, int PORT) {
        connect(SERVER_IP, PORT);
    }


    private void connect(String SERVER_IP, int port) {
        try {
            socket = new Socket(SERVER_IP, port);
            service = Executors.newFixedThreadPool(1);
            service.execute(new ClientMessageConsumer(socket));
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getMyInfo();
        sendInitialRoutingInfo();
        System.out.println(ANSI_CYAN + "ready for new Messages." + ANSI_RESET);
        getReceiverInfo();
    }

    private void messaging() {
        System.out.println(ANSI_CYAN + "What message do your want to send to: " + receiverName + "(" + receiverIP  + ":" + receiverPort + ") ?" + ANSI_RESET);
        System.out.print("> ");
        try {
            message = keyboard.readLine();
        } catch (IOException e) {
            System.out.println(ANSI_CYAN + "keyboard.readline(); failed." + ANSI_RESET);
            e.printStackTrace();
        }
        if (message.equals("quit")) {
            closeConnection();
            return;
        }

        out.println(buildJSONMessage(message));

        getReceiverInfo();
    }

    private String buildJSONMessage(String message) {
        MessageWrapper messageWrapper = new MessageWrapper(0, receiverIP, receiverPort, receiverName, myIP, myPort, myUsername, startTTL, new Timestamp(System.currentTimeMillis()), message);
        System.out.println(messageWrapper);
        return gson.toJson(messageWrapper);
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
            String hName = InetAddress.getLocalHost().getHostName();
            InetAddress addrs[] = InetAddress.getAllByName(hName);
            for (int i = 0; i < addrs.length; i++) {
                if (addrs[i].getHostAddress().contains("10.8.0.")) {
                    myIP = addrs[i].getHostAddress();
                    break;
                }
            }
            myPort = socket.getLocalPort();
            System.out.print("Enter your username: ");
            myUsername = keyboard.readLine();
            System.out.println(ANSI_CYAN + "Own IP: " + myIP + "\nOwn socket port: " + myPort + ANSI_RESET);
            RoutingEntry entry = new RoutingEntry(myIP, myPort, myUsername, -1, -1);
            HashMap<String, RoutingEntry> table = new HashMap<>();
            table.put(myIP + ":" + myPort, entry);
            myRoutingTable = new RoutingTable(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInitialRoutingInfo() {
        String routingInfo = myRoutingTable.getJSONTable();
        System.out.println(ANSI_CYAN + "initial routing info from client: " + routingInfo + ANSI_RESET);
        out.println(routingInfo);
    }

    public void getReceiverInfo()  {
        try {
            System.out.println(ANSI_CYAN + "What is the receiver's username?" + ANSI_RESET);
            receiverName = keyboard.readLine();
            System.out.println(ANSI_CYAN + "What is the receiver's IP and Port? Format: <IP>:<PORT>" + ANSI_RESET);
            String receiverDetails = keyboard.readLine();
            String[] split = receiverDetails.split(":");
            receiverIP = split[0];
            receiverPort = Integer.parseInt(split[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }
        messaging();
    }


    public static void main(String[] args) {
        Client client = new Client("10.8.0.2", 5001);
    }



}
