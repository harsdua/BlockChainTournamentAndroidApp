package shared.packets;

import java.io.Serializable;

public class RequestFinalContractPackage implements Serializable {
    private final String matchCode;
    private final String winner;

    public RequestFinalContractPackage(String matchCode, String winner) {
        this.matchCode = matchCode;
        this.winner = winner;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public String getWinner() {
        return winner;
    }
}
