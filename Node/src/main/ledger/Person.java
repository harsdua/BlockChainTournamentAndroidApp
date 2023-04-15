package main.ledger;

/*
  , ; ,   .-'"""'-.   , ; ,
  \\|/  .'         '.  \|//
   \-;-/   ()   ()   \-;-/
   // ;               ; \\
  //__; :.         .; ;__\\
 `-----\'.'-.....-'.'/-----'
        '.'.-.-,_.'.'
          '(  (..-'
            '-'
*/

import java.io.Serializable;
import java.security.PublicKey;

public class Person implements Serializable {
    private double reputation;
    private final PublicKey publicKey;



    public Person(PublicKey publicKey) {
        this(publicKey,1);
    }

    public Person(PublicKey publicKey, double reputation) {
        this.publicKey = publicKey;
        this.reputation = reputation;
    }

    /*  ====== Setters ====== */
    public void setReputation(double reputation) { this.reputation = reputation; }

    /* ====== Getters ====== */
    public double getReputation() {
        return reputation;
    }
    public PublicKey getPublicKey() {
        return this.publicKey;
    }
}
