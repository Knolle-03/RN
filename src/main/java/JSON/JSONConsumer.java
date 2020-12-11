package JSON;

import client.Client;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import message.MessageConsumer;
import message.MessageWrapper;
import routing.RoutingConsumer;
import routing.RoutingEntry;

import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;


public class JSONConsumer extends Thread {

    private Client client;
    private LinkedBlockingQueue<String> incomingJSON;
    private LinkedBlockingQueue<MessageWrapper> messages;
    private LinkedBlockingQueue<Set<RoutingEntry>> routingTables;
    private ExecutorService threadPool;
    private Socket socket;

    public JSONConsumer(Client client, Socket socket) {
        this.client = client;
        this.incomingJSON = client.getIncomingJSON();
        this.messages = client.getMessages();
        this.routingTables = client.getNewRoutingInfos();
        this.threadPool = client.getThreadPool();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            String JSON = incomingJSON.take();
            if (JSON.contains("hopCount")) {
                Set<RoutingEntry> newRoutingTable = new Gson().fromJson(JSON, new TypeToken<Set<RoutingEntry>>(){}.getType());
                new RoutingConsumer(client.getRoutingTable(), newRoutingTable, socket).start();
            } else if (JSON.contains("answerFlag")) {
                System.out.println(JSON);
                MessageWrapper mw = new Gson().fromJson(JSON, MessageWrapper.class);
                threadPool.execute(new MessageConsumer(mw, client));
            } else {
                System.out.println("Ill-formed JSON.\nDiscarding: " + JSON);
            }
        } catch (InterruptedException e) {
            System.out.println("JSON consumer was interrupted.");
            e.printStackTrace();
        }

    }
}
