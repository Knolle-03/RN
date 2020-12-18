package message;

import client.Client;
import com.google.gson.Gson;
import utils.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import static utils.Utils.ThreadColors.*;

// Handles incoming messages
public class MessageConsumer extends Thread {

    private final MessageWrapper mw;
    private final Client client;

    public MessageConsumer(MessageWrapper mw, Client client) {
        this.mw = mw;
        this.client = client;
    }

    @Override
    public void run() {
        // if for me
        if (msgForMe()) {
            // and not a confirmation
            if (mw.getAnswerFlag().equals("0")) {
                // print message
                System.out.println("Message from " + mw.getFromName() + ": " + mw.getMessage());
                System.out.println("Sending took: " + (System.currentTimeMillis() / 1000L - mw.getTimestamp()));

                // and send confirmation
                MessageWrapper confirmation = new MessageWrapper("1", mw.getFromIP(), mw.getFromPort(), mw.getFromName(), mw.getToIP(), mw.getToPort(), mw.getToName(), 20, System.currentTimeMillis() / 1000L, "");
                Socket socket = Utils.getSocketToSend(client.getRoutingTable(), mw.getFromIP(), mw.getFromPort(), mw.getFromName());
                if (socket == null) {
                    System.out.println(ANSI_BLUE_BACKGROUND + "Could not send confirmation. Sending client not found." + ANSI_RESET);
                    return;
                }
                PrintWriter pr;
                try {
                    System.out.println("Sending confirmation: "+ confirmation);
                    pr = new PrintWriter(socket.getOutputStream(), true);
                    pr.println(new Gson().toJson(confirmation));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            // and is a confirmation
            } else {
                // print confirmation message
                System.out.println(mw.getFromName() + " received your message.");

            }
        // if not for me
        } else {
            // and ttl <= 0
            if (mw.getTtl() <= 0) {
                // discard message
                System.out.println("TTL is 0\nDiscarding Message: " + mw);
            // and ttl > 0
            } else {
                // forward message
                Socket socket = Utils.getSocketToSend(client.getRoutingTable(), mw.getToIP(), mw.getToPort(), mw.getToName());

                try {
                    if (socket == null) throw new SocketException("No connection to given receiver.\nDiscarding message: " + mw);
                    boolean open = Utils.isConnectionOpen(socket);
                    if (!open) {
                        System.out.println(ANSI_BLUE_BACKGROUND + "Server no longer available" + ANSI_RESET);
                        boolean updated = Utils.removeCorrespondingEntries(client.getRoutingTable(), socket);
                        if (updated) Utils.propagateRoutingTable(client.getRoutingTable());
                    } else {
                        try {
                            mw.setTtl(mw.getTtl() - 1);
                            PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
                            pr.println(new Gson().toJson(mw));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    // check if ip port and name equals client info
    private boolean msgForMe() {
        return mw.getToIP().equals(client.getMyIP()) && mw.getToPort() == client.getMyPort() && mw.getToName().equals(client.getMyName());
    }

}
