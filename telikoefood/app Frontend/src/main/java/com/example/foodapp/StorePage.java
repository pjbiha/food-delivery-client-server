package com.example.foodapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class StorePage extends AppCompatActivity {
    //views gia plhrofories katastimatos
    TextView tvName, tvCategory, tvPriceRange, tvFoodItems;
    ArrayList<Product> productList = new ArrayList<>();
    //lista gia emfanisi proiontwn
    ListView listView;
    TextView totalText;
    LinearLayout orderLayout;
    Button orderButton;

    String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fortwnei to layout tis selidas
        setContentView(R.layout.activity_page);  

        //syndesi me ta views apo to layout
        tvName = findViewById(R.id.tv_name);
        tvCategory = findViewById(R.id.tv_category);
        tvPriceRange = findViewById(R.id.tv_price_range);
        tvFoodItems = findViewById(R.id.tv_food_items_label);

        //pairnei dedomena apo to intent
        Intent intent = getIntent();
        storeName = intent.getStringExtra("name");
        String category = intent.getStringExtra("category");
        String priceRange = intent.getStringExtra("price_range");
        ArrayList<Product> foodItems = (ArrayList<Product>)intent.getSerializableExtra("food_items");

        tvName.setText(storeName);
        tvCategory.setText("Κατηγορία: " + category);
        tvPriceRange.setText("Τιμή: " + priceRange);

        if (foodItems != null && !foodItems.isEmpty()) {
            for (Product food : foodItems) {
                //prosthetei ta proionta sth lista
                productList.add(food);
            }

            listView = findViewById(R.id.product_list);
            totalText = findViewById(R.id.total_items_text);
            orderLayout = findViewById(R.id.order_summary_layout);
            orderButton = findViewById(R.id.order_button);
            //koumpi gia rating
            Button rateButton = findViewById(R.id.button2);
            Button backButton = findViewById(R.id.backbutton);

            backButton.setOnClickListener(v -> {
                Toast.makeText(StorePage.this, "Αναζήτηση καταστημάτων...", Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    ArrayList<ShopInfo> shopList = new ArrayList<>();

                    try (Socket socket = new Socket("10.0.2.2", 5005)) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        String line;
                        boolean menuReached = false;
                        
                        while ((line = in.readLine()) != null) {
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
                        //diavasma katastimatwn apo server
                        while ((line = in.readLine()) != null) {
                            if (line.contains("Epilexe leitourgia")) break;

                            if (line.trim().isEmpty() || line.split(",").length < 7) continue;

                            try {
                                String[] tokens = line.split(",");
                                String name = tokens[0];
                                String category1 = tokens[1];
                                double lat = Double.parseDouble(tokens[2]);
                                double lon = Double.parseDouble(tokens[3]);
                                int stars = Integer.parseInt(tokens[4]);
                                int noOfReviews = Integer.parseInt(tokens[6]);
                                String price = tokens[5];

                                ShopInfo info = new ShopInfo(category1, price, new ArrayList<>(), stars, noOfReviews, lat, lon);
                                info.name = name;
                                shopList.add(info);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        runOnUiThread(() -> {
                            //epistrofi sto ShowActivity
                            Intent i = new Intent(StorePage.this, ShowActivity.class);
                            i.putExtra("shop_list", shopList);
                            startActivity(i);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(StorePage.this, "Αποτυχία σύνδεσης!", Toast.LENGTH_LONG).show());
                        e.printStackTrace();
                    }

                }).start();
            });
            rateButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(StorePage.this);
                builder.setTitle("Βαθμολόγησε το κατάστημα");

                final String[] starsOptions = {"1", "2", "3", "4", "5"};

                builder.setItems(starsOptions, (dialog, which) -> {
                    String selectedStars = starsOptions[which];

                    new Thread(() -> {
                        try {
                            Socket socket = new Socket("10.0.2.2", 5005);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            String line;
                            while ((line = in.readLine()) != null) {
                                if (line.contains("Epilexe leitourgia")) break;
                            }

                            out.println("4");
                            Thread.sleep(200);
                            out.println(storeName);
                            Thread.sleep(200);
                            out.println(selectedStars);
                            Thread.sleep(200);

                            socket.close();

                            runOnUiThread(() -> Toast.makeText(this,
                                    "Η βαθμολόγηση " + selectedStars + "★ στάλθηκε!", Toast.LENGTH_SHORT).show());

                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this,
                                    "Αποτυχία αποστολής βαθμολογίας!", Toast.LENGTH_LONG).show());
                            e.printStackTrace();
                        }
                    }).start();
                });
                //koumpi akyro
                builder.setNegativeButton("Άκυρο", null);
                builder.show();
            });
            ProductAdapter adapter = new ProductAdapter(this, productList, totalText, orderLayout);
            listView.setAdapter(adapter);

            orderButton.setOnClickListener(v -> {
                StringBuilder errors = new StringBuilder();
                boolean canProceed = true;

                for (Product p : productList) {
                    if (p.quantity > 0 && p.quantity > p.getAvailableAmount()) {
                        canProceed = false;
                        errors.append("Δεν υπάρχει αρκετό απόθεμα για: ")
                                .append(p.getName())
                                .append(" (Διαθέσιμο: ")
                                .append(p.getAvailableAmount())
                                .append(", Ζητήθηκαν: ")
                                .append(p.quantity)
                                .append(")\n");
                    }
                }

                if (!canProceed) {
                    new AlertDialog.Builder(this)
                            .setTitle("Σφάλμα παραγγελίας")
                            .setMessage(errors.toString())
                            .setPositiveButton("ΟΚ", null)
                            .show();
                    return;
                }

                //an ola einai entaksei ksekinaei apostoli paraggelias
                new Thread(() -> {
                    try {
                        Socket socket = new Socket("10.0.2.2", 5005);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        // περίμενε το μενού
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.contains("Epilexe leitourgia")) break;
                        }

                        for (Product p : productList) {
                            if (p.quantity > 0) {
                                out.println("3");
                                Thread.sleep(200);
                                out.println(storeName);
                                Thread.sleep(200);
                                out.println(p.getName());
                                Thread.sleep(200);
                                out.println(p.quantity);
                                Thread.sleep(200);
                                //upologismos neou apothematikou
                                int available = p.getAvailableAmount() - p.quantity;
                                p.setAvailableAmount(available);
                                p.quantity = 0;
                            }
                        }

                        socket.close();

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            orderLayout.setVisibility(v.GONE);
                            Toast.makeText(this, "Η παραγγελία καταχωρήθηκε!", Toast.LENGTH_LONG).show();
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Αποτυχία σύνδεσης με server!", Toast.LENGTH_LONG).show());
                        e.printStackTrace();
                    }
                }).start();

            });

        } else {
            tvFoodItems.setText("Δεν υπάρχουν διαθέσιμα φαγητά.");
        }
    }
}