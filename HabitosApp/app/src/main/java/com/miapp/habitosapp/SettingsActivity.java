package com.miapp.habitosapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicializa los botones
        logoutButton = findViewById(R.id.logoutButton);
        backButton = findViewById(R.id.backButton);

        // Configura el botón para cerrar sesión
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Cierra sesión en Firebase
            Toast.makeText(SettingsActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            // Redirige a la pantalla de inicio de sesión
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Configura el botón de retroceso
        backButton.setOnClickListener(v -> finish());
    }
}
