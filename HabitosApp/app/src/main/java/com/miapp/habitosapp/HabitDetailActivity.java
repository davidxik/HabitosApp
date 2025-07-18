package com.miapp.habitosapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.miapp.habitosapp.adapters.CalendarAdapter;
import com.miapp.habitosapp.adapters.HabitAdapter;
import com.miapp.habitosapp.managers.InactivityWorker;
import com.miapp.habitosapp.managers.StepTrackerManager;
import com.miapp.habitosapp.models.Habit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitDetailActivity extends AppCompatActivity implements StepTrackerManager.StepTrackerCallback {

    private Button createHabitButton, recordsButton;
    private ImageButton settingsButton;
    private RecyclerView habitRecyclerView, calendarRecyclerView;
    private HabitAdapter habitAdapter;
    private CalendarAdapter calendarAdapter;
    private List<Habit> habitList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StepTrackerManager stepTrackerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa StepTrackerManager
        stepTrackerManager = new StepTrackerManager(this, this);
        stepTrackerManager.startTracking();

        // Inicializa vistas
        initializeViews();

        // Configura RecyclerViews
        setupHabitRecyclerView();
        setupCalendarRecyclerView();

        // Configura Swipe para borrar
        setupSwipeToDelete();

        // Configura listeners
        setupListeners();

        // Carga hábitos desde Firebase
        loadHabitsFromFirebase();


    }

    private void initializeViews() {
        settingsButton = findViewById(R.id.settingsButton);
        createHabitButton = findViewById(R.id.createHabitButton);
        recordsButton = findViewById(R.id.recordsButton);
        habitRecyclerView = findViewById(R.id.habitRecyclerView);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
    }

    private void setupHabitRecyclerView() {
        habitAdapter = new HabitAdapter(habitList, this::onHabitChecked);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitRecyclerView.setAdapter(habitAdapter);
    }

    private void onHabitChecked(Habit habit) {
        Log.d("HabitDetailActivity", "Pasos actuales: " + stepTrackerManager.getTotalSteps());

        // Validación  Deporte
        if (habit.isStepHabit()) {
            if (stepTrackerManager.getTotalSteps() >= 5000) {
                Log.d("HabitDetailActivity", "Meta alcanzada. Permitido el check.");
                habit.setChecked(true);
                habit.incrementStreak();
                Toast.makeText(this, "¡Objetivo de pasos alcanzado!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("HabitDetailActivity", "No se alcanzaron los pasos necesarios.");
                Toast.makeText(this, "Aún no has alcanzado los 5000 pasos.", Toast.LENGTH_SHORT).show();
                return; // Salir si no cumple
            }
        } else {

            habit.setChecked(true);
            habit.incrementStreak();
        }
        // Hábito Dormir me hubiera gustado probarlo más realemente, lo dejo como mejora futura
        if (habit.getTitle().equalsIgnoreCase("Dormir")) {
            List<Integer> selectedDays = habit.getSelectedDaysAsIntegers(); // Método en Habit para obtener días seleccionados
            if (selectedDays != null && !selectedDays.isEmpty()) {
                monitorInactivity(selectedDays); // Llama al método de monitoreo
            } else {
                Log.d("HabitDetailActivity", "No hay días seleccionados para monitorear.");
            }
        }


        // Actualizar el hábito en Firebase
        updateHabitInFirebase(habit);
    }


    private void updateHabitInFirebase(Habit habit) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("habits")
                .document(habit.getId())
                .set(habit)
                .addOnSuccessListener(aVoid -> Log.d("HabitDetailActivity", "Hábito actualizado correctamente."))
                .addOnFailureListener(e -> Log.e("HabitDetailActivity", "Error al actualizar el hábito: ", e));
    }

    private void setupCalendarRecyclerView() {
        List<String> dynamicDates = generateDynamicDates();

        calendarAdapter = new CalendarAdapter(dynamicDates);
        calendarAdapter.setOnDateSelectedListener(selectedDate ->
                Toast.makeText(this, "Fecha seleccionada: " + selectedDate, Toast.LENGTH_SHORT).show()
        );

        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        calendarRecyclerView.setAdapter(calendarAdapter);

        int todayPosition = dynamicDates.indexOf(getFormattedTodayDate());
        if (todayPosition != -1) {
            calendarRecyclerView.scrollToPosition(todayPosition);
        }
    }

    private List<String> generateDynamicDates() {
        // Genera una lista de fechas dinámicas desde 15 días antes hasta 15 días después de la fecha actual
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = -15; i <= 15; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, i);
            dates.add(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, -i);
        }
        return dates;
    }

    private String getFormattedTodayDate() {
        return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date());
    }

    private void loadHabitsFromFirebase() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error al sincronizar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        habitList.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            Habit habit = document.toObject(Habit.class);
                            habit.setId(document.getId());
                            habitList.add(habit);
                        }
                        habitAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Habit habitToDelete = habitList.get(position);

                new AlertDialog.Builder(HabitDetailActivity.this)
                        .setTitle("Eliminar Hábito")
                        .setMessage("¿Estás seguro de que quieres eliminar este hábito?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            habitList.remove(position);
                            habitAdapter.notifyItemRemoved(position);

                            String userId = mAuth.getCurrentUser().getUid();
                            db.collection("users")
                                    .document(userId)
                                    .collection("habits")
                                    .document(habitToDelete.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(HabitDetailActivity.this, "Hábito eliminado", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(HabitDetailActivity.this, "Error al eliminar hábito.", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> habitAdapter.notifyItemChanged(position))
                        .create()
                        .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(habitRecyclerView);
    }

    private void setupListeners() {
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        createHabitButton.setOnClickListener(v -> startActivity(new Intent(this, CreateHabitActivity.class)));
        recordsButton.setOnClickListener(v -> startActivity(new Intent(this, RecordsActivity.class)));
    }

    @Override
    public void onStepUpdate(int steps) {
        Log.d("HabitDetailActivity", "Pasos actuales: " + steps);
    }

    private void monitorInactivity(List<Integer> selectedDays) {
        // Tiempo de inactividad (1min) 6 horas, no observado realmente por falta de tiempo
        long testInactivityTime = 1 * 60 * 1000;

        // Convertir en un array de enteros
        int[] selectedDaysArray = selectedDays.stream().mapToInt(i -> i).toArray();

        // Pasar datos al Worker
        Data data = new Data.Builder()
                .putLong("INACTIVITY_TIME", testInactivityTime)
                .putIntArray("SELECTED_DAYS", selectedDaysArray)
                .build();

        // Configurar el WorkManager
        OneTimeWorkRequest inactivityRequest = new OneTimeWorkRequest.Builder(InactivityWorker.class)
                .setInputData(data)
                .build();

        // Encolar el Worker
        WorkManager.getInstance(this).enqueue(inactivityRequest);
    }


}


