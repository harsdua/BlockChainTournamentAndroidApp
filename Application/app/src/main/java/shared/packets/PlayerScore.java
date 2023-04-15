package shared.packets;

import java.io.Serializable;

public class PlayerScore implements Serializable {
    private final String score;

    public PlayerScore(String score) {
        this.score = score;
    }

    public String getScore() {
        return score;
    }
}
