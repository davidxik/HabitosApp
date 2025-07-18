package com.miapp.habitosapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.miapp.habitosapp.managers.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Usama el mismo layout

        // Inicializa AuthManager
        authManager = new AuthManager();

        // Conecta los elementos del layout
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        // Configura el botón de registro
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInput(email, password)) {
                registerUser(email, password);
            }
        });
    }

    // Método para validar que los campos que no estén vacíos
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("El correo electrónico es obligatorio");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("La contraseña es obligatoria");
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        return true;
    }

    private void registerUser(String email, String password) {

        registerButton.setEnabled(false);

        authManager.registerUser(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Habilita el botón de registro de nuevo
                registerButton.setEnabled(true);

                if (task.isSuccessful()) {
                    FirebaseUser user = authManager.getCurrentUser();
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this,
                                                "Registro exitoso. Verifica tu correo electrónico.",
                                                Toast.LENGTH_LONG).show();
                                        // Redirige a LoginActivity después de registro exitoso
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this,
                                                "Error al enviar verificación. Intenta nuevamente.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    Log.e("RegisterActivity", "Error: " + task.getException());
                }
            }
        });
    }
}
