package org.eclise.keyple.example.remote.client;

import org.eclise.keyple.example.remote.server.InitServer;

public class Demo_ClientServer {


    public static void main(String[] args) throws Exception{


        InitServer server = new InitServer();
        server.boot();

        InitClient client = new InitClient();
        client.boot();
        client.demo();

        server.status();


    }
}
