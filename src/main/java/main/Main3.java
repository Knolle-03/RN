package main;

import client.Client;

public class Main3 {
    public static void main(String[] args) {
        Client client = new Client(6002);
        client.start();
    }
}
