package server.routing;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

@NoArgsConstructor
public class RoutingTable {

    private Map<String, RoutingEntry> table = new HashMap<>();



    public RoutingTable(String IP, int port, String userName, List<Socket> neighbours) {
        this.addEntry(new RoutingEntry(IP, port, userName, -1, null), neighbours);
    }

    public RoutingTable(String IP, int port, String userName, Socket socket, List<Socket> neighbours) {
        this.addEntry(new RoutingEntry(IP, port, userName, 0, socket), neighbours);
    }

    public RoutingTable(Map<String, RoutingEntry> table) {
        this.table = table;
    }

    public String getJSONTable() {
        return new Gson().toJson(this);
    }


    // sync in case two or more routingWorkerThreads want to add the same entry simultaneously.
    public synchronized void addEntry(RoutingEntry entry, List<Socket> neighbours) {
        if (!table.containsKey(entry.getIp() + ":" + entry.getPort())) {
            System.out.println("Table does not contain this entry: " + entry);
            table.put(entry.getIp() + ":" + entry.getPort(), entry);
            System.out.println("added new Entry: " + entry.toString());
            System.out.println("New Table: " + this);
            propagateTable(neighbours);
            return;
        }

        System.out.println("Entry is already known: " + entry.toString());

    }




    // sync in case two or more routingWorkerThreads want to update simultaneously.
    public synchronized void update(RoutingTable table, List<Socket> neighbours) {
        // keep track if table is updated
        boolean updated = false;
        // number of updates
        int counter = 0;
        // IP of the neighbour who sent the routing table
        String neighbourIP = null;
        // Port of the neighbour who sent the routing table
        int neighbourPort = -42;
        // Socket of the neighbour who sent the routing table
        Socket neighbourSocket = null;

        // Find neighbour in it's own routing table
        for (RoutingEntry entry : table.getEntrySet()) {
            System.out.println("Entry in loop:" + entry);
            if (entry.getHopCount() <= 1) {
                // assign IP and port
                neighbourIP = entry.getIp();
                neighbourPort = entry.getPort();
                break;
            }
        }
        System.out.println("neighbour: " + neighbourIP + ":" + neighbourPort);


        // if one or both is not assigned the neighbour is not present in it's own routing table
        //if (neighbourIP == null || neighbourPort == -42) throw new RuntimeException("Count not find neighbour who sent the table in the table itself. " + neighbourIP + ":" + neighbourPort);
        System.out.println("Neighbours in update(): " + neighbours);
        // find sending neighbour's socket
        for (Socket socket : neighbours) {
            if (socket.getInetAddress().getHostAddress().equals(neighbourIP) && socket.getPort() == neighbourPort) {
                neighbourSocket = socket;

                break;
            }
        }

        //if (neighbourSocket == null) throw new RuntimeException("Received table from neighbour that is not connected anymore.");


        for (RoutingEntry entry : table.getEntrySet()) {

            // if entry is totally new
            if (!this.table.containsValue(entry)) {
                updated = true;
                entry.setHopCount(entry.getHopCount() + 1);
                entry.setSocket(neighbourSocket);
                this.table.put(entry.getIp() + ":" + entry.getPort(), entry);
                counter++;
                continue;
            }

            RoutingEntry ownEntry = this.table.get(entry.getIp() + ":" + entry.getPort());

            // change HopCount if lower
            if (entry.getHopCount() + 1 < ownEntry.getHopCount()) {
                ownEntry.setHopCount(entry.getHopCount() + 1);
                ownEntry.setSocket(neighbourSocket);
                counter++;
                updated = true;
            }

            if (ownEntry.getName().equals("")) ownEntry.setName(entry.getName());
        }

        if (updated) {
            System.out.println(ANSI_CYAN + "Server's routing table was updated " + counter + " time(s)." + ANSI_RESET);
            System.out.println(ANSI_CYAN + "Propagating new routing table to neighbours." + ANSI_RESET);
            propagateTable(neighbours);
        } else {
            System.out.println(ANSI_CYAN + "No routes could be improved with the given table.\nNot propagating own table to neighbours." + ANSI_RESET);
        }
    }

    public void propagateTable(List<Socket> neighbours) {
        PrintWriter printWriter;
        System.out.println("Propagating table: " + this);
        for (Socket socket : neighbours) {
            try {
                System.out.println("To: "  + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println(this.getJSONTable());
            } catch (IOException e) {
                System.out.println(ANSI_CYAN + "Could not send new table to: " + socket.getInetAddress() + ":" + socket.getPort() + ANSI_RESET);
            }
        }
    }

    public Set<RoutingEntry> getEntrySet() {
        return new HashSet<>(this.table.values());
    }


    public Map<String, RoutingEntry> getTable() {
        return table;
    }

    public void setTable(Map<String, RoutingEntry> table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "RoutingTable{" +
                "table=" + table +
                '}';
    }

    public int size() {
        return table.size();
    }

    public String prettyRoutingTableJSON () {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public void remove(Socket socket) {
        for (RoutingEntry entry : this.getEntrySet()) {
            if (entry.getSocket() == socket) table.remove(entry.getIp() + ":" + entry.getPort());
        }
    }
}
