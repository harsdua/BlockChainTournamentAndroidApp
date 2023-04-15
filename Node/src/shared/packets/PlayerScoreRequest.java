package shared.packets;

import java.io.Serializable;

public class PlayerScoreRequest implements Serializable {
    private final String publicKey;

    public PlayerScoreRequest(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}

