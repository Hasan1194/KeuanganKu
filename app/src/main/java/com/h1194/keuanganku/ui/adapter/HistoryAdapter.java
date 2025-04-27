package com.h1194.keuanganku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.h1194.keuanganku.R;
import com.h1194.keuanganku.data.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Transaction> data;
    private final OnDeleteClickListener onDeleteClick;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public interface OnDeleteClickListener {
        void onDeleteClick(Transaction transaction);
    }

    public HistoryAdapter(List<Transaction> data, OnDeleteClickListener onDeleteClick) {
        this.data = data;
        this.onDeleteClick = onDeleteClick;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView typeTextView;
        public TextView amountTextView;
        public TextView dateTextView;
        public FloatingActionButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            typeTextView = view.findViewById(R.id.tvType);
            amountTextView = view.findViewById(R.id.tvAmount);
            dateTextView = view.findViewById(R.id.tvDate);
            deleteButton = view.findViewById(R.id.btn_delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = data.get(position);
        holder.typeTextView.setText(transaction.getType());

        String formattedAmount = currencyFormatter.format(transaction.getAmount()).replace("IDR", "Rp");
        holder.amountTextView.setText(formattedAmount);

        holder.dateTextView.setText(dateFormatter.format(transaction.getDate()));

        holder.deleteButton.setOnClickListener(v -> onDeleteClick.onDeleteClick(transaction));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<Transaction> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }
}