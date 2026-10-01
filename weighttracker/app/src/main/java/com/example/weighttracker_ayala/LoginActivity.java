package com.example.weighttracker_ayala;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> attemptRegister());
    }

    // checks credentials against the database before allowing access
    private void attemptLogin() {
        String username = getFieldText(etUsername);
        String password = getFieldText(etPassword);

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter your username and password.");
            return;
        }

        long userId = db.loginUser(username, password);
        if (userId != -1) {
            openWeightLog(userId);
        } else {
            showMessage("Incorrect username or password.");
        }
    }

    // creates a new account and saves credentials to the database
    private void attemptRegister() {
        String username = getFieldText(etUsername);
        String password = getFieldText(etPassword);

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please choose a username and password.");
            return;
        }

        // prevent duplicate usernames
        if (db.usernameExists(username)) {
            showMessage("That username is already taken. Please choose another.");
            return;
        }

        long userId = db.registerUser(username, password);
        if (userId != -1) {
            openWeightLog(userId);
        } else {
            showMessage("Registration failed. Please try again.");
        }
    }

    // pass userId so the weight log screen knows which user is logged in
    private void openWeightLog(long userId) {
        Intent intent = new Intent(this, WeightLogActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    private String getFieldText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
