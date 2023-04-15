package main.node;

import main.ledger.Blockchain;
import main.ledger.Match;
import main.network.Address;
import main.network.ScoreAddressTuple;
import shared.packets.*;

import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import shared.packets.Log;


public class NodeHandler {



    /* ============================================================================================================== */
    /*                                          packet identifiers                                                    */
    /* ============================================================================================================== */


    protected static boolean isMatch(Serializable packet) {
        return packet instanceof Match;
    }

    protected static boolean isBlockchain(Serializable packet) {
        return packet instanceof Blockchain;
    }

    protected static boolean isContractRequest(Serializable packet) {
        return packet instanceof RequestFinalContractPackage;
    }

    protected static boolean isSignedContract(Serializable packet) {
        return packet instanceof ContractPacket;
    }

    protected static boolean isNewMatchCode(Serializable packet) {
        return packet instanceof NewMatchPacket;
    }

    protected static boolean isBCScore(Serializable packet){
        return packet instanceof String && ((String) packet).contains("Score:");
    }

    protected static boolean isBlockchainRequest(Serializable packet) {
        return packet instanceof String && ((String) packet).contains("RequestingBlockchain");
    }

    protected static boolean isBlockchainScoreRequest(Serializable packet) {
        return packet instanceof String && ((String) packet).contains("Requesting BC-Score#");
    }

    protected static boolean isTransactionPacket(Serializable packet) {
        return packet instanceof TransactionPacket;
    }

    protected static boolean isPlayerScoreRequest(Serializable packet) {
        return packet instanceof PlayerScoreRequest;
    }

    protected static boolean RequestLeaderboardPackage(Serializable packet) {
        return packet instanceof RequestLeaderboardPacket;
    }


    /* ============================================================================================================== */
    /*                                           Packet handlers                                                      */
    /* ============================================================================================================== */

    protected static void handleBlockchain(Node node, Blockchain blockchain) {
        node.setBlockchain(blockchain);

        synchronized (node.getWaitingForBCLock()) {
            node.getWaitingForBCLock().notifyAll();
        }
        System.out.println(node.getBlockchain());
        System.out.println("get blockchain");
        Log.getInstance().writeLog(blockchain.toString(), "yes" );

    }

    protected static Match handleTransaction(Node node, Serializable packet) {
        TransactionPacket tp = (TransactionPacket) packet;

        if (!node.isValidTransaction(tp))
            return null;


        Match match = new Match(
                tp.getWinnerPk(),
                tp.getLoserPK(),
                tp.getUmpireKey(),
                tp.getContract(),
                tp.getUmpireSignature());

        System.out.println(match.toStringShort());
        Log.getInstance().writeLog( match.toStringShort(), "yes" );

        return match;
    }

    public static void handleContractRequest(Node node, Serializable packet, Socket clientSocket) {
        RequestFinalContractPackage cp = (RequestFinalContractPackage) packet;
        String matchCode = cp.getMatchCode();


        if (!node.getMatchCodeContracts().containsKey(matchCode)
                || node.getMatchCodeContracts().get(matchCode).size() < 2)

            return;

        System.out.println("MatchCode found and complete");
        Log.getInstance().writeLog("Contract Requested by Umpire" , "yes");


        int winner = Integer.parseInt(cp.getWinner());

        int loser = (winner == 1) ? 2 : 1;

        Log.getInstance().writeLog("Winner: " + winner + " Loser: " + loser, "yes");

        String winnerPk = node.getMatchCodePlayerPk()
                .get(matchCode)
                .get(winner);

        String loserPk = node.getMatchCodePlayerPk()
                .get(matchCode)
                .get(loser);

        Log.getInstance().writeLog( "looking up pks in generic contract " + matchCode, "yes");
        Log.getInstance().writeLog(node.getMatchCodePlayerPk().get(matchCode).toString(), "yes");


        Log.getInstance().writeLog(new FinalContractPacket(winnerPk, loserPk, node.finalContractGenerator(cp)).toString(), "yes");

        if (node.sendToClient(new FinalContractPacket(winnerPk, loserPk, node.finalContractGenerator(cp)), clientSocket))
            node.resetForMatchCode(matchCode);

    }

    public static void handlePlayerScoreRequest(Node node, Serializable packet, Socket clientSocket) {
        PlayerScoreRequest psr = (PlayerScoreRequest) packet;
        String publicKey = psr.getPublicKey();
        int score = node.getBlockchain().calculateEloOf(publicKey);
        node.sendToClient(new PlayerScore(Integer.toString(score)), clientSocket);
    }

    public static void handleLeaderboardRequest(Node node, Serializable packet, Socket clientSocket) {
        Log.getInstance().writeLog("sending Leaderboard ");
        node.sendToClient( new LeaderboardPacket(node.getBlockchain().calculateLeaderboard()), clientSocket);
    }

