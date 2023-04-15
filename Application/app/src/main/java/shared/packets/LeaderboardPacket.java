package shared.packets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LeaderboardPacket implements Serializable {
    private final List<PkEloPair> leaderboard;

    public LeaderboardPacket(List<PkEloPair> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public List<PkEloPair> getLeaderboard() {
        return leaderboard;
    }
}
