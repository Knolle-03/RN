package server.routing;


import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.*;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;


public class RoutingTable {

    private Map<String, RoutingEntry> table = new HashMap<>();

    public RoutingTable(String IP, int port, String userName) {
        this.addEntry(Map.entry((IP + ":" + port), new RoutingEntry(IP, port, userName, -1, -1)));
    }

    public RoutingTable(String IP, int port, String userName, int outPort) {
        this.addEntry(Map.entry((IP + ":" + port), new RoutingEntry(IP, port, userName, -1, outPort)));
    }

    public RoutingTable(Map<String, RoutingEntry> table) {
        this.table = table;
    }

    public String getJSONTable() {
        String json = new Gson().toJson(this);
        System.out.println(json);
        return json;
    }

    // sync in case two or more routingWorkerThreads want to add the same entry simultaneously.
    public synchronized void addEntry(Map.Entry<String, RoutingEntry> entrySet) {
        table.putIfAbsent(entrySet.getKey(), entrySet.getValue());
    }




    // sync in case two or more routingWorkerThreads want to update simultaneously.
    public synchronized void update(RoutingTable table, List<Socket> neighbours) {
        boolean updated = false;
        int counter = 0;
        String neighbourIP = null;
        int neighbourPort = -42;
        Socket neighbourSocket = null;

        for (RoutingEntry entry : table.getEntrySet()) {
            if (entry.getHopCount() == 0) {
                neighbourIP = entry.getIp();
                neighbourPort = entry.getPort();
                break;
            }


        }
        if (neighbourIP == null || neighbourPort == -42) throw new RuntimeException("Count not find neighbour who sent the table in the table itself.");
        for (Socket socket : neighbours) {
            if (socket.getInetAddress().getHostAddress().equals(neighbourIP) && socket.getPort() == neighbourPort) {
                neighbourSocket = socket;
                break;
            }
        }

        if (neighbourSocket == null) throw new RuntimeException("Received table from neighbour that is not connected anymore.");


        for (RoutingEntry entry : table.getEntrySet()) {

            // if entry is totally new
            if (!this.table.containsValue(entry)) {
                updated = true;
                entry.setHopCount(entry.getHopCount() + 1);
                entry.setOutPort(neighbourPort);
                this.table.put(entry.getName(), entry);
                counter++;
                continue;
            }

            RoutingEntry ownEntry = this.table.get(entry.getIp() + ":" + entry.getPort());

            // change HopCount if lower
            if (entry.getHopCount() + 1 < ownEntry.getHopCount()) {
                ownEntry.setHopCount(entry.getHopCount() + 1);
                ownEntry.setOutPort(neighbourSocket.getPort());
                counter++;
                updated = true;
            }
        }

        if (updated) {
            System.out.println(ANSI_CYAN + "Server's routing table was updated " + counter + " time(s)." + ANSI_RESET);
            System.out.println(ANSI_CYAN + "Propagating new routing table to neighbours." + ANSI_RESET);
            propagateTable(neighbours);
        } else {
            System.out.println(ANSI_CYAN + "No routes could be improved with the given table.\nNot propagating own table to neighbours." + ANSI_RESET);
        }
    }

    private void propagateTable(List<Socket> neighbours) {
        PrintWriter printWriter;
        for (Socket socket : neighbours) {
            try {
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
}
