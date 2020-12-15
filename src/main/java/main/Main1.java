package main;

import client.Client;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main1 {

    public static void main(String[] args) {
//        int num = 4;
//        ExecutorService clients = Executors.newFixedThreadPool(num);
//        for (int i = 0; i < num; i++) {
//            clients.execute(new Client(5000 + i));
//        }


        Client client = new Client(1000);
        client.start();
    }

}
