package com.example.blockchaineloapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import shared.packets.ContractPacket;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class SignMatch extends SigningActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_match);

        setTitle("Signer un match");
        sendSignatureOnClick(R.id.sign);
    }

    @Override
    protected void nodeResponseHandler(Serializable response) {
        Log.d("Node response", response.toString() == null ? "null" : response.toString());
    }

    protected String constructContract(String player) {
        return " I am player " + player + " and I agree to participate in this match ";
    }

    /**
     * Associates "send text on click" action to button returned
     *
     * @param id view id
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendSignatureOnClick(int id) {

        findViewById(id).setOnClickListener(view -> {
            try {
                EditText codeInput = findViewById(R.id.inputMatchID);
                EditText playerInput = findViewById(R.id.inputPlayer);

                String matchCode = codeInput.getText().toString();
                String player = playerInput.getText().toString();

                if (incompleteInput(matchCode, player))
                    return;

                KeyPair kp = loadKeyPair();
                PrivateKey privateKey = kp.getPrivate();
                PublicKey publicKey = kp.getPublic();

                ContractPacket cp = new ContractPacket(
                        Integer.parseInt(player),
                        signContract(constructContract(player), privateKey),
                        matchCode,
                        Base64.getEncoder().encodeToString(publicKey.getEncoded())
                );

                sendToNode(cp);

                finish();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}