package client;

import server.routing.RoutingTable;


public class ClientRoutingInfoThread extends Thread {

    private RoutingTable routingTable;

    public ClientRoutingInfoThread(RoutingTable routingTable) {

        this.routingTable = routingTable;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(30 * 1000);
                System.out.println("===========================================");
                System.out.println("Routing table size: " + routingTable.size());
                System.out.println("Routing table: " + routingTable.prettyRoutingTableJSON());
                System.out.println("============================================");


            } catch (InterruptedException e) {
                System.err.println("Server Info Thread was interrupted!");
                e.printStackTrace();
            }
        }

    }

}
