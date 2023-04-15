package shared.packets;
import java.io.Serializable;
public class PkEloPair implements Serializable  {
    private int ELO;
    private String PK;

    public PkEloPair(int elo, String publiKey) {
        this.ELO = elo;
        this.PK = publiKey;
    }
    public int getElo() {
        return ELO;
    }
    public String getPk() { return this.PK; }
    public String getShortPk() {
        return this.PK.substring(90,100);
    }
}



    