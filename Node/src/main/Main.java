package main;

import main.network.*;
import main.node.MasterNode;
import main.node.Node;
import main.node.SimpleNode;

import shared.packets.Log;



public class Main {
    public static void main(String[] args) throws Exception {

        /* ====================================================================================================== */
        /*                                             Arguments :                                                */
        /*                                                                                                        */
        /*                                master node :                                                           */
        /*                                        java -jar <jar_name> master [ LoadBC ]                          */
        /*                                                                                                        */
        /*                                simple node :                                                           */
        /*                                        java -jar <jar_name> <master_node_ip> <port (optional)>         */
        /*                                                                                                        */
        /* ====================================================================================================== */

        // fixed port
        int port = 8088;

        DNS dns = new DNS();
        Node node;


        // Case this is the master node
        if (args[0].equals("master")) {
            Log.getInstance("master");
            System.out.println("        MASTER NODE");
            System.out.println("----------------------------");

            node = new MasterNode(port, dns);

            if ( args.length == 2 && args[1].equals("LoadBc")) {
                node.setBlockchain(Log.getInstance().readBC());
            }

            Thread DNSThread = new Thread(() -> {
                try {
                    dns.Launch();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            DNSThread.start();
        }

        // Case this is a simple node
        else if (args.length == 1 || args.length == 2) {
            int masterNodePort = port;

            if ( args.length == 2 && !args[1].equals("LoadBc") ) {
                port = Integer.parseInt(args[1]); }

            Log.getInstance(args[1]);

            System.out.println("        single node");
            System.out.println("      (on port: " + port + ")");

            node = new SimpleNode(port, args[0], masterNodePort);

            System.out.println("     (on IP : " + node.getIP() + ")");      // TODO : that func is acting weird
            System.out.println("\n   master node is at :");
            System.out.println("      " + args[0] + ":" + masterNodePort);
            System.out.println("----------------------------");
        }

        else {
            throw new Exception("E: wrong arguments");
        }


        // exec node.listen
        System.out.println(" [ Main ]            Node begins listening");
        // for testing rn
        while (true) {
            node.listen();
        }
    }
}