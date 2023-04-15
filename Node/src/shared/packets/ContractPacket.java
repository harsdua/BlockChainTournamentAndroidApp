package shared.packets;

import java.io.Serializable;

public class ContractPacket implements Serializable {

    private final int player;
    private final String signedContract;
    private final String matchCode;
    private final String pk;

    public ContractPacket(int player, String signedContract, String matchCode, String pk) {
        this.player = player;
        this.signedContract = signedContract;
        this.matchCode = matchCode;
        this.pk = pk;
    }

    public int getPlayer() {
        return player;
    }

    public String getSignedContract() {
        return signedContract;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public String getPk() {
        return pk;
    }

    public String getShortPk() {
        return pk.substring(90,100);
    }

}