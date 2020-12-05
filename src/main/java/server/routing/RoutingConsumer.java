package server.routing;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.SocketHandler;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

public class RoutingConsumer extends Thread{

    private RoutingTable ownRoutingTable;
    private RoutingTable newRoutingInfo;
    private LinkedBlockingQueue<RoutingTable> routingTables;
    private List<Socket> directNeighbours;

    public RoutingConsumer(RoutingTable ownRoutingTable, LinkedBlockingQueue<RoutingTable> routingTables, List<Socket> directNeighbours) {
        this.ownRoutingTable = ownRoutingTable;
        this.routingTables = routingTables;
        this.directNeighbours = directNeighbours;
        //System.out.println("init: " + this.getClass());
    }

    @Override
    public void run() {
        while (true) {
            try {
                newRoutingInfo = routingTables.take();
                System.out.println(ANSI_CYAN + "Took routing table from queue. routingTablesQueue size: " + routingTables.size() + ANSI_RESET);
            } catch (InterruptedException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
            ownRoutingTable.update(newRoutingInfo, directNeighbours);
        }
    }
}
