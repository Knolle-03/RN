package main;

import client.Client;

public class Main2 {
    public static void main(String[] args) {
        Client client = new Client(2000);
        client.start();
    }
}
