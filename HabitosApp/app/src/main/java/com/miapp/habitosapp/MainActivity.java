package com.miapp.habitosapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.miapp.habitosapp.managers.AuthManager;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private Button logoutButton;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        authManager = new AuthManager();

        // Conecta los elementos del layout
        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Mostra el nombre de usuario (si está disponible)
        if (authManager.getCurrentUser() != null) {
            String email = authManager.getCurrentUser().getEmail();
            welcomeTextView.setText("Bienvenido, " + email);
        }

        // Configura el botón de cierre de sesión
        logoutButton.setOnClickListener(v -> {
            authManager.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}