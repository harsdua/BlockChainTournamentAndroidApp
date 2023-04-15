package com.example.blockchaineloapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import shared.packets.FinalContractPacket;
import shared.packets.NewMatchPacket;
import shared.packets.RequestFinalContractPackage;
import shared.packets.TransactionPacket;

public class UmpireMatch extends SigningActivity {
    
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_umpire_match);

        setTitle("Aribitrer un match");

        EditText matchIDOutput = findViewById(R.id.match_id_display);
        matchIDOutput.setFocusable(false);
        matchIDOutput.setClickable(false);

        initCreateMatchButton(matchIDOutput);

        EditText matchIDInput = findViewById(R.id.matchIDInputUmpire);
        EditText winnerInput = findViewById(R.id.winnerInput);
        initSignButton(matchIDInput, winnerInput);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void nodeResponseHandler(Serializable response) {
        if (!(response instanceof FinalContractPacket))
            return;

        FinalContractPacket contractPacket = (FinalContractPacket) response;

        String contract =
                constructContract(
                        contractPacket.getWinner(),
                        contractPacket.getLoser(),
                        contractPacket.getContract()
                );

        KeyPair kp = loadKeyPair();
        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();

        TransactionPacket tp = new TransactionPacket(
                signContract(contract, privateKey),//signature
                contractPacket.getWinner(),
                contractPacket.getLoser(),
                contractPacket.getContract(),
                Base64.getEncoder().encodeToString(publicKey.getEncoded()),
                contract //contract
            );

        sendToNode(tp);
    }

    private String constructContract(String winner, String loser, String contract) {
        return " Player " + winner + " beat player " + loser + " " + contract + " ";
    }


    /**
     * Creates a button which generates match code
     *
     * @param matchIDOutput where to display the code
     */
    private void initCreateMatchButton(EditText matchIDOutput) {
        Button createMatch = findViewById(R.id.generateID);
        createMatch.setOnClickListener(view -> {
            // Creates a random six digit number
            String code = generateCode();

            // Displaying and sending code
            matchIDOutput.setText(code);
            sendToNode(new NewMatchPacket(code));
        });
    }


    /**
     * Creates button which handles code given by user
     *
     * @param matchIDInput where to fetch the code
     */
    private void initSignButton(EditText matchIDInput, EditText winnerInput) {
        Button signUmpireButton = findViewById(R.id.signUmpire);

        signUmpireButton.setOnClickListener(view -> {
            String matchCode = matchIDInput.getText().toString();
            String player = winnerInput.getText().toString();

            if (incompleteInput(matchCode, player))
                return;

            sendToNode(new RequestFinalContractPackage(matchCode, player));
            finish();
        });
    }


    private String generateCode() {
        return Integer.toString(
                ThreadLocalRandom
                        .current()
                        .nextInt(100000, 999999)
            );
    }
}