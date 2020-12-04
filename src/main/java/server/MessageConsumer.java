package server;

import server.routing.RoutingTable;
import server.utils.Validator;

import java.net.Socket;
import java.util.List;
import java.util.Queue;

public class MessageConsumer extends Thread {

    Queue<String> messages;
    RoutingTable routingTable;
    List<Socket> directNeighbours;

    public MessageConsumer(Queue<String> messages, RoutingTable routingTable, List<Socket> directNeighbours) {
        this.messages = messages;
        this.routingTable = routingTable;
        this.directNeighbours = directNeighbours;
    }

    @Override
    public void run() {
        String message = messages.poll();
        Validator.validate(message);
    }
}
