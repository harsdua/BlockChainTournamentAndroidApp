package main.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

public abstract class Server {

    private final ServerSocket serverSocket;
    protected int port;

    public Server(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    public void listen() throws Exception {

        Socket clientSocket = serverSocket.accept();
        System.out.println(" [ Server ]          Socket accepts connection from " + clientSocket.getInetAddress().getHostAddress());

        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

        Serializable obj = (Serializable) ois.readObject();
        this.receptionHandler(obj, clientSocket);

        ois.close();
        clientSocket.close();
    }


    public boolean sendToClient(Serializable obj, Socket clientSocket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            out.writeObject(obj);
            out.flush();

            clientSocket.close();

            return true;
        }

        catch (UnknownHostException | ConnectException e) {
            return false;
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean sendToClient(Serializable obj, String clientIP, int clientPort) {
        try {
            Socket clientSocket = new Socket(clientIP, clientPort);
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            out.writeObject(obj);
            out.flush();

            clientSocket.close();

            return true;
        }

        catch (UnknownHostException | ConnectException e) {
            return false;
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * sends obj to each client IP in ipAddressList
     * @throws IOException if client not found
     */
    protected abstract void broadcast(Serializable packet);

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public int getPort() {
        return port;
    }

    public String getIP() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    protected abstract void receptionHandler(Serializable obj, Socket clientSocket);

}


