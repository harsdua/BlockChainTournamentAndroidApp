package main.node;

import main.ledger.Blockchain;
import main.ledger.Match;
import main.network.Address;
import main.network.DNS;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;


public class MasterNode extends Node{

    DNS dns;

    public MasterNode(int port , DNS dns) throws IOException {
        super(port);
        this.dns = dns;
        this.setBlockchain(new Blockchain());
    }

    public DNS getDns() {
        return dns;
    }

    @Override
    protected void extraHandler(Serializable packet, Socket clientSocket) {

        if (MasterNodeHandler.isIPListRequest(packet)) {
            MasterNodeHandler.handleIPListRequest(this, packet, clientSocket);
        }

        else if (MasterNodeHandler.isLiveNotification(packet)) {
            MasterNodeHandler.handleLiveNotification(this, packet, clientSocket);
        }

        else{
            System.out.println(" [ MasterNode ]      unknown packet received : " + packet);
        }
    }

    @Override
    protected void broadcast(Serializable packet) {
        // broadcast match to nodes
        System.out.println(" [ MasterNode ]      Broadcasting match to nodes");

        ArrayList<Address> fullAddressList = this.getDns().getAddressList();
        fullAddressList.add(new Address("127.0.0.1", this.port));

        for (Address address : fullAddressList) {
            String IP = address.getIP();
            int PORT = address.getPort();
            this.sendToClient(packet, IP, PORT);
        }
    }
}
