package main.ledger;

import shared.packets.PkEloPair;

import java.io.Serializable;
import java.util.*;


public class Blockchain implements Serializable {

    private ArrayList<Match> chain;

    public Blockchain() {
        setChain(new ArrayList<>());
    }

    /*  ====== Setters ======   */
    public void setChain(ArrayList<Match> blockchain) {  this.chain = blockchain;  }

    /* ====== Getters ======  */
    public int calculateScore() { return chain.size(); }

    public void addMatch(Match match) {
        //TODO: change isMatchValid from true to match.isValid()
        boolean isMatchValid = true;
        if (isMatchValid){
            chain.add(match);
        }

    }

    /*  ======================================================================================  */

    public String toString() {
        StringBuilder blockchainString = new StringBuilder();
        blockchainString.append(" [ BlockChain ]      Displaying BC \n");
        blockchainString.append("Blockchain Size ").append(chain.size()).append('\n');

        for (int blockIndex = 0; blockIndex < chain.size(); blockIndex++) {
            blockchainString.append("\n Block#").append(blockIndex+1).append(" : \n");
            blockchainString.append("Match ").append(blockIndex).append(" : \n      ")
                    .append(chain.get(blockIndex).getContract());
        }

        return blockchainString.toString();
    }

    public String toStringShort() {
        StringBuilder blockchainString = new StringBuilder();

        blockchainString.append("Blockchain Size ").append(chain.size()).append('\n');

        for (int blockIndex = 0; blockIndex < chain.size(); blockIndex++) {
            blockchainString.append('\n');
            blockchainString.append("Match ").append(blockIndex).append(" : \n  Umpire Signature: ")
                    .append(chain.get(blockIndex).getUmpireShortSignature());
        }

        return blockchainString.toString();
    }

    public boolean isValid() throws Exception {
        for (Match match: chain ) {
            if (!match.isValid())
                return false;
        }
        return true;
    }

    public HashMap<String,Integer> calculatePlayerElo(){
        HashMap<String, Integer> pkToEloMap = new HashMap<>();
        for (Match match: chain) {
            String winnerPk = match.getWinner();
            String loserPk = match.getLoserPk();

            pkToEloMap.putIfAbsent(winnerPk, 800);
            pkToEloMap.putIfAbsent(loserPk, 800);

            int winnerElo = pkToEloMap.get(winnerPk);
            int loserElo = pkToEloMap.get(loserPk);
            pkToEloMap.put(winnerPk, winnerElo + calculateEloChange(winnerElo, loserElo));
            pkToEloMap.put(loserPk, loserElo - calculateEloChange(loserElo, winnerElo));
        }

        return pkToEloMap;
    }

    public List<PkEloPair> calculateLeaderboard() {

        HashMap<String, Integer> pkToEloMap = calculatePlayerElo();
        Set<Map.Entry<String, Integer>> set = pkToEloMap.entrySet();

        List<Map.Entry<String, Integer>> list = new ArrayList<>(set);

        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        List<PkEloPair> resList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : list) {
            resList.add(new PkEloPair(entry.getValue(), entry.getKey()));
        }

        System.out.println( resList );
        return resList;
    }

    private int calculateEloChange(int playerElo,int opponentElo) {
        double eloChangeMultiplier = opponentElo/playerElo;
        return (int) Math.round(eloChangeMultiplier*20);
    }

    public int calculateEloOf(String playerPk) {

        HashMap<String, Integer> EloMap = calculatePlayerElo();

        if (EloMap.containsKey(playerPk)) {
            return EloMap.get(playerPk);
        }
        return 0;
    }

}