package com.example.weighttracker_ayala;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeightLogActivity extends AppCompatActivity {

    private static final int AVERAGE_WINDOW = 7;

    private DatabaseHelper db;
    private WeightAdapter adapter;
    private long userId;

    private TextView tvCurrentWeight;
    private TextView tvGoalWeight;
    private TextView tvMovingAverage;
    private TextView tvTrend;
    private TextView tvEmptyState;
    private MaterialCardView smsBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        // retrieve the logged-in user's ID passed from LoginActivity
        userId = getIntent().getLongExtra("USER_ID", -1);
        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoalWeight = findViewById(R.id.tvGoalWeight);
        tvMovingAverage = findViewById(R.id.tvMovingAverage);
        tvTrend = findViewById(R.id.tvTrend);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        smsBanner = findViewById(R.id.smsBanner);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WeightAdapter(new ArrayList<>(), new WeightAdapter.OnEntryActionListener() {
            @Override
            public void onDelete(long entryId) {
                db.deleteWeightEntry(entryId);
                refreshData();
            }

            @Override
            public void onEdit(WeightEntry entry) {
                // open AddEntryActivity in edit mode by passing the existing entry ID
                Intent intent = new Intent(WeightLogActivity.this, AddEntryActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("ENTRY_ID", entry.getId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fabAddEntry).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEntryActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // show banner prompting SMS setup if permission not yet granted
        findViewById(R.id.btnEnableSms).setOnClickListener(v ->
                startActivity(new Intent(this, SmsPermissionActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weight_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            openSettings();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh list and summary card whenever returning to this screen
        refreshData();
    }

    private void refreshData() {
        List<WeightEntry> entries = db.getWeightEntries(userId);
        adapter.setEntries(entries);

        double goal = db.getGoalWeight(userId);
        tvGoalWeight.setText(goal > 0
                ? String.format(Locale.US, "%.1f", goal)
                : getString(R.string.goal_not_set));

        // show most recent entry as current weight
        tvCurrentWeight.setText(entries.isEmpty()
                ? getString(R.string.goal_not_set)
                : String.format(Locale.US, "%.1f", entries.get(0).getWeight()));

        // moving average and trend come from WeightTrends
        tvMovingAverage.setText(entries.isEmpty()
                ? getString(R.string.goal_not_set)
                : String.format(Locale.US, "%.1f", WeightTrends.latestAverage(entries, AVERAGE_WINDOW)));
        tvTrend.setText(entries.isEmpty()
                ? getString(R.string.goal_not_set)
                : trendLabel(WeightTrends.trend(entries, AVERAGE_WINDOW)));

        tvEmptyState.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);

        // hide SMS banner once the user has granted permission
        boolean smsGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        smsBanner.setVisibility(smsGranted ? View.GONE : View.VISIBLE);
    }

    private String trendLabel(WeightTrends.Trend trend) {
        switch (trend) {
            case LOSING:
                return getString(R.string.trend_losing);
            case GAINING:
                return getString(R.string.trend_gaining);
            default:
                return getString(R.string.trend_holding);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    // clear the back stack so the user cannot navigate back to the log after logging out
    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
