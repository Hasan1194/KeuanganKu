package com.h1194.keuanganku.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.h1194.keuanganku.ui.adapter.HistoryAdapter;
import com.h1194.keuanganku.R;
import com.h1194.keuanganku.data.AppDatabase;
import com.h1194.keuanganku.data.Transaction;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private HistoryAdapter historyAdapter;
    private RecyclerView recyclerView;
    private AppDatabase db;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvStory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getDatabase(requireContext());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Transaction> transactions = db.transactionDao().getAllTransactions();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        historyAdapter = new HistoryAdapter(transactions, HistoryFragment.this::deleteTransaction);
                        recyclerView.setAdapter(historyAdapter);
                    }
                });
            }
        });

        return view;
    }

    private void deleteTransaction(Transaction transaction) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                db.transactionDao().deleteTransaction(transaction);

                List<Transaction> updatedTransactions = db.transactionDao().getAllTransactions();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        historyAdapter.updateData(updatedTransactions);
                    }
                });
            }
        });
    }
}