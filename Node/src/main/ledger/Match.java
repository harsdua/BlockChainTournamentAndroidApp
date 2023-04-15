package main.ledger;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;


public class Match implements Serializable {
    private final String winnerPk;
    private final String loserPk;
    private final String umpirePk;
    private final LocalDateTime timeStamp;
    private final String contract;
    private final String contractSignature;


    public Match(String winnerPk, String loserPk, String umpirePk, String contract, String contractSignature) {
        this.winnerPk = winnerPk;
        this.loserPk = loserPk;
        this.umpirePk = umpirePk;
        this.contract = contract;
        this.contractSignature = contractSignature;
        timeStamp = LocalDateTime.now();
    }

    public String getWinner() {
        return winnerPk;
    }

    public String getLoserPk() {
        return loserPk;
    }

    public String getUmpirePk() {
        return umpirePk;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getContract() {
        return contract;
    }

    public String getUmpireShortSignature() {
        return contract.substring(90,100);
    }



    @Override
    public String toString() {
        return "Match{" +
                "winner=" + winnerPk +
                ", loser=" + loserPk +
                ", umpire=" + umpirePk +
                ", timeStamp=" + timeStamp.toString() +
                '}';
    }

    public String toStringShort() {
        return "Match{" +
                "winnerShort=" + winnerPk.substring(90,100) +
                ", loserShort=" + loserPk.substring(90,100) +
                ", umpireShort=" + umpirePk.substring(90,100) +
                ", timeStamp=" + timeStamp.toString() +
                '}';
    }

    public boolean isValid() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Base64.Decoder decoder = Base64.getDecoder();
        PublicKey umpirePublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoder.decode(umpirePk)));
        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initVerify(umpirePublicKey);

        byte[] hashedContractBytes = contract.getBytes(StandardCharsets.UTF_8);
        sig.update(hashedContractBytes, 0, hashedContractBytes.length);

        byte[] signatureBytes = Base64.getDecoder().decode(contractSignature);
        return sig.verify(signatureBytes);
    }


}