package server;

import com.google.gson.Gson;
import server.routing.RoutingEntry;
import server.routing.RoutingTable;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

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
                System.out.println(ANSI_CYAN + "Took message from messageWrapper queue. messageWrapperQueue size: " + messageWrappers.size()+ ANSI_RESET);
            } catch (InterruptedException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
            String receiverIP = messageWrapper.getToIP();
            int receiverPort = messageWrapper.getToPort();
            int outPort = -42;

            System.out.println(ANSI_CYAN + "Want to send msg to: " + receiverIP + ":" + receiverPort + ANSI_RESET);

            for (RoutingEntry entry : routingTable.getEntrySet()) {
                if (receiverIP.equals(entry.getIp()) && receiverPort == entry.getPort()){
                    outPort = entry.getPort();
                    System.out.println(ANSI_CYAN + "Found corresponding outPort: " + outPort + ANSI_RESET);
                }
            }

            for (Socket socket : directNeighbours) {
                if (socket.getPort() == outPort) {
                    System.out.println(ANSI_CYAN + "Found socket of outPort." + ANSI_RESET);
                    try {
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println(new Gson().toJson(messageWrapper));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            System.out.println("Unknown address. Message was discarded.");

        }
    }
}