    /**
     * Checks if signature is valid and saves it if it is valid, ignores is if not
     */
    protected static void handleSignedContract(Node node, Serializable packet) {

        ContractPacket cp = (ContractPacket) packet;
        String matchCode = cp.getMatchCode();

        Log.getInstance().writeLog("Received SignedContract " + cp, "yes" );

        if (!node.getMatchCodeContracts().containsKey(matchCode)
                || !node.isValidSignedParticipation(cp)
                || node.getMatchCodePlayerPk().get(matchCode).containsKey(cp.getPlayer())) {

            noMatchCodeLog(cp.getMatchCode());
            return;
        }

        /*
            Recording Contract match code pairs
         */
        node.getMatchCodeContracts()
                .get(matchCode)
                .add(cp.getSignedContract() + cp.getPk());

        /*
            Recording player number, public key pairs associated to match code
         */
        node.getMatchCodePlayerPk()
                .get(matchCode)
                .put(cp.getPlayer(), cp.getPk());

        receiveSignedContractLog(node, cp);
    }

    protected static void handleNewMatchCode(Node node, Serializable packet) {
        String matchCode = ((NewMatchPacket) packet).getMatchCode();

        node.getMatchCodeContracts().put(matchCode, new ArrayList<>());
        node.getMatchCodePk().put(matchCode, new ArrayList<>());
        node.getMatchCodePlayerPk().put(matchCode, new HashMap<>());
        node.getPlayerContract().put(matchCode, new HashMap<>());

        receiveNewMatchCodeLog(matchCode);
        Log.getInstance().writeLog("Received matchCode " + matchCode.toString(), "yes" );

    }

    protected static void handleBCScore(Node node, Serializable packet, Socket clientSocket) {
        int score = Integer.parseInt(packet.toString().substring(0, 14));
        int port = extractPortFromMessage(packet);
        String ip = clientSocket.getInetAddress().getHostAddress();


        System.out.println(" [ NodeHandler ]     Score received: " + score + " port: " + port);
        Log.getInstance().writeLog("receiving score: " + score + " port: " + port, "yes");

        node.getScoreAddressTupleList().add(new ScoreAddressTuple(score, new Address( ip, port)));
    }

    protected static void handleBlockchainScoreRequest(Node node, Serializable packet, Socket clientSocket) {
        System.out.println(" [ NodeHandler ]     BC score requested");
        if (node.getBlockchain() != null)
            returnBCScore(node, clientSocket, NodeHandler.extractPortFromMessage(packet));
    }

    protected static void handleBlockchainRequest(Node node, Serializable packet, Socket clientSocket) {
        int port = NodeHandler.extractPortFromMessage(packet);
        String ip = clientSocket.getInetAddress().getHostAddress();
        System.out.println(" [ NodeHandler ]     Received a BC request from " + ip + ":" + port);

        if (node.getBlockchain() != null)
            returnBC(node, clientSocket, NodeHandler.extractPortFromMessage(packet));
    }

    protected static void handleMatch(Node node, Match match){
        node.getBlockchain().addMatch(match);

        Log.getInstance().writingBC( node.getBlockchain());


        // make log the fact that a match has been added
        Log.getInstance().writeLog("Match added to blockchain: " + match.toStringShort(), "yes");
        Log.getInstance().writeLog("Blockchain: " + node.getBlockchain().toString(), "yes");

        System.out.println(" [ NodeHandler ]     Added match to blockchain (may have written to file)");
    }
    /* ============================================================================================================== */
    /*                                          Receive Methods                                                      */
    /* ============================================================================================================== */


    private static void receiveSignedContractLog(Node node, ContractPacket cp) {
        System.out.println();
        System.out.println(" [ NodeHandler ]     Received matching Signed Contract with Code: "
                + cp.getMatchCode()
                + "\n Unique signatures obtained: "
                + node.getMatchCodeContracts().get(cp.getMatchCode()).size()
        );
        System.out.println();
        System.out.println(" [ NodeHandler ]     Public key: " + cp.getShortPk());
        System.out.println();
    }

    private static void receiveNewMatchCodeLog(String matchCode) {
        System.out.println();
        System.out.println(" [ NodeHandler ]     New match creation code received: " + matchCode);
        System.out.println();
    }


    /* ============================================================================================================== */
    /*                                                   utils                                                        */
    /* ============================================================================================================== */


    private static void noMatchCodeLog(String matchCode) {
        System.out.println(" [ NodeHandler ]     Code (" + matchCode + ") did not match most recent match creation or contract was bad");
    }

    protected static int extractPortFromMessage(Serializable obj) {      // Todo put 20 in constant not hardcoded
        return Integer.parseInt(((String) obj).substring(20));  // 20 is the convention of port substring
    }

    private static void returnBCScore(Node node, Socket clientSocket, int port) {
        String ip = clientSocket.getInetAddress().getHostAddress();

        String requestType= "Score:";
        String score = String.valueOf(node.getBlockchain().calculateScore());
        int len = score.length();
        String formatter = "00000000000000";

        // format needs to be 24 digits long example : "00000000000123Score:8080"
        //       with score==123 : 00000000000  (14-3)  + 123   + "Score:" + 8080
        String message = formatter.substring(0, 14-len) + score + requestType + node.getPort();

        node.sendToClient(message, ip, port);
    }

    private static void returnBC(Node node, Socket clientSocket, int port) {
        Serializable sendingBlockchain = node.getBlockchain();
        String ip = clientSocket.getInetAddress().getHostAddress();
        System.out.println(" [ NodeHandler ]     Sending BC back to " + ip + ":" + port);

        node.sendToClient(sendingBlockchain, ip, port);
    }

    /**
     * Checks if signed contract is valid using pk
     *
     */
    private boolean isValidSignedContract(String pk, String sc, String expectedResult) {
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

}
