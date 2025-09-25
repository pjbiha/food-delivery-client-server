package com.example.foodapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductAdapter extends BaseAdapter {
    //to context tou activity pou xrhsimopoiei to adapter
    Context context;
    //lista me ta proionta pou emfanizontai
    ArrayList<Product> productList;
    //textview pou deixnei to synoliko kostos
    TextView totalText;
    //layout pou emfanizetai mono an yparxei paraggelia
    View orderLayout;

    public ProductAdapter(Context context, ArrayList<Product> productList, TextView totalText, View orderLayout) {
        this.context = context;
        this.productList = productList;
        this.totalText = totalText;
        this.orderLayout = orderLayout;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int i) {
        return productList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //dimiourgei kai epistrefei to view gia kathe stoixeio tis listas
    public View getView(int i, View convertView, ViewGroup parent) {
        //fortwnei to layout pou periexei ta stoixeia kathe proiontos
        View v = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        Product product = productList.get(i);

        TextView name = v.findViewById(R.id.product_name);
        TextView count = v.findViewById(R.id.product_count);
        Button plus = v.findViewById(R.id.btn_plus);
        Button minus = v.findViewById(R.id.btn_minus);
        //emfanizei onoma kai timi tou proiontos
        name.setText(product.getName() + "\n"+ product.getPrice() + " €");
        //emfanizei tin posothta pou exei epilexei o xrhsths
        count.setText(String.valueOf(product.quantity));

        plus.setOnClickListener(view -> {
            product.quantity++;
            //kanei refresh to adapter gia na emfanisei allagh
            notifyDataSetChanged();
            updateSummary();
        });

        minus.setOnClickListener(view -> {
            if (product.quantity > 0) {
                product.quantity--;
                notifyDataSetChanged();
                updateSummary();
            }
        });
        //epistrefei to view pou tha emfanistei sth lista
        return v;
    }
    //ypologizei kai emfanizei to synoliko kostos paraggelias
    private void updateSummary() {
        double total = 0.0;
        for (Product p : productList) {
            total += p.quantity * p.getPrice();
        }

        if (total > 0) {
            orderLayout.setVisibility(View.VISIBLE);
            totalText.setText("Σύνολο: " + total + " €");
        } else {
            orderLayout.setVisibility(View.GONE);
        }
    }
}

