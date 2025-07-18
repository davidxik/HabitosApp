package com.miapp.habitosapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.miapp.habitosapp.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    // Elementos de la interfaz para mostrar estadísticas y el calendario.
    private TextView currentStreakTextView;
    private TextView longestStreakTextView;
    private TextView completionPercentageTextView;
    private TextView calendarTitleTextView;
    private TextView motivationalPhraseTextView;
    private GridLayout calendarGrid;

    // Instancias de Firebase y el calendario actual.
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Calendar currentCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // Inicializa vistas de la interfaz.
        currentStreakTextView = findViewById(R.id.currentStreakTextView);
        longestStreakTextView = findViewById(R.id.longestStreakTextView);
        completionPercentageTextView = findViewById(R.id.completionPercentageTextView);
        calendarTitleTextView = findViewById(R.id.calendarTitleTextView);
        motivationalPhraseTextView = findViewById(R.id.motivationalPhraseTextView);
        calendarGrid = findViewById(R.id.calendarGrid);

        // Inicializa Firebase para gestionar datos del usuario.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa el calendario actual.
        currentCalendar = Calendar.getInstance();

        // Configura los botones para cambiar de mes en el calendario.
        Button prevMonthButton = findViewById(R.id.prevMonthButton);
        Button nextMonthButton = findViewById(R.id.nextMonthButton);

        // Configura el botón para retroceder un mes y recargar el calendario.
        prevMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarTitle();
            loadCalendar();
        });

        // Configura el botón para avanzar un mes y recargar el calendario.
        nextMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarTitle();
            loadCalendar();
        });

        // Carga los datos iniciales: título del calendario, estadísticas, calendario y frase motivacional.
        updateCalendarTitle();
        loadStatistics();
        loadCalendar();
        loadMotivationalPhrase();
    }

    // Actualiza el título del calendario con el mes y año actuales.
    private void updateCalendarTitle() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String title = dateFormat.format(currentCalendar.getTime());
        calendarTitleTextView.setText(title.substring(0, 1).toUpperCase() + title.substring(1)); // Capitalizar
    }

    // Carga el calendario mensual, resaltando los días en los que se completaron hábitos.
    private void loadCalendar() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        calendarGrid.removeAllViews(); // Limpia el calendario antes de recargarlo.
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);

        String userId = mAuth.getCurrentUser().getUid();

        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayView = createDayView(day);

            // Formatea la fecha del día actual.
            String date = String.format("%02d/%02d/%04d", day, currentMonth + 1, currentYear);
            db.collection("users")
                    .document(userId)
                    .collection("habits")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        boolean isCompleted = false;

                        // Verifica si el día está marcado como completado en algún hábito.
                        for (Habit habit : querySnapshot.toObjects(Habit.class)) {
                            if (habit.getCompletedDates().contains(date)) {
                                isCompleted = true;
                                break;
                            }
                        }

                        if (isCompleted) {
                            dayView.setBackgroundColor(Color.GREEN); // Marca el día como completado.
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar datos del calendario", Toast.LENGTH_SHORT).show();
                    });

            calendarGrid.addView(dayView);
        }
    }

    // Crea la vista de cada día en el calendario.
    private TextView createDayView(int day) {
        TextView dayView = new TextView(this);
        dayView.setText(String.valueOf(day));
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setTextSize(24); // Tamaño del texto del día.
        dayView.setPadding(16, 16, 16, 16);
        dayView.setBackgroundColor(Color.TRANSPARENT);
        return dayView;
    }

    // Carga las estadísticas del usuario, como la racha actual y la racha más larga.
    private void loadStatistics() {
        if (mAuth.getCurrentUser() == null) {
            currentStreakTextView.setText("Usuario no autenticado");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        db.collection("users")
                .document(userId)
                .collection("habits")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalStreak = 0;
                    int longestStreak = 0;
                    boolean hasUpdatedTotalStreak = false;
                    double maxHabitPercentage = 0;

                    for (Habit habit : querySnapshot.toObjects(Habit.class)) {
                        // Incrementa la racha total si aplica.
                        if (habit.getCompletedDates().contains(today) && !hasUpdatedTotalStreak) {
                            totalStreak++;
                            hasUpdatedTotalStreak = true;
                        }

                        // Calcula la racha más larga entre los hábitos.
                        longestStreak = Math.max(longestStreak, habit.getLongestStreak());

                        // Calcula el porcentaje completado del hábito más cercano a 66 días.
                        double habitPercentage = (habit.getStreak() / 66.0) * 100;
                        if (habitPercentage > maxHabitPercentage) {
                            maxHabitPercentage = habitPercentage;
                        }
                    }

                    // Actualiza las vistas con las estadísticas calculadas.
                    currentStreakTextView.setText("Racha actual total: " + totalStreak);
                    longestStreakTextView.setText("Racha más larga: " + longestStreak);
                    completionPercentageTextView.setText(String.format(Locale.getDefault(), "Mejor hábito (66 días): %.1f %% completado", maxHabitPercentage));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar estadísticas", Toast.LENGTH_SHORT).show();
                });
    }

    // Carga una frase motivacional aleatoria desde Firestore para mostrar en la pantalla.
    private void loadMotivationalPhrase() {
        db.collection("motivational_phrases")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String phrase = querySnapshot.getDocuments()
                                .get((int) (Math.random() * querySnapshot.size()))
                                .getString("text");
                        motivationalPhraseTextView.setText(phrase);
                    } else {
                        motivationalPhraseTextView.setText("¡Nunca es tarde para empezar algo nuevo!");
                    }
                })
                .addOnFailureListener(e -> {
                    motivationalPhraseTextView.setText("Error al cargar frase motivacional.");
                });
    }

    // Finaliza la actividad y vuelve a la pantalla anterior.
    public void onBackButtonPressed(View view) {
        finish();
    }
}
