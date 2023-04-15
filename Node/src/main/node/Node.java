package main.node;

import main.ledger.*;
import main.network.Address;
import main.network.ScoreAddressTuple;
import main.network.Server;
import shared.packets.ContractPacket;
import shared.packets.RequestFinalContractPackage;
import shared.packets.TransactionPacket;
import shared.packets.*;
import shared.packets.Log;


import java.io.IOException;

import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.logging.Logger;


/*
                                            |---||---|
  ,;:;,        /^\   /^\         \          |---||---|
 ((@ @))       \\\\.////          \_____________||__________ /
 ))\=/((        \\\|///            \       nerd yacht       /
.((/"\))         \\|//              \______________________/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
public abstract class Node extends Server {

    //
    //                  DATA
    //          ---------------------

    protected Blockchain blockchain;
    protected ArrayList<ScoreAddressTuple> ScoreAddressTupleList;

    // (Match code, contract) pair
    private final HashMap< String, ArrayList<String> > matchCodeContracts = new HashMap<>();

    // (Match code, public key) pair
    private final HashMap< String, ArrayList<String> > matchCodePk = new HashMap<>();

    // (Match code, (player number, public key)) pair
    private final HashMap< String, HashMap<Integer, String>> matchCodePlayerPk = new HashMap<>();

    // (Match code, (player, contract)) pair
    private final HashMap< String, HashMap<Integer, String>> playerContract = new HashMap<>();

    ArrayList<Address> addressList = new ArrayList<>();

    // Thread lockers
    private final Object waitingForIPLock = new Object();
    private final Object waitingForBCLock = new Object();

    protected final Logger logger = Logger.getLogger(Node.class.getName());


    //
    //                 GETTERS
    //          ---------------------

    public ArrayList<ScoreAddressTuple> getScoreAddressTupleList() {
        return ScoreAddressTupleList;
    }

    public HashMap<String, ArrayList<String>> getMatchCodeContracts() {
        return matchCodeContracts;
    }

    public HashMap<String, ArrayList<String>> getMatchCodePk() {
        return matchCodePk;
    }

    public HashMap<String, HashMap<Integer, String>> getMatchCodePlayerPk() {
        return matchCodePlayerPk;
    }

    public HashMap<String, HashMap<Integer, String>> getPlayerContract() {
        return playerContract;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }
    public ArrayList<Address> getAddressList() {
        return addressList;
    }

    public Object getWaitingForBCLock() {
        return waitingForBCLock;
    }

    public Object getWaitingForIPLock() {
        return waitingForIPLock;
    }


    //
    //                 SETTERS
    //          ---------------------


    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }
    public void setAddressList(ArrayList<Address> addressList) {
        this.addressList = addressList;
    }

    //
    //                   CONSTRUCTORS
    //          ------------------------------


    public Node(int port) throws IOException {
        super(port);
    }


    //
    //                      METHODS
    //          ------------------------------


    public void resetForMatchCode(String matchCode) {
        matchCodeContracts.remove(matchCode);
        matchCodePk.remove(matchCode);
        matchCodePlayerPk.remove(matchCode);
        playerContract.remove(matchCode);
    }

    public boolean isValidSignedContract(String pk, String sc, String expectedResult) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();

            PublicKey publicKey =
                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoder.decode(pk)));

            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify(publicKey);

            signature.update(expectedResult.getBytes(StandardCharsets.UTF_8));

            return signature.verify(decoder.decode(sc));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if signed contract is valid using pk
     *
     * @param cp contract packet
     * @return true if valid, false otherwise
     */
    public boolean isValidSignedParticipation(ContractPacket cp) {
        String genericContract = " I am player " + cp.getPlayer() + " and I agree to participate in this match ";
        return isValidSignedContract(cp.getPk(), cp.getSignedContract(), genericContract);
    }


    public boolean isValidTransaction(TransactionPacket tp) {
        String winnerPk = tp.getWinnerPk();
        String loserPk = tp.getLoserPK();
        String genericContract = " Player " + winnerPk + " beat player " + loserPk + " " + tp.getWinnerLoserContract() + " ";
        return isValidSignedContract(tp.getUmpireKey(), tp.getUmpireSignature(), genericContract);
    }


    public boolean isNewPlayer(String pk, Integer player, String matchCode) {
        return (matchCodePk.containsKey(matchCode) && !matchCodePk.get(matchCode).contains(pk))
                && (matchCodePlayerPk.containsKey(matchCode) && !matchCodePlayerPk.get(matchCode).containsKey(player));
    }


    public String finalContractGenerator(RequestFinalContractPackage cp) {

        int winner = Integer.parseInt(cp.getWinner());
        int loser = winner == 1 ? 2 : 1;

        String winnerContract = playerContract.get(cp.getMatchCode()).get(winner);
        String winnerPk = matchCodePlayerPk.get(cp.getMatchCode()).get(winner);

        String loserContract = playerContract.get(cp.getMatchCode()).get(loser);
        String loserPk = matchCodePlayerPk.get(cp.getMatchCode()).get(loser);

        String completeWinner = winnerContract + winnerPk;
        String completeLoser = loserContract + loserPk;

        return completeWinner + completeLoser;
    }


    protected ArrayList<ScoreAddressTuple> sortScoreAddressTupleList(ArrayList<ScoreAddressTuple> ScoreAddressTupleList) {
        ScoreAddressTupleList.sort((o1, o2) -> o2.getScore() - o1.getScore());

        //TODO: remove this after testing
        System.out.println(" [ Node ]            \nsorted list: ");
        for (ScoreAddressTuple ScoreAddressTuple : ScoreAddressTupleList) {
            System.out.println(ScoreAddressTuple.getScore());
        }
        System.out.println();

        return ScoreAddressTupleList;
    }

    protected boolean verifyBC() throws Exception {
        if (this.blockchain == null) {
            return false;
        }
        else if (!this.blockchain.isValid()) {
            this.blockchain = null;
            return false;
        }

        return true;
    }




    /* ============================================================================================================== */
    /*                                        Network reception Handler                                               */
    /* ============================================================================================================== */

    @Override
    protected void receptionHandler(Serializable packet, Socket clientSocket) {

        if (NodeHandler.isBlockchain(packet))
            NodeHandler.handleBlockchain(this, (Blockchain) packet);

        // Umpire gets the contract signed by both players
        else if (NodeHandler.isContractRequest(packet))
            NodeHandler.handleContractRequest(this, packet, clientSocket);

        // Players send code (given by umpire irl) to the node
        else if (NodeHandler.isSignedContract(packet))
            NodeHandler.handleSignedContract(this, packet);

        // Umpire send the new match code to the node
        else if (NodeHandler.isNewMatchCode(packet))
            NodeHandler.handleNewMatchCode(this, packet);

        // Umpire has sent a fully signed transaction
        else if (NodeHandler.isTransactionPacket(packet)) {
            Match match = NodeHandler.handleTransaction(this, packet);
            this.broadcast(match);
        }

        else if (NodeHandler.isBCScore(packet))
            NodeHandler.handleBCScore(this, packet, clientSocket);

        else if (NodeHandler.isBlockchainScoreRequest(packet))
            NodeHandler.handleBlockchainScoreRequest(this, packet, clientSocket);

        else if (NodeHandler.isBlockchainRequest(packet))
            NodeHandler.handleBlockchainRequest(this, packet, clientSocket);

        else if (NodeHandler.isPlayerScoreRequest(packet)) {
            System.out.println(" [ Node ]            PLayer requests Score");
            NodeHandler.handlePlayerScoreRequest(this, packet, clientSocket);
        }
        // requestLeaderboardPacket

        else if (NodeHandler.RequestLeaderboardPackage(packet)) {
            System.out.println(" [ Node ]            Requested leaderboard");
            NodeHandler.handleLeaderboardRequest(this, packet, clientSocket);
        }

        else if (NodeHandler.isMatch(packet))
            NodeHandler.handleMatch(this, (Match) packet);

        else
            extraHandler(packet, clientSocket);
    }

    protected abstract void extraHandler(Serializable obj, Socket clientSocket);


    /* ============================================================================================================== */
    /*                                                 UTILS                                                          */
    /* ============================================================================================================== */


    byte[] combineByteArrays(byte[] pk, byte[] signedContract) {

        byte[] pkContractCombined = new byte[pk.length +signedContract.length];
        System.arraycopy(signedContract,0, pkContractCombined, 0, signedContract.length);
        System.arraycopy(pk,0, pkContractCombined, signedContract.length, pk.length);

        return pkContractCombined;
    }

}