package utils;

import com.google.gson.Gson;
import routing.RoutingEntry;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Utils {

    public static boolean updateRoutingInfo(Set<RoutingEntry> myRoutingInfo, Set<RoutingEntry> newRoutingInfo, Socket socket) {
        // bool to see if own routing info propagation is necessary.
        boolean updated = false;

        // Sub set of connections reachable trough sending neighbour
        Set<RoutingEntry> subSet = new HashSet<>();
        for (RoutingEntry entry : myRoutingInfo) {
            if (entry.getSocket() == socket) {
                subSet.add(entry);
            }
        }

        for (RoutingEntry entry : newRoutingInfo) {
            // add new entries
            if (!myRoutingInfo.contains(entry)) {
                entry.setSocket(socket);
                entry.setHopCount(entry.getHopCount() + 1);
                myRoutingInfo.add(entry);
                updated = true;
            }
            // update if better
            else {
                Iterator<RoutingEntry> it = myRoutingInfo.iterator();
                RoutingEntry own = it.next();
                while (it.hasNext()) {
                    if (own.equals(entry) && own.getHopCount() > entry.getHopCount() + 1) {
                        own.setHopCount(entry.getHopCount() + 1);
                        updated = true;
                    }
                    own = it.next();
                }
            }
        }

        // delete outdated entries
        for (RoutingEntry entry : subSet) {
            if (!newRoutingInfo.contains(entry)) {
                myRoutingInfo.remove(entry);
                updated = true;
            }
        }
        return updated;
    }


    public static Socket getSocketToSend(Set<RoutingEntry> routingTable, String ip, int port, String name) {
        RoutingEntry best = null;
        Iterator<RoutingEntry> iterator = routingTable.iterator();
        // search for the first possible connection
        do {
            RoutingEntry entry = iterator.next();
            if (entry.getIp().equals(ip) && entry.getPort() == port && entry.getName().equals(name)) best = entry;
        } while (best == null && iterator.hasNext());

        // if there is non return null
        if (best == null) return null;

        // if there is one, compare the hop count to other entries
        for (RoutingEntry entry : routingTable) {
            if (entry.getIp().equals(ip) && entry.getPort() == port && entry.getName().equals(name) && best.getHopCount() > entry.getHopCount()) best = entry;
        }
        return best.getSocket();
    }

    public static void propagateRoutingTable(Set<RoutingEntry> routingTable) {
        Set<Socket> neighbours = new HashSet<>();

        // look through routing table
        for (RoutingEntry entry : routingTable) {
            // if neighbour entry is a neighbour add to set
            if (entry.getHopCount() == 1) {
                neighbours.add(entry.getSocket());
            }
        }
        PrintWriter pr;
        String JSON;
        for (Socket socket : neighbours) {
            try {
                pr = new PrintWriter(socket.getOutputStream(), true);
                JSON = new Gson().toJson(routingTable);
                pr.println(JSON);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ThreadColors {

        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";

        public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
        public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
        public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
        public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
        public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
        public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
        public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
        public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    }
}
