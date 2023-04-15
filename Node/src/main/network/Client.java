package main.network;

import java.io.*;
import java.net.*;
import java.io.Serializable;

public class Client {

    /**
     * Sends object to destination IP
     *
     * @param obj Object to be sent
     * @param destinationIP IP address of destination
     */
    public void send(Serializable obj, String destinationIP, int destinationPort) throws IOException {
        try {
            Socket socket = new Socket(destinationIP, destinationPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(obj);
            out.flush();

            socket.close();
        } catch(Exception e) {
            System.out.println(" [ Client ]          unreachable host, " + destinationIP + " : " + destinationPort + " might be down");
        }
    }

}
