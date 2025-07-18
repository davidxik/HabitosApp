package com.miapp.habitosapp.managers;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miapp.habitosapp.models.Habit;

import java.util.ArrayList;
import java.util.List;


// Clase FirestoreManager creada para gestionar hábitos en Firestore
// Actualmente no se está usando, pero puede ser utilizada en futuras actualizaciones


// Gestiona las operaciones de Firestore relacionadas con los hábitos.
public class FirestoreManager {

    private final FirebaseFirestore db; // Instancia de Firestore.

    // Inicializa el administrador de Firestore.
    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Obtiene una referencia de la colección de hábitos de un usuario.
    public CollectionReference getHabitsCollection(String userId) {
        return db.collection("users").document(userId).collection("habits");
    }

    // Añade un nuevo hábito.
    public void addHabit(String userId, Habit habit) {
        String habitId = getHabitsCollection(userId).document().getId(); // Genera un ID único.
        habit.setId(habitId);
        getHabitsCollection(userId).document(habitId).set(habit)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreManager", "Hábito añadido correctamente."))
                .addOnFailureListener(e -> Log.e("FirestoreManager", "Error al añadir el hábito.", e));
    }

    // Actualiza un hábito existente.
    public void updateHabit(String userId, Habit habit) {
        DocumentReference habitRef = getHabitsCollection(userId).document(habit.getId());
        habitRef.set(habit)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreManager", "Hábito actualizado correctamente."))
                .addOnFailureListener(e -> Log.e("FirestoreManager", "Error al actualizar el hábito.", e));
    }

    // Obtiene todos los hábitos de un usuario.
    public void getAllHabits(String userId, FirestoreListCallback callback) {
        getHabitsCollection(userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Habit> habits = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Habit habit = document.toObject(Habit.class);
                        if (habit != null) {
                            habits.add(habit);
                        }
                    }
                    callback.onSuccess(habits);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Obtiene un hábito por ID.
    public void getHabit(String userId, String habitId, FirestoreCallback callback) {
        getHabitsCollection(userId).document(habitId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Habit habit = documentSnapshot.toObject(Habit.class);
                        callback.onSuccess(habit);
                    } else {
                        callback.onFailure(new Exception("Hábito no encontrado."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Elimina un hábito por ID.
    public void deleteHabit(String userId, String habitId) {
        getHabitsCollection(userId).document(habitId).delete()
                .addOnSuccessListener(aVoid -> Log.d("FirestoreManager", "Hábito eliminado correctamente."))
                .addOnFailureListener(e -> Log.e("FirestoreManager", "Error al eliminar el hábito.", e));
    }

    // Interfaz para manejar callbacks al obtener una lista de hábitos.
    public interface FirestoreListCallback {
        void onSuccess(List<Habit> habits);

        void onFailure(Exception e);
    }

    // Interfaz para manejar callbacks al obtener un hábito.
    public interface FirestoreCallback {
        void onSuccess(Habit habit);

        void onFailure(Exception e);
    }
}
