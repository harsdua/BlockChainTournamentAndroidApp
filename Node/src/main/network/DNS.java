package main.network;

import shared.packets.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class DNS {

    private final ArrayList<Address> addressList = new ArrayList<>();

    public void Launch() throws InterruptedException, IOException {

        System.out.println(" [ DNS ]             DNS launch. Starting IP management");
        while (true) {

            // We take the list in reverse such that the removal of elements doesn't mess up our loop
            for (int i = addressList.size(); i > 0 ; i--) {
                int port = addressList.get(i-1).getPort();
                String IP = addressList.get(i-1).getIP();

                if (addressList.get(i-1).isOutdated()) {
                    addressList.remove(addressList.get(i-1));

                } else if(addressList.get(i-1).isAlmostOutdated()) {
                    System.out.println(" [ DNS ]             " + IP + ":" + port + " TTL almost outdated");

                    // We send a request to the node to check if it's still alive
                    requestAlive(addressList.get(i-1).getIP(), addressList.get(i-1).getPort());

                } else {
                    System.out.println(" [ DNS ]             " + IP + ":" + port + " is UP");
                }


            }
            System.out.println("\n [ DNS ]             sleep\n");
            TimeUnit.SECONDS.sleep(10);
        }
    }

    private void requestAlive(String IP, int port) throws IOException {
        try {
            System.out.println(" [ DNS ]             -> " + IP + ":" + port + " are u still alive ?");
            new Client().send("stillAliveRequest", IP, port);
        } catch (IOException e) {
            System.out.println(" [ DNS ]             -> " + IP + ":" + port + " is not responding");
        }
    }

    public ArrayList<Address> getAddressList() {
        return addressList;
    }

    public void addAddress(String IP, int port) {
        for (Address address : addressList) {
            if (address.getIP().equals(IP) && address.getPort() == port) {
                address.refreshTimeStamp();
                return;
            }
        }

        addressList.add(new Address(IP, port));
    }

}
