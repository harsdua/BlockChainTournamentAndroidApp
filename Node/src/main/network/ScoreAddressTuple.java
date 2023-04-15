package main.network;

public class ScoreAddressTuple {
    private int score;
    private Address address;

    public ScoreAddressTuple(int score, Address address) {
        this.score = score;
        this.address = address;
    }

    public int getScore() {
        return score;
    }

    public Address getAddress() {
        return this.address;
    }

}
