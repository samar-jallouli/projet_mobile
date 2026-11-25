package com.clubsync.ui.admin;

import android.graphics.Color;
import android.os.Bundle;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityAnalyticsBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.util.Constants;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends BaseActivity {

    private ActivityAnalyticsBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_stats);
        configureChartsAppearance();
        loadAnalyticsData();
    }

    private void configureChartsAppearance() {
        // PieChart configuration
        binding.pieChartDepartments.getDescription().setEnabled(false);
        binding.pieChartDepartments.getLegend().setEnabled(false);
        binding.pieChartDepartments.setDrawHoleEnabled(true);
        binding.pieChartDepartments.setHoleColor(Color.parseColor("#12182B"));
        binding.pieChartDepartments.setHoleRadius(65f);
        binding.pieChartDepartments.setTransparentCircleRadius(70f);
        binding.pieChartDepartments.setDrawEntryLabels(false);

        // BarChart configuration
        binding.barChartConversion.getDescription().setEnabled(false);
        binding.barChartConversion.getLegend().setEnabled(false);
        binding.barChartConversion.setDrawGridBackground(false);
        binding.barChartConversion.setDrawBarShadow(false);
        binding.barChartConversion.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.barChartConversion.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#A5B0D0"));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"ACTIF", "EN_ATTENTE"}));
        xAxis.setGranularity(1f);

        binding.barChartConversion.getAxisLeft().setTextColor(Color.parseColor("#A5B0D0"));
        binding.barChartConversion.getAxisLeft().setDrawGridLines(false);
        binding.barChartConversion.getAxisLeft().setAxisMinimum(0f);
    }

    private void loadAnalyticsData() {
        repository.getMembers(new Callback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> members) {
                if (members == null) members = new ArrayList<>();
                updateDepartmentChart(members);
                updateConversionChart(members);
            }

            @Override
            public void onError(String error) {
                showError("Erreur de calcul des statistiques : " + error);
            }
        });
    }

    private void updateDepartmentChart(List<Member> members) {
        int tech = 0, com = 0, event = 0, rh = 0;
        for (Member m : members) {
            String dept = m.getDepartment();
            if (Constants.DEPT_TECH.equalsIgnoreCase(dept)) tech++;
            else if (Constants.DEPT_COM.equalsIgnoreCase(dept)) com++;
            else if (Constants.DEPT_EVENT.equalsIgnoreCase(dept)) event++;
            else if (Constants.DEPT_RH.equalsIgnoreCase(dept)) rh++;
        }

        int total = tech + com + event + rh;
        if (total == 0) {
            tech = 1; total = 1; // Valeur par défaut si vide
        }

        List<PieEntry> entries = new ArrayList<>();
        if (tech > 0) entries.add(new PieEntry(tech, "Tech"));
        if (com > 0) entries.add(new PieEntry(com, "Com"));
        if (event > 0) entries.add(new PieEntry(event, "Event"));
        if (rh > 0) entries.add(new PieEntry(rh, "RH"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#6B4EFF")); // Tech Violet
        colors.add(Color.parseColor("#22D3EE")); // Com Cyan
        colors.add(Color.parseColor("#FCD34D")); // Event Ambre
        colors.add(Color.parseColor("#F9A8D4")); // RH Rose
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        binding.pieChartDepartments.setData(pieData);
        binding.pieChartDepartments.invalidate();

        // Mise à jour de la légende textuelle
        int techPct = (int) Math.round((tech * 100.0) / total);
        int comPct = (int) Math.round((com * 100.0) / total);
        int eventPct = (int) Math.round((event * 100.0) / total);
        int rhPct = (int) Math.round((rh * 100.0) / total);

        binding.tvLegendTech.setText("Tech · " + techPct + " %");
        binding.tvLegendCom.setText("Com · " + comPct + " %");
        binding.tvLegendEvent.setText("Event · " + eventPct + " %");
        binding.tvLegendRH.setText("RH · " + rhPct + " %");
    }

    private void updateConversionChart(List<Member> members) {
        int actif = 0, pending = 0;
        for (Member m : members) {
            if (Constants.STATUS_PENDING.equalsIgnoreCase(m.getStatus())) {
                pending++;
            } else {
                actif++;
            }
        }

        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, actif));
        barEntries.add(new BarEntry(1f, pending));

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(new int[]{Color.parseColor("#6B4EFF"), Color.parseColor("#FCD34D")});
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(14f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);
        binding.barChartConversion.setData(barData);
        binding.barChartConversion.invalidate();

        int total = actif + pending;
        int rate = total > 0 ? (int) Math.round((actif * 100.0) / total) : 0;
        binding.tvConversionRate.setText(rate + " %");
    }
}
