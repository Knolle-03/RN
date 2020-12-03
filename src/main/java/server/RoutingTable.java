package server;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingTable {

    private Map<String, RoutingEntry> table = new HashMap<>();
    private Gson gson = new Gson();

    public List<String> getJSONTable() {
        List<String> JSONEntries = new ArrayList<>();
        for (RoutingEntry entry : table.values()){
            JSONEntries.add(gson.toJson(entry));
        }

        return JSONEntries;
    }

    public void addEntry(Map.Entry<String, RoutingEntry> entrySet) {
        table.putIfAbsent(entrySet.getKey(), entrySet.getValue());
    }

    public void updateTable(List<RoutingEntry> entries) {
        boolean updated = false;
        for(RoutingEntry entry : entries) {
            if (!table.containsValue(entry)){
                updated = true;
                table.put(entry.getName(), entry);
                continue;
            }
            RoutingEntry inTable = table.get(entry.getName());

            // change HopCount if lower
            if (entry.getHopCount() + 1 < inTable.getHopCount()){
                inTable.setHopCount(entry.getHopCount() + 1);
                updated = true;
            }
            // change outPort if new
            if (entry.getOutPort() != inTable.getOutPort()) {
                inTable.setOutPort(entry.getOutPort());
                updated = true;
            }

        }

        if (updated) propagateTable();

    }

    private void propagateTable() {
        for(RoutingEntry entry : table.values()) {
            if (entry.getHopCount() == 1) {
                Server.sendTable(entry.getIp(), entry.getPort());
            }
        }
    }



    public Map<String, RoutingEntry> getTable() {
        return table;
    }

    public void setTable(Map<String, RoutingEntry> table) {
        this.table = table;
    }


    public static void main(String[] args) {
        RoutingTable routingTable = new RoutingTable();
        RoutingEntry entry1 = new RoutingEntry("127.0.0.1", 9990, "testUser1", 1, 9991);
        RoutingEntry entry2 = new RoutingEntry("127.0.0.1", 9989, "testUser2", 2, 9992);


        routingTable.addEntry(Map.entry("testUser2", entry2));
        routingTable.addEntry(Map.entry("testUser", entry1));

        System.out.println(routingTable.table.size());
        System.out.println(routingTable.getJSONTable());
    }
}
