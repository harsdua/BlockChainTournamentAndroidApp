package com.example.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;

public class NodeClient {

    private static String nearestNode = "192.168.1.37"; //"192.168.1.37";

    public static Serializable send(Serializable obj) throws IOException {
        try {
            Socket socket = new Socket(nearestNode, 8088);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            out.writeObject(obj);
            out.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Serializable response = (Serializable) ois.readObject();
            socket.close();

            return response;

        } catch (ClassNotFoundException | EOFException e) {
            System.out.println("Broken");
            return "";
        }
    }
}
