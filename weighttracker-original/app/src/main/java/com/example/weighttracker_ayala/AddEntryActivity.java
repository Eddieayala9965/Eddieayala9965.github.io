package com.example.weighttracker_ayala;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEntryActivity extends AppCompatActivity {

    static final String EXTRA_ENTRY_ID = "ENTRY_ID";

    private DatabaseHelper db;
    private long userId;
    private long editEntryId = -1; // -1 means new entry, any other value means edit mode

    private TextInputEditText etDate;
    private TextInputEditText etWeight;
    private TextInputEditText etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        userId = getIntent().getLongExtra("USER_ID", -1);
        editEntryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddEntry);
        toolbar.setNavigationOnClickListener(v -> finish());

        // change toolbar title when editing an existing entry
        if (editEntryId != -1) {
            toolbar.setTitle(getString(R.string.screen_edit_entry));
        }

        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        etNotes = findViewById(R.id.etNotes);

        etDate.setOnClickListener(v -> showDatePicker());

        if (editEntryId != -1) {
            populateFieldsForEdit();
        } else {
            // default to today's date for new entries
            etDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    .format(Calendar.getInstance().getTime()));
        }

        findViewById(R.id.btnSaveEntry).setOnClickListener(v -> saveEntry());
        findViewById(R.id.btnCancelEntry).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String formatted = String.format(Locale.US, "%02d/%02d/%04d", month + 1, day, year);
            etDate.setText(formatted);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // load existing values into fields when editing
    private void populateFieldsForEdit() {
        WeightEntry existing = db.getWeightEntry(editEntryId);
        if (existing != null) {
            etDate.setText(existing.getDate());
            etWeight.setText(String.format(Locale.US, "%.1f", existing.getWeight()));
            etNotes.setText(existing.getNotes());
        }
    }

    private void saveEntry() {
        String date = getText(etDate);
        String weightStr = getText(etWeight);
        String notes = getText(etNotes);

        if (date.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Date and weight are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editEntryId != -1) {
            db.updateWeightEntry(editEntryId, date, weight, notes);
        } else {
            db.addWeightEntry(userId, date, weight, notes);
        }

        checkGoalAndNotify(weight);
        finish();
    }

    // send SMS alert if the user hit their goal weight and permission is granted
    private void checkGoalAndNotify(double weight) {
        double goal = db.getGoalWeight(userId);
        if (goal <= 0 || weight > goal) {
            return;
        }

        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            return;
        }

        String phone = db.getPhoneNumber(userId);
        if (phone.isEmpty()) {
            return;
        }

        try {
            // SmsManager.getDefault() is deprecated on API 31+, use getSystemService instead
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            String message = "You reached your goal weight of " + goal + " lbs! Great work!";
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(this, "Goal reached! SMS notification sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not send SMS notification.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }
}
