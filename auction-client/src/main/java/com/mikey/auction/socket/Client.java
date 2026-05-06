package com.mikey.auction.socket;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static Client instance;
    private static Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Client() {
        try {
            this.socket = new Socket("localhost", 12345);
            this.in = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
    } catch (Exception e) {
            e.printStackTrace();
    }
}
    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void sendMessage(String message) {
        out.println(message);
    }
    public void re
}
