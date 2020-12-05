package server;

import com.google.gson.Gson;
import server.routing.RoutingEntry;
import server.routing.RoutingTable;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageConsumer extends Thread {

    LinkedBlockingQueue<MessageWrapper> messageWrappers;
    List<Socket> directNeighbours;
    RoutingTable routingTable;
    MessageWrapper messageWrapper;
    PrintWriter printWriter;

    public MessageConsumer(LinkedBlockingQueue<MessageWrapper> messageWrappers, RoutingTable routingTable, List<Socket> directNeighbours) {
        this.messageWrappers = messageWrappers;
        this.directNeighbours = directNeighbours;
        this.routingTable = routingTable;
        //System.out.println("init: " + this.getClass());
    }

    @Override
    public void run() {
        while (true) {
            try {
                messageWrapper = messageWrappers.take();
                System.out.println("Took message from messageWrapper queue. messageWrapperQueue size: " + messageWrappers.size());
            } catch (InterruptedException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
            String receiverIP = messageWrapper.getToIP();
            int receiverPort = messageWrapper.getToPort();
            int outPort = -42;

            System.out.println("Want to send msg to: " + receiverIP + ":" + receiverPort);

            for (RoutingEntry entry : routingTable.getEntrySet()) {
                if (receiverIP.equals(entry.getIp()) && receiverPort == entry.getPort()){
                    outPort = entry.getPort();
                    System.out.println("Found corresponding outPort: " + outPort);
                }
            }

            for (Socket socket : directNeighbours) {
                if (socket.getPort() == outPort) {
                    System.out.println("Found socket of outPort.");
                    try {
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println(new Gson().toJson(messageWrapper));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }




        }
    }
}
