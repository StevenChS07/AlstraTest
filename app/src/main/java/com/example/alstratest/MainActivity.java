package com.example.alstratest;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private native String getDeviceIpAddress();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        final TextView textView = findViewById(R.id.textView);
        final ImageView imageView = findViewById(R.id.imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = getDeviceIpAddress();
                textView.setText(ipAddress);
                sendIpAddressToServer(ipAddress, textView, imageView);
            }
        });
    }

    private void sendIpAddressToServer(final String ipAddress, final TextView textView, final ImageView imageView) {
        final String[] msg = {""};
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("Please wait...");
                imageView.setVisibility(View.GONE);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("address", ipAddress);
                    URL url = new URL("https://s7om3fdgbt7lcvqdnxitjmtiim0uczux.lambda-url.us-east-2.on.aws/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = json.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int code = conn.getResponseCode();
                    StringBuilder response = new StringBuilder();

                    if (code == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                    } else {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        String responseMessage = response.toString();
                        msg[0] = responseMessage;
                        in.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Hide the loading message
                            textView.setText(ipAddress + " " + msg[0]);
                            if (Objects.equals(msg[0], "true")) {
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setBackgroundColor(Color.GREEN);
                            }else {
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setBackgroundColor(Color.RED);
                            }
                            // Update the UI based on the server response
                        }
                    });
                }
            }
        }).start();
    }

}