package main.node;

import main.ledger.Match;
import main.network.Address;

import java.io.Serializable;
import java.net.Socket;


/**
 * Stateless static class used to contain the master node handler related methods
 */
public class MasterNodeHandler extends NodeHandler {

    /* ============================================================================================================== */
    /*                                          packet identifiers                                                    */
    /* ============================================================================================================== */


    protected static boolean isIPListRequest(Serializable packet) {
        return packet instanceof String && ((String) packet).contains("Requesting IP-List##");
    }

    protected static boolean isLiveNotification(Serializable packet) {
        return packet instanceof String && ((String) packet).contains("IAmAlive");
    }


    /* ============================================================================================================== */
    /*                                          packet handlers                                                    */
    /* ============================================================================================================== */


    protected static void handleIPListRequest(MasterNode node, Serializable packet, Socket clientSocket) {
        int port = NodeHandler.extractPortFromMessage(packet);
        String ip = clientSocket.getInetAddress().getHostAddress();
        System.out.println(" [ MasterNHandler ]  IP-list is requested from " + ip + ":" + port);

        returnAddressList(node, clientSocket, port);
    }

    protected static void handleLiveNotification(MasterNode node, Serializable packet, Socket clientSocket) {
        int port = extractPortOfLiveNotification(packet);
        String ip = clientSocket.getInetAddress().getHostAddress();
        System.out.println(" [ MasterNHandler ]  " + ip + ":" + port + " is alive");
        addClientToDNS(node, packet, clientSocket);
    }



    /* ============================================================================================================== */


    private static void returnAddressList(MasterNode node, Socket clientSocket, int port) {
        Serializable AddressList = node.getDns().getAddressList();

        String ip = clientSocket.getInetAddress().getHostAddress();

        System.out.println(" [ MasterNHandler ]  Sending back IP-list to : " + ip + ":" + port);
        node.sendToClient(AddressList, ip, port);
    }

    private static int extractPortOfLiveNotification(Serializable packet) {
        return Integer.parseInt(((String) packet).substring(8));
    }

    private static void addClientToDNS(MasterNode node, Serializable obj, Socket clientSocket) {
        String ip = clientSocket.getInetAddress().getHostAddress();
        int port = extractPortOfLiveNotification(obj);
        node.getDns().addAddress(ip, port);
    }




}
