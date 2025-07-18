package com.miapp.habitosapp.managers;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// Gestiona las operaciones de autenticación con Firebase
public class AuthManager {

    // Instancia de FirebaseAuth
    private FirebaseAuth auth;

    // Inicializa el administrador de autenticación
    public AuthManager() {
        auth = FirebaseAuth.getInstance();
    }

    // Registra un nuevo usuario con email y contraseña
    public void registerUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Inicia sesión con email y contraseña
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Inicia sesión con Google
    public void signInWithCredential(AuthCredential credential, OnCompleteListener<AuthResult> listener) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(listener);
    }

    // Obtiene el usuario actualmente autenticado
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Cierra la sesión del usuario actual
    public void logout() {
        auth.signOut();
    }
}
