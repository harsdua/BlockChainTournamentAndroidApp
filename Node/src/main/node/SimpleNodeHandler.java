package main.node;

import main.ledger.Match;
import main.network.Address;
import main.network.Fetch;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Stateless static class used to contain the simple node handler related methods
 */
public class SimpleNodeHandler extends NodeHandler {

    /* ============================================================================================================== */
    /*                                          packet identifiers                                                    */
    /* ============================================================================================================== */


    protected static boolean isLiveRequest(Serializable packet) {
        return packet instanceof String && packet.equals("stillAliveRequest");
    }

    protected static boolean isAddressList(Serializable packet){
        if (packet instanceof ArrayList<?> IpList) {
            return !IpList.isEmpty() && IpList.iterator().next() instanceof Address;
        }
        else{
            return false;
        }
    }


    /* ============================================================================================================== */
    /*                                          packet handlers                                                    */
    /* ============================================================================================================== */


    protected static void handleLiveRequest(SimpleNode node) {
        System.out.println(" [ SimpleNHandler ]  DNS asks if I'm still alive");
        node.IAmAlive(node.getPort());
    }

    protected static void handleAddressList(SimpleNode node, String masterIP, int masterPort, Serializable packet){
        System.out.println(" [ SimpleNHandler ]  IP list fetched successfully");
        receiveAddressList(node, new Address(masterIP, masterPort), packet);
    }


    /* ============================================================================================================== */


    private static void receiveAddressList(SimpleNode node, Address masterAddress, Serializable packet) {

        ArrayList<Address> list = (ArrayList<Address>) packet;
        list.add(masterAddress);
        node.setAddressList(list);

        synchronized (node.getWaitingForIPLock()) {
            node.getWaitingForIPLock().notifyAll();
        }
    }
}

