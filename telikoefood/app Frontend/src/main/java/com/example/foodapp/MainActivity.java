package com.example.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView text;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //energopoiei thn edge-to-edge emfanish
        EdgeToEdge.enable(this);
        //fortwnei to layout activity_main
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //koumpia
        text = findViewById(R.id.textView);
        btn = findViewById(R.id.button);
        //otan patietai to koumpi, trexei to parakato thread
        btn.setOnClickListener(view -> {
            new Thread(() -> {
                //lista gia ta katasthmata pou tha erthoun apo ton server
                ArrayList<ShopInfo> shopList = new ArrayList<>();

                try (Socket socket = new Socket("10.0.2.2", 5005)) {
                    //anoigei socket me thn dieythynsi 10.0.2.2 (host machine apo emulator) kai porta 5005
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String line;
                    //flag an ftasame sto menu
                    boolean menuReached = false;
                    //emfanizei kathe grammh sto log
                    while ((line = in.readLine()) != null) {
                        Log.d("DUMMY_OUTPUT", line);
                        if (line.contains("Epilexe leitourgia")) {
                            menuReached = true;
                            break;
                        }
                    }

                    if (menuReached) {
                        out.println("1");
                        Thread.sleep(200);
                        out.println("2");
                        Thread.sleep(200);
                        out.println("0");
                        Thread.sleep(200);
                        out.println("0");
                        Thread.sleep(200);
                        out.println("0");
                    }

                    while ((line = in.readLine()) != null) {
                        Log.d("DUMMY_OUTPUT", line);
                        if (line.contains("Epilexe leitourgia")) break;
                        //an h grammh einai keni h exei lathos plhthos stoixeiwn, proxwraei stin epomeni
                        if (line.trim().isEmpty() || line.split(",").length < 7) continue;

                        try {
                            String[] tokens = line.split(",");
                            String name = tokens[0];
                            String category = tokens[1];
                            double lat = Double.parseDouble(tokens[2]);
                            double lon = Double.parseDouble(tokens[3]);
                            int stars = Integer.parseInt(tokens[4]);
                            int noOfReviews = Integer.parseInt(tokens[6]);
                            String price = tokens[5];
                            //ftiaxnei neo antikeimeno ShopInfo
                            ShopInfo info = new ShopInfo(category, price, new ArrayList<>(), stars, noOfReviews, lat, lon);
                            info.name = name;
                            shopList.add(info);
                        } catch (Exception ex) {
                            Log.e("PARSING", "Error parsing line: " + line, ex);
                        }
                    }
                    //metaferei thn leitourgia sto UI thread gia na kanei allagh activity
                    runOnUiThread(() -> {
                        Log.d("MAIN", "Metavasi sto ShowActivity me " + shopList.size() + " katasthmata");
                        //dimiourgei intent gia metavasi sto ShowActivity
                        Intent i = new Intent(MainActivity.this, ShowActivity.class);
                        i.putExtra("shop_list", shopList);
                        startActivity(i);
                    });

                } catch (Exception e) {
                    Log.e("ERROR", "Socket error: " + e.getMessage(), e);
                }
            }).start();

            Toast.makeText(MainActivity.this, "Αναζήτηση καταστημάτων...", Toast.LENGTH_SHORT).show();
        });
    }
}