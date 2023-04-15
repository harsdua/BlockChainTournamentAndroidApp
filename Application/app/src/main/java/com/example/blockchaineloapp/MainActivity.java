package com.example.blockchaineloapp;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.security.KeyPairGenerator;
import java.security.KeyStore;

public class MainActivity extends MiddleManActivity {

    private final String keyStoreAlias = "EloKeys";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        checkSystemForKeys();

        associateActivity(R.id.umpireMatch, UmpireMatch.class);
        associateActivity(R.id.signMatch, SignMatch.class);
        associateActivity(R.id.myScore, UserScore.class);
    }

    /**
     * Generates a new RSA key pair and stores it using Android KeyStore API
     */
    private void generateKeyPair() {
        try {
          KeyPairGenerator kpg = KeyPairGenerator.getInstance(
              KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

          kpg.initialize(
              new KeyGenParameterSpec
                  .Builder(this.keyStoreAlias, KeyProperties.PURPOSE_SIGN |
                                          KeyProperties.PURPOSE_VERIFY)
                  .setDigests(KeyProperties.DIGEST_SHA512,
                              KeyProperties.DIGEST_SHA256)
                  .setKeySize(2048)
                  .setEncryptionPaddings(
                      KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                      KeyProperties.ENCRYPTION_PADDING_RSA_OAEP,
                      KeyProperties.ENCRYPTION_PADDING_NONE)
                  .setSignaturePaddings(
                      KeyProperties.SIGNATURE_PADDING_RSA_PKCS1,
                      KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                  .build());

          kpg.generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * If system does not contain an RSA key pair it generates one
     */
    private void checkSystemForKeys() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null, null);

            if (!keyStore.containsAlias(this.keyStoreAlias))
                generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}