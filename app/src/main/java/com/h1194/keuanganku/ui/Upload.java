package com.h1194.keuanganku.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.h1194.keuanganku.R;
import com.h1194.keuanganku.data.AppDatabase;
import com.h1194.keuanganku.data.Transaction;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Upload extends AppCompatActivity {

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Spinner spinnerType = findViewById(R.id.spinnerType);
        EditText etAmount = findViewById(R.id.etAmount);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        String[] types = {"Pendapatan", "Pengeluaran"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(adapter);

        AppDatabase db = AppDatabase.getDatabase(this);

        executorService = Executors.newSingleThreadExecutor();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = spinnerType.getSelectedItem().toString();
                String amountStr = etAmount.getText().toString();
                Float amount = null;

                try {
                    amount = Float.parseFloat(amountStr);
                } catch (NumberFormatException e) {

                }

                if (amount != null) {
                    Date currentDate = new Date();
                    Transaction transaction = new Transaction(type, amount, currentDate);

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            db.transactionDao().insertTransaction(transaction);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Upload.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                                    etAmount.getText().clear();
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(Upload.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
