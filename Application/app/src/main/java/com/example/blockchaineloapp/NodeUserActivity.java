package com.example.blockchaineloapp;

import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NodeClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;

public abstract class NodeUserActivity extends AppCompatActivity {

    protected void makeToast(String message) {
        Toast toast = Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        );
        toast.show();
    }

    protected void sendToNode(Serializable message) {
        Thread t1 = new Thread(() -> {
            Looper.prepare();
            try {
                nodeResponseHandler(NodeClient.send(message));

            } catch (ConnectException ce) {
                runOnUiThread(() -> makeToast("Connection failed"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t1.start();
    }

    protected abstract void nodeResponseHandler(Serializable response);
}
