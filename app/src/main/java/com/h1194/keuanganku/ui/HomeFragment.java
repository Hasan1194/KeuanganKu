package com.h1194.keuanganku.ui;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.h1194.keuanganku.R;
import com.h1194.keuanganku.data.AppDatabase;
import com.h1194.keuanganku.data.Transaction;
import com.h1194.keuanganku.databinding.FragmentHomeBinding;
import com.h1194.keuanganku.ui.adapter.HomeAdapter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AppDatabase db;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = AppDatabase.getDatabase(requireContext());

        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Transaction> transactions = db.transactionDao().getAllTransactions();

            requireActivity().runOnUiThread(() -> {
                try {
                    if (transactions != null && !transactions.isEmpty()) {
                        HomeAdapter adapter = new HomeAdapter(transactions);
                        binding.recyclerViewHistory.setAdapter(adapter);
                    }
                    setupLineChart(binding.lineChart);
                    setupProfitCard();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        return view;
    }

    private void setupLineChart(LineChart lineChart) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", new Locale("id"));
                List<String> months = new ArrayList<>();
                List<Entry> pendapatanEntries = new ArrayList<>();
                List<Entry> pengeluaranEntries = new ArrayList<>();

                for (int i = 5; i >= 0; i--) {
                    calendar.add(Calendar.MONTH, -i);
                    String monthStr = monthFormat.format(calendar.getTime());
                    months.add(monthStr);

                    Calendar monthStart = (Calendar) calendar.clone();
                    monthStart.set(Calendar.DAY_OF_MONTH, 1);
                    Calendar monthEnd = (Calendar) calendar.clone();
                    monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

                    Float pendapatan = db.transactionDao().getTotalByTypeAndDateRange("Pendapatan", monthStart.getTime(), monthEnd.getTime());
                    Float pengeluaran = db.transactionDao().getTotalByTypeAndDateRange("Pengeluaran", monthStart.getTime(), monthEnd.getTime());

                    pendapatanEntries.add(new Entry(5 - i, pendapatan != null ? pendapatan : 0f));
                    pengeluaranEntries.add(new Entry(5 - i, pengeluaran != null ? pengeluaran : 0f));

                    calendar.add(Calendar.MONTH, i);
                }

                requireActivity().runOnUiThread(() -> {
                    LineDataSet pendapatanDataSet = new LineDataSet(pendapatanEntries, "Pendapatan");
                    pendapatanDataSet.setColor(getResources().getColor(R.color.green));
                    pendapatanDataSet.setCircleColor(getResources().getColor(R.color.green));
                    pendapatanDataSet.setValueTextColor(getResources().getColor(R.color.white));
                    pendapatanDataSet.setValueTextSize(12f);
                    pendapatanDataSet.setLineWidth(2f);

                    LineDataSet pengeluaranDataSet = new LineDataSet(pengeluaranEntries, "Pengeluaran");
                    pengeluaranDataSet.setColor(getResources().getColor(R.color.red));
                    pengeluaranDataSet.setCircleColor(getResources().getColor(R.color.red));
                    pengeluaranDataSet.setValueTextColor(getResources().getColor(R.color.white));
                    pengeluaranDataSet.setValueTextSize(12f);
                    pengeluaranDataSet.setLineWidth(2f);

                    lineChart.setData(new LineData(pendapatanDataSet, pengeluaranDataSet));
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setTextColor(getResources().getColor(R.color.white));

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
                    xAxis.setTextColor(getResources().getColor(R.color.white));
                    xAxis.setGridColor(getResources().getColor(R.color.white));
                    xAxis.setGranularity(1f);

                    lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.white));
                    lineChart.getAxisLeft().setGridColor(getResources().getColor(R.color.white));
                    lineChart.getAxisRight().setEnabled(false);

                    lineChart.setTouchEnabled(true);
                    lineChart.setDragEnabled(true);
                    lineChart.setScaleEnabled(true);

                    lineChart.animateX(1000);
                    lineChart.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error setting up chart: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupProfitCard() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = calendar.getTime();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date monthEnd = calendar.getTime();

                Float totalPendapatan = db.transactionDao().getTotalByTypeAndDateRange("Pendapatan", monthStart, monthEnd);
                Float totalPengeluaran = db.transactionDao().getTotalByTypeAndDateRange("Pengeluaran", monthStart, monthEnd);

                float pendapatan = totalPendapatan != null ? totalPendapatan : 0f;
                float pengeluaran = totalPengeluaran != null ? totalPengeluaran : 0f;
                float profit = pendapatan - pengeluaran;

                float finalPendapatan = pendapatan;
                float finalPengeluaran = pengeluaran;
                float finalProfit = profit;

                requireActivity().runOnUiThread(() -> {
                    String formattedProfit = currencyFormatter.format(finalProfit).replace("IDR", "Rp");
                    binding.textViewProfit.setText("Keuntungan Bulan Ini: " + formattedProfit);

                    binding.buttonPrint.setOnClickListener(v -> {
                        ExecutorService printExecutor = Executors.newSingleThreadExecutor();
                        printExecutor.execute(() -> {
                            exportToExcel(finalPendapatan, finalPengeluaran, finalProfit);
                        });
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void exportToExcel(float pendapatan, float pengeluaran, float profit) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Laporan Keuangan");

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Kategori");
            headerRow.createCell(1).setCellValue("Jumlah (Rp)");

            org.apache.poi.ss.usermodel.Row pendapatanRow = sheet.createRow(1);
            pendapatanRow.createCell(0).setCellValue("Pendapatan");
            pendapatanRow.createCell(1).setCellValue(pendapatan);

            org.apache.poi.ss.usermodel.Row pengeluaranRow = sheet.createRow(2);
            pengeluaranRow.createCell(0).setCellValue("Pengeluaran");
            pengeluaranRow.createCell(1).setCellValue(pengeluaran);

            org.apache.poi.ss.usermodel.Row profitRow = sheet.createRow(3);
            profitRow.createCell(0).setCellValue("Keuntungan");
            profitRow.createCell(1).setCellValue(profit);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", new Locale("id"));
            String fileName = "Laporan_Keuangan_" + dateFormat.format(new Date()) + ".xlsx";

            File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Laporan berhasil disimpan di: " + file.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Gagal mencetak laporan: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}