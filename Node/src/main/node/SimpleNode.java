package main.node;

import main.ledger.Blockchain;
import main.ledger.Match;
import main.network.Address;
import main.network.Fetch;
import main.network.ScoreAddressTuple;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class SimpleNode extends Node {

    private final String masterNodeIP;
    private final int masterNodePort;

    public String getMasterNodeIP() {
        return masterNodeIP;
    }

    public int getMasterNodePort() {
        return masterNodePort;
    }

    public SimpleNode(int port, String masterNodeIP, int masterNodePort) throws IOException {
        super(port);
        this.masterNodeIP = masterNodeIP;
        this.masterNodePort = masterNodePort;

        IAmAlive(port);

        Fetch.fetchIPBefore(this, this::askBlockchain);
    }


    void IAmAlive(int port) {
        sendToClient("IAmAlive" + port, masterNodeIP, masterNodePort);
    }

    @Override
    protected void extraHandler(Serializable packet, Socket clientSocket) {

        if (SimpleNodeHandler.isLiveRequest(packet)) {
            SimpleNodeHandler.handleLiveRequest(this);
        }

        else if (SimpleNodeHandler.isAddressList(packet)) {
            SimpleNodeHandler.handleAddressList(this, masterNodeIP, masterNodePort, packet);
        }

        else{
            System.out.println(" [ SimpleNode ]      unknown packet received : " + packet);
        }
    }

    @Override
    protected void broadcast(Serializable packet) {
        // broadcast match to nodes
        Fetch.fetchIPBefore(this, () -> {

            System.out.println(" [ SimpleNode ]      Broadcasting match to nodes");

            for (Address address : this.getAddressList()) {
                String IP = address.getIP();
                int PORT = address.getPort();
                this.sendToClient(packet, IP, PORT);
            }
        });
    }

    void askBlockchain() {
        this.ScoreAddressTupleList = new ArrayList<>();

        Fetch.fetchAllScoresBefore(this, () -> {

            ArrayList<ScoreAddressTuple> sortedScoreAddressTupleList = sortScoreAddressTupleList(this.ScoreAddressTupleList);
            AtomicBoolean BCFound = new AtomicBoolean(false);
            Object lock = new Object();

            for (ScoreAddressTuple currentScoreAddressTuple: sortedScoreAddressTupleList) {
                if (!BCFound.get()) {

                    Address targetAddress = currentScoreAddressTuple.getAddress();

                    System.out.println(" [ SimpleNode ]      fetching BC @ " + targetAddress.getIP() + ":" + targetAddress.getPort() + " ...");

                    Fetch.fetchBCBefore(this, new Address(targetAddress.getIP(), targetAddress.getPort()), () -> {

                        boolean isValidBlockChain = false;

                        try {
                            isValidBlockChain = verifyBC();
                        } catch (Exception e) {
                            System.out.println(" [ SimpleNode ]      Error while verifying BC in fetch BC process ");
                            e.printStackTrace();
                        }


                        if (isValidBlockChain) {
                            System.out.println(" [ SimpleNode ]      Valid bc found in fetch BC process");
                            BCFound.set(true);
                        } else System.out.println(" [ SimpleNode ]      This Bc is not valid trying next one in fetch BC process");

                        synchronized (lock) { lock.notifyAll(); }

                    });

                    // here we wait for the BC to be fetched and verified
                    // this timeout must be >= the timeout of fetchBCBefore
                    try {
                        synchronized (lock) { lock.wait(10000); }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            if (!BCFound.get()) {
                System.out.println(" [ SimpleNode ]      no valid BC found");

                // set empty blockchain
                this.setBlockchain(new Blockchain());
            }
        });
    }

}
