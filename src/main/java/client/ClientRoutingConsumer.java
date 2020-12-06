package client;

import server.routing.RoutingTable;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

public class ClientRoutingConsumer extends Thread {

    private RoutingTable ownRoutingTable;
    private RoutingTable newRoutingInfo;
    private List<Socket> directNeighbours;

    public ClientRoutingConsumer(Client client, RoutingTable newRoutingInfo ) {
        this.ownRoutingTable = client.getMyRoutingTable();
        this.newRoutingInfo = newRoutingInfo;
    }

    @Override
    public void run() {
            System.out.println(ANSI_CYAN + "Updating table with Info: " + newRoutingInfo + "\nCurrent neighbours: " + directNeighbours + ANSI_RESET);
            ownRoutingTable.update(newRoutingInfo, directNeighbours);
    }


}
