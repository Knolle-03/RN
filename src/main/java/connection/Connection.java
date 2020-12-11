package connection;


import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
@Data
public class Connection {

    private Socket socket;
    private BufferedReader reader;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unable to initialize buffered reader for connection socket: " + socket);
            e.printStackTrace();
        }
    }
}
