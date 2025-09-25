package com.example.foodapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowActivity extends AppCompatActivity {
    //textview gia minima katastimatwn
    TextView showText;
    ListView listView;
    ArrayList<ShopInfo> shopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SHOW_ACTIVITY", "Εκκινήθηκε η ShowActivity");
        super.onCreate(savedInstanceState);
        //energopoiei edge to edge layout
        EdgeToEdge.enable(this);
        //fortwnei to layout ths selidas
        setContentView(R.layout.activity_show);
        //koumpi gia filtrarisma
        Button filterButton = findViewById(R.id.btn_filters);
        filterButton.setOnClickListener(v -> {
            new AlertDialog.Builder(ShowActivity.this)
                    .setTitle("Φίλτρα")
                    .setMessage("Θέλεις να αναζητήσεις για μαγαζιά σύμφωνα με απόσταση < 5km ή με custom φίλτρα;")
                    .setPositiveButton("Απόσταση", (dialog, which) -> applyFilters(true))
                    .setNegativeButton("Custom φίλτρα", (dialog, which) -> applyFilters(false))
                    .setNeutralButton("Άκυρο", null)
                    .show();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showText = findViewById(R.id.showText);
        //h lista me ta katastimata
        listView = findViewById(R.id.listView);
        //pairnei th lista me ta katastimata apo to intent
        shopList = (ArrayList<ShopInfo>) getIntent().getSerializableExtra("shop_list");

        if (shopList == null || shopList.isEmpty()) {
            shopList = new ArrayList<>();
            showText.setText("Κανένα κατάστημα δεν βρέθηκε");
        } else {
            showText.setText("Βρέθηκαν καταστήματα");
        }
        //adapter gia na ginei emfanisi katastimatwn sto listview
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return shopList.size();
            }

            @Override
            public Object getItem(int i) {
                return shopList.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                //fortwnei to layout tou kathe stoixeiou
                View v = LayoutInflater.from(ShowActivity.this).inflate(R.layout.list_item, viewGroup, false);
                TextView text = v.findViewById(R.id.list_item_text);
                TextView subText = v.findViewById(R.id.list_item_subtext);
                TextView reviewText = v.findViewById(R.id.list_item_reviews);
                ImageView image = v.findViewById(R.id.item_image);

                ShopInfo shop = shopList.get(i);

                text.setText(shop.name);
                subText.setText(shop.category + " " + shop.priceRange);
                reviewText.setText("★ " + shop.reviews + " (" + shop.noOfReviews + ")");

                switch (shop.category.toLowerCase()) {
                    case "souvlakia":
                        image.setImageResource(R.drawable.souvlaki);
                        break;
                    case "sushi":
                        image.setImageResource(R.drawable.sushi);
                        break;
                    default:
                        image.setImageResource(R.drawable.n_a);
                }

                return v;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            //debug gia click
            Log.d("SHOW_ACTIVITY", "Patithike to katastima sth thesi " + position);
            //to epilegmeno katastima
            ShopInfo selected = shopList.get(position);

            new Thread(() -> {
                ArrayList<Product> products = new ArrayList<>();
                //regex gia parsing grammwn proiontwn
                Pattern productPattern = Pattern.compile("^(.*?) \\| Price: ([0-9]+(?:\\.[0-9]+)?) euro \\| Stock: (\\d+)$");

                try {
                    Log.d("DEBUG", "Trying to connect to dummy socket");
                    Socket socket = new Socket("10.0.2.2", 5005);
                    Log.d("DEBUG", "Socket connected");

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.d("DEBUG", "Streams created");

                    String line;
                    boolean menuReached = false;

                    while ((line = in.readLine()) != null) {
                        Log.d("DUMMY_OUTPUT", line);
                        if (line.contains("Epilexe leitourgia")) {
                            menuReached = true;
                            break;
                        }
                    }

                    if (menuReached) {
                        Log.d("DEBUG", "Menu reached, sending command 2 and shop name");
                        //epilogi gia emfanisi proiontwn
                        out.println("2");
                        Thread.sleep(200);
                        out.println(selected.name);
                        Log.d("DEBUG", "Sent shop name: " + selected.name);
                    }

                    while ((line = in.readLine()) != null) {
                        Log.d("DEBUG", "Received: " + line);
                        if (line.contains("Epilexe leitourgia")) break;
                        //prospathoume na tairiaksoume to regex
                        Matcher matcher = productPattern.matcher(line);
                        if (matcher.matches()) {
                            try {
                                String pname = matcher.group(1).trim();
                                double cost = Double.parseDouble(matcher.group(2));
                                int available = Integer.parseInt(matcher.group(3));

                                Product p = new Product(pname, cost, available);
                                products.add(p);
                                Log.d("DEBUG", "Added product: " + pname);
                            } catch (Exception e) {
                                Log.e("PARSING", "Error parsing product line: " + line, e);
                            }
                        } else {
                            Log.d("PARSING", "Skipped unrelated line: " + line);
                        }
                    }

                    socket.close();
                    //metavasi sto storePage
                    runOnUiThread(() -> {
                        Intent intent = new Intent(ShowActivity.this, StorePage.class);
                        intent.putExtra("name", selected.name);
                        intent.putExtra("category", selected.category);
                        intent.putExtra("price_range", selected.priceRange);
                        intent.putExtra("reviews", selected.reviews);
                        intent.putExtra("noOfReviews", selected.noOfReviews);
                        intent.putExtra("food_items", products);
                        startActivity(intent);
                    });

                } catch (Exception e) {
                    Log.e("SOCKET_ERROR", "Error connecting to server", e);
                }

            }).start();
        });
    }
    private void applyFilters(boolean useDistance) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("10.0.2.2", 5005);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Περίμενε το μενού
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("Epilexe leitourgia")) break;
                }

                out.println("1"); 
                Thread.sleep(200);

                if (useDistance) {
                    //an epilexthike apostasi, zhtaei plhthos/geo
                    runOnUiThread(() -> {
                        View inputView = LayoutInflater.from(ShowActivity.this).inflate(R.layout.dialog_location, null);
                        EditText inputLat = inputView.findViewById(R.id.input_lat);
                        EditText inputLon = inputView.findViewById(R.id.input_lon);

                        new AlertDialog.Builder(ShowActivity.this)
                                .setTitle("Τοποθεσία Χρήστη")
                                .setMessage("Δώσε γεωγραφικό πλάτος και μήκος:")
                                .setView(inputView)
                                .setPositiveButton("Αναζήτηση", (dialog, which1) -> {
                                    String lat = inputLat.getText().toString().trim();
                                    String lon = inputLon.getText().toString().trim();

                                    new Thread(() -> {
                                        try {
                                            out.println("1");
                                            Thread.sleep(200);
                                            out.println(lat); 
                                            Thread.sleep(200);
                                            out.println(lon); 
                                            //diavazoume ta nea
                                            ArrayList<ShopInfo> newShops = fetchShops(in);
                                            socket.close();

                                            runOnUiThread(() -> restartWithResults(newShops));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Άκυρο", null)
                                .show();
                    });
                } else {
                    //custom filters
                    runOnUiThread(() -> {
                        View inputView = LayoutInflater.from(ShowActivity.this).inflate(R.layout.dialog_custom_filters, null);
                        EditText inputCat = inputView.findViewById(R.id.input_category);
                        EditText inputStars = inputView.findViewById(R.id.input_stars);
                        EditText inputPrice = inputView.findViewById(R.id.input_price);

                        new AlertDialog.Builder(ShowActivity.this)
                                .setTitle("Custom Φίλτρα")
                                .setMessage("Συμπλήρωσε τα παρακάτω φίλτρα:")
                                .setView(inputView)
                                .setPositiveButton("Αναζήτηση", (dialog, which2) -> {
                                    String cat = inputCat.getText().toString().trim();
                                    String stars = inputStars.getText().toString().trim();
                                    String price = inputPrice.getText().toString().trim();

                                    new Thread(() -> {
                                        try {
                                            out.println("2");
                                            Thread.sleep(200);
                                            out.println(cat);
                                            Thread.sleep(200);
                                            out.println(stars);
                                            Thread.sleep(200);
                                            out.println(price);

                                            ArrayList<ShopInfo> newShops = fetchShops(in);
                                            socket.close();

                                            runOnUiThread(() -> restartWithResults(newShops));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                })
                                .setNegativeButton("Άκυρο", null)
                                .show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private ArrayList<ShopInfo> fetchShops(BufferedReader in) throws Exception {
        ArrayList<ShopInfo> list = new ArrayList<>();
        String line;
        //diavazei katasthmata apo to stream
        while ((line = in.readLine()) != null) {
            if (line.contains("Epilexe leitourgia")) break;
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

                ShopInfo info = new ShopInfo(category, price, new ArrayList<>(), stars, noOfReviews, lat, lon);
                info.name = name;
                list.add(info);
            } catch (Exception ex) {
                Log.e("PARSING", "Error parsing: " + line, ex);
            }
        }

        return list;
    }
    //xrhsimopoieitai gia na ksanafortwthei h ShowActivity me nea apotelesmata
    private void restartWithResults(ArrayList<ShopInfo> results) {
        Intent intent = new Intent(ShowActivity.this, ShowActivity.class);
        intent.putExtra("shop_list", results);
        startActivity(intent);
        finish();
    }

}
