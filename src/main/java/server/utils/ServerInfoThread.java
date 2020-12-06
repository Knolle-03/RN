package server.utils;


import server.routing.RoutingTable;

import java.net.Socket;
import java.util.List;

public class ServerInfoThread extends Thread {

    private List<Socket> neighbours;
    private RoutingTable routingTable;

    public ServerInfoThread(List<Socket> neighbours, RoutingTable routingTable) {
        this.neighbours = neighbours;
        this.routingTable = routingTable;
    }

    @Override
    public void run() {
        while (true){
            try {
                System.out.println("===========================================");
                System.out.println("open socket count: " + neighbours.size());
                System.out.println("Socket: " + neighbours);
                System.out.println("Routing table size: " + routingTable.size());
                System.out.println("Routing table: " + routingTable.prettyRoutingTableJSON());
                System.out.println("============================================");

                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                System.err.println("Server Info Thread was interrupted!");
                e.printStackTrace();
            }
        }

    }
}
