package com.example.weighttracker_ayala;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private long userId;

    private TextInputEditText etGoalWeight;
    private TextInputEditText etPhoneNumber;
    private SwitchMaterial switchSms;
    private TextView tvPermissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userId = getIntent().getLongExtra("USER_ID", -1);
        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        etGoalWeight = findViewById(R.id.etGoalWeight);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        switchSms = findViewById(R.id.switchSmsNotifications);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);

        // pre-fill fields with saved values from the database
        double goal = db.getGoalWeight(userId);
        if (goal > 0) {
            etGoalWeight.setText(String.format(Locale.US, "%.1f", goal));
        }
        etPhoneNumber.setText(db.getPhoneNumber(userId));

        findViewById(R.id.btnSaveGoal).setOnClickListener(v -> saveGoalWeight());
        findViewById(R.id.btnSavePhone).setOnClickListener(v -> savePhoneNumber());
        findViewById(R.id.btnDone).setOnClickListener(v -> finish());

        // redirect to permission screen if user tries to enable SMS without granting permission
        switchSms.setOnCheckedChangeListener((btn, isChecked) -> {
            boolean alreadyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED;
            if (isChecked && !alreadyGranted) {
                switchSms.setChecked(false);
                startActivity(new Intent(this, SmsPermissionActivity.class));
            }
        });

        // log out and clear the back stack so the user cannot return to the app without logging in
        findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // sync toggle state in case user granted/denied permission from another screen
        syncSmsToggle();
    }

    private void syncSmsToggle() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        // temporarily remove listener to avoid triggering it during programmatic toggle
        switchSms.setOnCheckedChangeListener(null);
        switchSms.setChecked(granted);
        switchSms.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked && !granted) {
                switchSms.setChecked(false);
                startActivity(new Intent(this, SmsPermissionActivity.class));
            }
        });

        tvPermissionStatus.setText(granted
                ? getString(R.string.sms_permission_granted)
                : getString(R.string.sms_permission_not_granted));
    }

    private void saveGoalWeight() {
        String input = etGoalWeight.getText() != null ? etGoalWeight.getText().toString().trim() : "";
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a goal weight.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double goal = Double.parseDouble(input);
            db.updateGoalWeight(userId, goal);
            Toast.makeText(this, "Goal weight saved.", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePhoneNumber() {
        String phone = etPhoneNumber.getText() != null ? etPhoneNumber.getText().toString().trim() : "";
        db.updatePhoneNumber(userId, phone);
        Toast.makeText(this, "Phone number saved.", Toast.LENGTH_SHORT).show();
    }
}
