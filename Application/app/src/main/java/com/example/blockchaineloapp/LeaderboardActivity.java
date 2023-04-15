package com.example.blockchaineloapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import shared.packets.LeaderboardPacket;
import shared.packets.PkEloPair;
import shared.packets.RequestLeaderboardPacket;

public class LeaderboardActivity extends SigningActivity {

    private ListView leaderboardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        setTitle("Leaderboard");
        leaderboardList = findViewById(R.id.list);

        sendToNode(new RequestLeaderboardPacket());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void nodeResponseHandler(Serializable response) {
        if (!(response instanceof LeaderboardPacket)){
            return;}

        List<PkEloPair> leaderboard
                = ((LeaderboardPacket) response).getLeaderboard();

        List<String> prettyLeaderboard = new ArrayList<>();

        KeyPair keyPair = loadKeyPair();

        PublicKey pk = keyPair.getPublic();

        String PKstring = Base64.getEncoder().encodeToString(pk.getEncoded());

        leaderboard.forEach( (l) -> {
            if (l.getPk().equals(PKstring)){
                prettyLeaderboard.add(l.getShortPk() + "  " + l.getElo() + " <- ( Vous )");
            }
            else {
                prettyLeaderboard.add(l.getShortPk() + "  " + l.getElo());
            }
        });


        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, prettyLeaderboard);

        runOnUiThread(() -> leaderboardList.setAdapter(adapter));
    }
}