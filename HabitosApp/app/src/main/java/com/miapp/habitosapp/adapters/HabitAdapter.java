package com.miapp.habitosapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miapp.habitosapp.R;
import com.miapp.habitosapp.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Gestiona la lista de hábitos para mostrar en un RecyclerView
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habitList; // Lista de hábitos
    private final FirebaseAuth mAuth; // Instancia de Firebase Authentication
    private final FirebaseFirestore db; // Instancia de Firestore
    private final OnHabitCheckedListener onHabitCheckedListener; // Listener para eventos de marcado
    private final Set<String> daysLogged; // Almacena días registrados para la racha global

    // Interfaz para manejar el evento de marcado de hábitos
    public interface OnHabitCheckedListener {
        void onHabitChecked(Habit habit);
    }

    // Inicializa el adaptador con la lista de hábitos y el listener
    public HabitAdapter(List<Habit> habitList, OnHabitCheckedListener onHabitCheckedListener) {
        this.habitList = habitList;
        this.onHabitCheckedListener = onHabitCheckedListener;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        daysLogged = new HashSet<>();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño de cada elemento de la lista.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        // Muestra el título del hábito
        holder.habitTitle.setText(habit.getTitle());

        // Configura el estado del botón de marcado (check)
        boolean isChecked = habit.isChecked();
        holder.checkHabitButton.setImageResource(R.drawable.ic_check);
        holder.checkHabitButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(),
                isChecked ? R.color.green : R.color.gray));

        // Maneja el clic en el botón de marcado
        holder.checkHabitButton.setOnClickListener(v -> {
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Verifica condiciones específicas para hábitos de pasos
            if (habit.isStepHabit()) {
                Toast.makeText(holder.itemView.getContext(),
                        "Realiza mínimo 5000 pasos y podrás verificar este hábito.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Marca el hábito como completado si no está registrado hoy
            if (!habit.getCompletedDates().contains(today)) {
                habit.addCompletedDate(today); // Añade la fecha al hábito.
                habit.incrementStreak(); // Incrementa la racha del hábito.
                updateHabitInFirebase(habit); // Actualiza en Firebase.

                // Incrementa la racha global si es el primer hábito registrado hoy
                if (!daysLogged.contains(today)) {
                    incrementGlobalStreak();
                    daysLogged.add(today);
                }

                // Felicita si alcanza los 66 días de racha
                if (habit.getStreak() == 66) {
                    Toast.makeText(holder.itemView.getContext(),
                            "¡Felicitaciones! Completaste 66 días en: " + habit.getTitle(),
                            Toast.LENGTH_LONG).show();
                }
            }

            // Notifica al adaptador que se actualizó un elemento
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        // Devuelve el número de hábitos en la lista
        return habitList.size();
    }

    // Actualiza un hábito en Firestore
    private void updateHabitInFirebase(Habit habit) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users")
                    .document(userId)
                    .collection("habits")
                    .document(habit.getId())
                    .set(habit)
                    .addOnSuccessListener(aVoid -> Log.d("HabitAdapter", "Hábito actualizado en Firestore"))
                    .addOnFailureListener(e -> Log.e("HabitAdapter", "Error al actualizar hábito en Firestore: " + e.getMessage()));
        }
    }

    // Incrementa la racha global
    private void incrementGlobalStreak() {
        Log.d("HabitAdapter", "Racha global incrementada.");
    }

    // Representa un elemento de la lista de hábitos
    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView habitTitle; // Título del hábito
        ImageButton checkHabitButton; // Botón para marcar el hábito

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            habitTitle = itemView.findViewById(R.id.habitTitleTextView);
            checkHabitButton = itemView.findViewById(R.id.checkHabitButton);
        }
    }
}
