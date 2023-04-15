package shared.packets;

import java.io.Serializable;

public class FinalContractPacket implements Serializable {

    private final String winner;
    private final String loser;
    private final String contract;

    public FinalContractPacket(String winner, String loser, String contract) {
        this.winner = winner;
        this.loser = loser;
        this.contract = contract;
    }

    public String getWinner() {
        return winner;
    }

    public String getLoser() {
        return loser;
    }

    public String getContract() {
        return contract;
    }
}
