package com.miapp.habitosapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.miapp.habitosapp.managers.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button googleSignInButton;
    private Button registerButton;
    private AuthManager authManager;

/*
    No he encontrado ninguno que no
    este deprecate en la siguiente
    versión habria que actializarlo
    antes que desaparezca
 */

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configuración de Google Sign-In, no he encontrado ninguno que no este deprecated
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        authManager = new AuthManager();

        // Conecta los elementos del layout
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        registerButton = findViewById(R.id.registerButton);


        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginUser(email, password);
        });

        // Configura el botón de Google Sign-In
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Configura el botón de registro
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    if (task.isSuccessful()) {
                        GoogleSignInAccount account = task.getResult();
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } else {
                        Log.w("LoginActivity", "Google sign in failed", task.getException());
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authManager.signInWithCredential(credential, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = authManager.getCurrentUser();
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HabitDetailActivity.class));
                    finish();
                }
            } else {
                Log.w("LoginActivity", "SignInWithCredential:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        authManager.loginUser(email, password, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = authManager.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HabitDetailActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Verifica tu correo electrónico", Toast.LENGTH_SHORT).show();
                    authManager.logout();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Error: " + task.getException());
            }
        });
    }
}
