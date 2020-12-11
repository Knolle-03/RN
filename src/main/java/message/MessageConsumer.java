package message;

import client.Client;
import com.google.gson.Gson;
import utils.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import static utils.Utils.ThreadColors.*;

public class MessageConsumer extends Thread {

    private MessageWrapper mw;
    private Client client;

    public MessageConsumer(MessageWrapper mw, Client client) {
        this.mw = mw;
        this.client = client;
    }

    @Override
    public void run() {
        if (msgForMe()) {
            if (mw.getAnswerFlag() == 0) {
                System.out.println("Message from: " + mw.getFromName() + ": " + mw.getMessage());
                System.out.println("Sending took: " + (System.currentTimeMillis() / 1000L - mw.getTimestamp()));
                MessageWrapper confirmation = new MessageWrapper(1, mw.getFromIP(), mw.getFromPort(), mw.getFromName(), mw.getToIP(), mw.getToPort(), mw.getToName(), 20, System.currentTimeMillis() / 1000L, "");
                Socket socket = Utils.getSocketToSend(client.getRoutingTable(), mw.getFromIP(), mw.getFromPort(), mw.getFromName());
                if (socket == null) {
                    System.out.println(ANSI_BLUE_BACKGROUND + "Could not send confirmation. Sending client not found." + ANSI_RESET);
                    return;
                }

                try {
                    PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
                    pr.println(new Gson().toJson(confirmation));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println(mw.getFromName() + " received your message.");

            }
        } else {
            if (mw.getTtl() == 0) {
                System.out.println("TTL is 0\nDiscarding Message: " + mw);
            }
            Socket socket = Utils.getSocketToSend(client.getRoutingTable(), mw.getToIP(), mw.getToPort(), mw.getToName());
            try {
                if (socket == null) throw new SocketException("No connection to given receiver.\nDiscarding message: " + mw);
                mw.setTtl(mw.getTtl() - 1);
                PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
                pr.println(new Gson().toJson(mw));
                //pr.println(mw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private boolean msgForMe() {
        return mw.getToIP().equals(client.getMyIP()) && mw.getToPort() == client.getMyPort() && mw.getToName().equals(client.getMyName());
    }

}
