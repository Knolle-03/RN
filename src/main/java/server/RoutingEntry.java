package server;


import com.google.gson.Gson;
import lombok.Data;

import java.util.Objects;

@Data
public class RoutingEntry {

    private String ip;
    private int port;
    private String name;
    private int hopCount;
    private int outPort;

    public RoutingEntry(String ip, int port, String name, int hopCount, int outPort) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.hopCount = hopCount + 1;
        this.outPort = outPort;
    }


    public static RoutingEntry getRoutingEntryFromJSON(String JSON){
        Gson gson = new Gson();
        RoutingEntry newEntry = gson.fromJson(JSON, RoutingEntry.class);
        //this.setHopCount(this.getHopCount() + 1);
        System.out.println("Object in JSON constructor: " + newEntry);

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
