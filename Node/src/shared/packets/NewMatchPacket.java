package shared.packets;

import java.io.Serializable;

public class NewMatchPacket implements Serializable {
    private final String matchCode;

    public NewMatchPacket(String matchCode) {
        this.matchCode = matchCode;
    }

    public String getMatchCode() {
        return matchCode;
    }
}
