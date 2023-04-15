package com.example.blockchaineloapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.network.NodeClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

import shared.packets.PlayerScore;
import shared.packets.PlayerScoreRequest;

public class UserScore extends SigningActivity {

    private EditText scoreDisplay;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_score);

        setTitle("Votre score");
        associateActivity(R.id.leaderboard, LeaderboardActivity.class);


        scoreDisplay = findViewById(R.id.scoreDisplay);

        requestScore();
    }

    @Override
    protected void nodeResponseHandler(Serializable response) {
        if (response instanceof PlayerScore)
            scoreDisplay.setText(((PlayerScore)response).getScore());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestScore() {
        KeyPair kp = loadKeyPair();
        PublicKey publicKey = kp.getPublic();
        sendToNode(new PlayerScoreRequest(Base64.getEncoder().encodeToString(publicKey.getEncoded())));
    }

    protected void associateActivity(int buttonID, Class<?> activity) {
        Button button = findViewById(buttonID);
        button.setOnClickListener(view -> launchActivity(activity));
    }

    protected void launchActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}