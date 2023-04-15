package com.example.blockchaineloapp;

import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NodeClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public abstract class SigningActivity extends NodeUserActivity {

    protected KeyPair loadKeyPair() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null, null);

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("EloKeys", null);

            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            PublicKey publicKey = ks.getCertificate("EloKeys").getPublicKey();

            return new KeyPair(publicKey, privateKey);

        } catch (KeyStoreException e) {
            makeToast("Problem loading keys");
            finish();

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected String signContract(String contract, PrivateKey sk) {
        try {
            Signature sig = Signature.getInstance("SHA512withRSA");
            sig.initSign(sk);
            sig.update(contract.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(sig.sign());

        } catch (Exception e) {
            makeToast("Problem signing");
            finish();

            return null;
        }
    }


    private boolean isNotValidMatchCode(String input) {
        return input.length() != 6;
    }

    private boolean isNotValidPlayer(String input) {
        try {
            int player = Integer.parseInt(input);

            return player != 1 && player != 2;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    protected boolean incompleteInput(String matchCode, String player) {

        if (isNotValidPlayer(player)) {
            String badPlayerNumber = "Enter a valid player number";
            makeToast(badPlayerNumber);

            return true;
        }

        if (isNotValidMatchCode(matchCode)) {
            String badMatchCodeError = "Enter a valid match code";
            makeToast(badMatchCodeError);

            return true;
        }

        return false;
    }
}
