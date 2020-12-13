package routing;

import utils.Utils;

import java.net.Socket;
import java.util.Set;

import static utils.Utils.ThreadColors.*;

public class RoutingConsumer extends Thread {

    private final Set<RoutingEntry> ownRoutingTable;
    private final Set<RoutingEntry> newRoutingInfo;
    private final Socket neighbourSocket;

    public RoutingConsumer(Set<RoutingEntry> ownRoutingTable, Set<RoutingEntry> newRoutingInfo, Socket neighbourSocket) {
        this.ownRoutingTable = ownRoutingTable;
        this.newRoutingInfo = newRoutingInfo;
        this.neighbourSocket = neighbourSocket;
        if (this.neighbourSocket == null) throw new RuntimeException(ANSI_BLACK_BACKGROUND + "Could not find sender in its own table" + ANSI_RESET);
    }

    @Override
    public void run() {
        boolean updated = Utils.updateRoutingInfo(ownRoutingTable, newRoutingInfo, neighbourSocket);
        if (updated) Utils.propagateRoutingTable(ownRoutingTable);
    }



}



