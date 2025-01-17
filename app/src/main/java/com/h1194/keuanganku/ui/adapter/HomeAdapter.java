package com.h1194.keuanganku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.h1194.keuanganku.R;
import com.h1194.keuanganku.data.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private final List<Transaction> data;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public HomeAdapter(List<Transaction> data) {
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView amountTextView;
        TextView dateTextView;

        public ViewHolder(View view) {
            super(view);
            typeTextView = view.findViewById(R.id.tvType);
            amountTextView = view.findViewById(R.id.tvAmount);
            dateTextView = view.findViewById(R.id.tvDate);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardhome, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = data.get(position);
        holder.typeTextView.setText(transaction.getType());

        String formattedAmount = currencyFormatter.format(transaction.getAmount()).replace("IDR", "Rp");
        holder.amountTextView.setText(formattedAmount);

        holder.dateTextView.setText(dateFormatter.format(transaction.getDate()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}