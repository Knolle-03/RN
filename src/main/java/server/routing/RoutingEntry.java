package server.routing;


import com.google.gson.Gson;
import lombok.Data;

import java.net.Socket;
import java.util.Objects;

import static server.utils.ThreadColors.ANSI_CYAN;
import static server.utils.ThreadColors.ANSI_RESET;

@Data
public class RoutingEntry {

    private String ip;
    private int port;
    private String name;
    private int hopCount;
    private transient Socket socket;

    public RoutingEntry(String ip, int port, String name, int hopCount, Socket socket) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.hopCount = hopCount;
        this.socket = socket;
    }

    public static RoutingEntry getRoutingEntryFromJSON(String JSON){
        Gson gson = new Gson();
        RoutingEntry newEntry = gson.fromJson(JSON, RoutingEntry.class);
        //this.setHopCount(this.getHopCount() + 1);
        System.out.println(ANSI_CYAN + "Object in JSON constructor: " + newEntry + ANSI_RESET);

        return newEntry;

    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutingEntry)) return false;
        RoutingEntry entry = (RoutingEntry) o;
        return port == entry.port &&
                ip.equals(entry.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
