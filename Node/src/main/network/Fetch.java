package main.network;

import main.node.Node;
import main.node.SimpleNode;

import java.io.IOException;

public class Fetch {

    /*Object waitingForIPLock;
    Object waitingForBCLock;
    String masterNodeIP;
    int masterNodePort;
    int port;

    private ArrayList<ScoreAddressTuple> ScoreAddressTupleList;
    private ArrayList<Address> addressList;

    public Fetch(Node node) {
        this.waitingForIPLock = node.getWaitingForIPLock();
        this.waitingForBCLock = node.getWaitingForBCLock();
        this.masterNodeIP = node.getMasterNodeIP();
        this.masterNodePort = node.getMasterNodePort();
        this.port = node.getPort();

        this.ScoreAddressTupleList = node.getScoreAddressTupleList();
        this.addressList = node.getAddressList();
    }*/

    /**
     * Template function that fetches the ip list and then run the code passed as argument
     * example :
     *      fetchIPBefore(() -> {
     *          System.out.println("got notified !  list is :");
     *          System.out.println(addressList);
     *      });
     *
     * @param job : lambda function of the code that should be executed after fetching the ip list
     */
    public static void fetchIPBefore(SimpleNode node, Runnable job) {
        Thread t1 = new Thread(() -> {
            
            try {
                new Client().send("Requesting IP-List##" + node.getPort(), node.getMasterNodeIP(), node.getMasterNodePort());

                synchronized (node.getWaitingForIPLock()) {
                    node.getWaitingForIPLock().wait(20000);
                }

                // ---------- code that uses ip list ------------

                job.run();

                // ----------------------------------------------

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        });
        t1.start();
    }

    public static void fetchBCBefore(Node node, Address clientAddress, Runnable job) {
        Thread t1 = new Thread(() -> {

            try {
                new Client().send("RequestingBlockchain" + node.getPort(), clientAddress.getIP(), clientAddress.getPort());

                synchronized (node.getWaitingForBCLock()) {
                    node.getWaitingForBCLock().wait(10000);
                }

                // ---------- code that uses ip list ------------

                job.run();

                // ----------------------------------------------

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        });
        t1.start();
    }

    /*
     *       /!\ this function needs to be called in a fetchIPBefore /!\
     */
    public static void fetchAllScoresBefore(Node node, Runnable job) {
        Thread t1 = new Thread(() -> {
            // clean up ScoreAddressTupleList
            if (node.getScoreAddressTupleList() != null) node.getScoreAddressTupleList().clear();

            // here we ask everyone the score of his BC
            for (Address address : node.getAddressList()) {
                try {
                    new Client().send("Requesting BC-Score#" + node.getPort(), address.getIP(), address.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // here we sleep an amount of time that wraps a timeout
            try {
                Thread.sleep(1000);
                // if we didn't receive all the scores, we wait a bit more
                if (node.getScoreAddressTupleList().size() < node.getAddressList().size())
                    Thread.sleep(4000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // here all the scores should have been received
            job.run();
        });

        t1.start();
    }
}
