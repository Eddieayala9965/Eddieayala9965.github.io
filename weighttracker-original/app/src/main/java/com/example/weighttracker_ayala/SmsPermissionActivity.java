package com.example.weighttracker_ayala;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SMS = 101;

    private TextView tvPermissionStatus;
    private MaterialCardView cardPermissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSms);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        cardPermissionStatus = findViewById(R.id.cardPermissionStatus);

        findViewById(R.id.btnGrantPermission).setOnClickListener(v -> requestSmsPermission());
        // deny button closes the screen and the app continues without SMS
        findViewById(R.id.btnDenyPermission).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatusCard();
    }

    private void requestSmsPermission() {
        // skip the request if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            refreshStatusCard();
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                REQUEST_CODE_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SMS) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            refreshStatusCard();
            if (granted) {
                Toast.makeText(this, "SMS notifications enabled!", Toast.LENGTH_SHORT).show();
                // auto-close after a short delay so the user sees the confirmation
                new android.os.Handler().postDelayed(this::finish, 1200);
            } else {
                // app continues to function normally without SMS
                Toast.makeText(this,
                        "SMS permission denied. The app will work without notifications.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // update the status card color and text to reflect current permission state
    private void refreshStatusCard() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (granted) {
            tvPermissionStatus.setText(getString(R.string.sms_status_granted));
            tvPermissionStatus.setTextColor(
                    getResources().getColor(R.color.success_color, getTheme()));
            cardPermissionStatus.setCardBackgroundColor(
                    getResources().getColor(R.color.success_light, getTheme()));
        } else {
            tvPermissionStatus.setText(getString(R.string.sms_status_not_granted));
            tvPermissionStatus.setTextColor(
                    getResources().getColor(R.color.warning_dark, getTheme()));
            cardPermissionStatus.setCardBackgroundColor(
                    getResources().getColor(R.color.warning_light, getTheme()));
        }
    }
}
