package main.network;

class KeyContractMatchcodeTruple {
    private final String key;
    private final String contract;
    private final String matchCode;

    public KeyContractMatchcodeTruple(String matchCode, String key, String contract) {
        this.key = key;
        this.contract = contract;
        this.matchCode = matchCode;
    }

    String getKey() {  return key;  }
    String getContract() {  return contract; }
    String getMatchCode() { return matchCode; }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final KeyContractMatchcodeTruple other = (KeyContractMatchcodeTruple) obj;

        return this.getKey().equals(other.getKey());
    }
}
