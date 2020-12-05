package server.utils;

import com.google.gson.Gson;
import server.MessageWrapper;
import server.routing.RoutingTable;

import java.lang.reflect.Type;
import java.util.concurrent.LinkedBlockingQueue;
import static server.utils.ThreadColors.*;


public class JSONConsumer extends Thread{

    private LinkedBlockingQueue<String> unformattedMessages;
    private String currentMsg;
    private final LinkedBlockingQueue<MessageWrapper> messageWrappers;
    private final LinkedBlockingQueue<RoutingTable> routingTables;
    private final Gson gson = new Gson();

    public JSONConsumer(LinkedBlockingQueue<String> unformattedMessages, LinkedBlockingQueue<MessageWrapper> messageWrappers, LinkedBlockingQueue<RoutingTable> routingTables) {
        this.unformattedMessages = unformattedMessages;
        this.messageWrappers = messageWrappers;
        this.routingTables = routingTables;
        //System.out.println("init: " + this.getClass());

    }

    @Override
    public void run() {
        while (true) {
            try {
                currentMsg = unformattedMessages.take();
            } catch (InterruptedException e) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
            // TODO rewrite validation!
            if (currentMsg.contains("answerFlag")) {
                MessageWrapper messageWrapper = gson.fromJson(currentMsg, MessageWrapper.class);
                messageWrappers.add(messageWrapper);
                System.out.println(ANSI_RED + "Added new message. messageWrapperQueue size: " + messageWrappers.size() + ANSI_RESET);
            }else if (currentMsg.contains("hopCount")) {
                RoutingTable table = gson.fromJson(currentMsg, RoutingTable.class);

                routingTables.add(table);
                System.out.println(ANSI_RED + "Added new routing table. RoutingTableQueue size: " + routingTables.size() + ANSI_RESET);
            } else {
                System.out.println(ANSI_RED + "discarded ill-formatted JSON: '" + currentMsg + "' | unformattedMessages size: " + unformattedMessages.size() + ANSI_RESET);
            }
        }


    }


}
