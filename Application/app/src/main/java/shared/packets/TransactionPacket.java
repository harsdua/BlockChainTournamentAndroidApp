package shared.packets;

import java.io.Serializable;

public class TransactionPacket implements Serializable {

    private final String umpireSignature;
    private final String winnerPk;
    private final String loserPK;
    private final String winnerLoserContract;
    private final String umpireKey;
    private final String contract;


    public TransactionPacket(String umpireSignature, String winnerPk, String loserPK, String winnerLoserContract, String umpireKey, String contract) {
        this.umpireSignature = umpireSignature;
        this.winnerPk = winnerPk;
        this.loserPK = loserPK;
        this.winnerLoserContract = winnerLoserContract;
        this.umpireKey = umpireKey;
        this.contract = contract;
    }

    public String getUmpireSignature() {
        return umpireSignature;
    }

    public String getUmpireKey() {
        return umpireKey;
    }

    public String getWinnerPk() {
        return winnerPk;
    }

    public String getLoserPK() {
        return loserPK;
    }

    public String getWinnerLoserContract() {
        return winnerLoserContract;
    }

    public String getContract() {
        return contract;
    }
}
