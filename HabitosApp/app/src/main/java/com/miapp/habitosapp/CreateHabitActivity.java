package com.miapp.habitosapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miapp.habitosapp.managers.NotificationWorker;
import com.miapp.habitosapp.models.Habit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateHabitActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    private Button buttonMonday, buttonTuesday, buttonWednesday, buttonThursday, buttonFriday, buttonSaturday, buttonSunday, backButton, saveButton;
    private Spinner habitSpinner;
    private String selectedHabitTitle = "Leer"; // Título predeterminado
    private boolean[] selectedDays = new boolean[7]; // Días de la semana seleccionados
    private boolean isStepHabit = false; // Indica si el hábito seleccionado requiere pasos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar permisos para notificaciones (Android 13 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso si no está concedido
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                Log.d("CreateHabitActivity", "Solicitando permiso para POST_NOTIFICATIONS.");
            } else {
                Log.d("CreateHabitActivity", "Permiso POST_NOTIFICATIONS ya concedido.");
            }
        }

        initializeViews();
        setupHabitSpinner();
        setupDayButtons();

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveHabitData());
    }

    private void initializeViews() {
        buttonMonday = findViewById(R.id.buttonMonday);
        buttonTuesday = findViewById(R.id.buttonTuesday);
        buttonWednesday = findViewById(R.id.buttonWednesday);
        buttonThursday = findViewById(R.id.buttonThursday);
        buttonFriday = findViewById(R.id.buttonFriday);
        buttonSaturday = findViewById(R.id.buttonSaturday);
        buttonSunday = findViewById(R.id.buttonSunday);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        habitSpinner = findViewById(R.id.habitSpinner);
    }

    private void setupHabitSpinner() {
        String[] habitsArray = {
                "Leer", "Dormir", "Beber agua", "Estudiar", "Limpiar",
                "Comer sano", "Deporte", "Fuera móvil", "Meditar", "Ahorrar"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, habitsArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        habitSpinner.setAdapter(spinnerAdapter);

        habitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHabitTitle = habitsArray[position];
                isStepHabit = selectedHabitTitle.equalsIgnoreCase("Deporte"); // Detectar si es el hábito "Deporte"
                Log.d("CreateHabitActivity", "Hábito seleccionado: " + selectedHabitTitle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("CreateHabitActivity", "No se seleccionó ningún hábito.");
            }
        });
    }

    private void setupDayButtons() {
        setButtonListener(buttonMonday, 0);
        setButtonListener(buttonTuesday, 1);
        setButtonListener(buttonWednesday, 2);
        setButtonListener(buttonThursday, 3);
        setButtonListener(buttonFriday, 4);
        setButtonListener(buttonSaturday, 5);
        setButtonListener(buttonSunday, 6);
    }

    private void setButtonListener(Button button, final int dayIndex) {
        button.setOnClickListener(v -> {
            selectedDays[dayIndex] = !selectedDays[dayIndex];
            updateButtonColors(button, selectedDays[dayIndex]);
        });
    }

    private void updateButtonColors(Button button, boolean isSelected) {
        int textColor = ContextCompat.getColor(this, isSelected ? android.R.color.black : android.R.color.white);
        int backgroundColor = ContextCompat.getColor(this, isSelected ? R.color.selectedButtonColor : R.color.deselectedButtonColor);

        button.setTextColor(textColor);
        button.setBackgroundColor(backgroundColor);
    }

    private void saveHabitData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Validar que al menos un día esté seleccionado
        boolean hasSelectedDay = false;
        for (boolean day : selectedDays) {
            if (day) {
                hasSelectedDay = true;
                break;
            }
        }
        if (!hasSelectedDay) {
            Toast.makeText(this, "Selecciona al menos un día para el hábito.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un nuevo hábito con los datos seleccionados
        Habit habit = new Habit(null, selectedHabitTitle, convertArrayToList(selectedDays), isStepHabit);

        // Guardar el hábito en Firestore
        db.collection("users").document(userId)
                .collection("habits")
                .whereEqualTo("title", selectedHabitTitle)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Ya tienes un hábito con este título.", Toast.LENGTH_SHORT).show();
                    } else {
                        saveNewHabit(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateHabitActivity", "Error al verificar duplicados: " + e.getMessage());
                    Toast.makeText(this, "Error al verificar duplicados.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewHabit(String userId) {
        Habit habit = new Habit(null, selectedHabitTitle, convertArrayToList(selectedDays), isStepHabit);

        db.collection("users").document(userId)
                .collection("habits")
                .add(habit)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CreateHabitActivity", "Hábito guardado. Programando notificación...");
                    Toast.makeText(this, "Hábito guardado", Toast.LENGTH_SHORT).show();
                    scheduleNotification(habit.getTitle());
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateHabitActivity", "Error al guardar el hábito: " + e.getMessage());
                    Toast.makeText(this, "Error al guardar el hábito.", Toast.LENGTH_SHORT).show();
                });
    }

    private int mapDayIndexToCalendarValue(int dayIndex) {
        switch (dayIndex) {
            case 0:
                return Calendar.MONDAY;
            case 1:
                return Calendar.TUESDAY;
            case 2:
                return Calendar.WEDNESDAY;
            case 3:
                return Calendar.THURSDAY;
            case 4:
                return Calendar.FRIDAY;
            case 5:
                return Calendar.SATURDAY;
            case 6:
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }


    // Convertimos el array de días seleccionados en una lista de valores de Calendar

    private void scheduleNotification(String habitTitle) {

        List<Integer> selectedDaysList = new ArrayList<>();
        for (int i = 0; i < selectedDays.length; i++) {
            if (selectedDays[i]) {
                int calendarValue = mapDayIndexToCalendarValue(i); // Ajustar índice al formato Calendar
                selectedDaysList.add(calendarValue);
            }
        }

        // Convertimos la lista en un String para enviarla como Data
        String selectedDaysString = selectedDaysList.toString();


        // Configurar la hora de la notificación
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22); // 22h  notifiación
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);


        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Construir datos para pasar al Worker
        Data data = new Data.Builder()
                .putString("HABIT_TITLE", habitTitle)
                .putString("SELECTED_DAYS", selectedDaysString) // Pasar los días seleccionados
                .build();

        // Programar la notificación con WorkManager
        long delay = calendar.getTimeInMillis() - System.currentTimeMillis(); // Calcular el tiempo restante
        OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Retraso calculado
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueue(notificationRequest);
        Log.d("CreateHabitActivity", "Notificación programada para 1 minuto con título: " + habitTitle + " y días: " + selectedDaysString);
    }


    private List<Boolean> convertArrayToList(boolean[] array) {
        List<Boolean> list = new ArrayList<>();
        for (boolean value : array) {
            list.add(value);
        }
        return list;
    }
}
