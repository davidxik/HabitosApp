package com.miapp.habitosapp.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Habit {

    private String id;
    private String title;
    private List<Boolean> selectedDays;
    private List<String> completedDates; // Registrar días completados
    private int streak;
    private int longestStreak;
    private boolean isStepHabit;


    // Constructor vacío necesario para Firebase
    public Habit() {
        this.id = "";
        this.title = "Hábito sin título";
        this.selectedDays = new ArrayList<>(List.of(false, false, false, false, false, false, false));
        this.completedDates = new ArrayList<>();
        this.streak = 0;
        this.longestStreak = 0;
        this.isStepHabit = false;
    }

    // Constructor con parámetros
    public Habit(String id, String title, List<Boolean> selectedDays, boolean isStepHabit) {
        this.id = id != null ? id : "";
        this.title = title != null ? title : "Hábito sin título";
        this.selectedDays = selectedDays != null ? selectedDays : new ArrayList<>(List.of(false, false, false, false, false, false, false));
        this.completedDates = new ArrayList<>();
        this.streak = 0;
        this.longestStreak = 0;
        this.isStepHabit = isStepHabit;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Boolean> getSelectedDays() {
        return selectedDays;
    }

    public List<Integer> getSelectedDaysAsIntegers() {
        List<Integer> selectedDaysAsIntegers = new ArrayList<>();
        for (int i = 0; i < selectedDays.size(); i++) {
            if (selectedDays.get(i)) {
                selectedDaysAsIntegers.add(i); // Agrega índices 0-6 para los días seleccionados
            }
        }
        return selectedDaysAsIntegers;
    }


    public List<String> getCompletedDates() {
        return completedDates;
    }

    public void addCompletedDate(String date) {
        if (!completedDates.contains(date)) {
            completedDates.add(date);
        }
    }

    public boolean isChecked() {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        return completedDates.contains(today);
    }

    public int getStreak() {
        return streak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public boolean isStepHabit() {
        return isStepHabit;
    }

    public void setStepHabit(boolean stepHabit) {
        this.isStepHabit = stepHabit;
    }


    // Métodos agregados
    public void setChecked(boolean checked) {
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        if (checked && !completedDates.contains(today)) {
            completedDates.add(today);
        }
    }

    public void incrementStreak() {
        this.streak++;
        if (this.streak > this.longestStreak) {
            this.longestStreak = this.streak;
        }
    }

    // Actualizar racha basada en días seleccionados
    public void updateStreak() {
        int missedDays = 0;
        int currentStreak = 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < 30; i++) { // Revisar 30 días hacia atrás
            String dateToCheck = sdf.format(calendar.getTime());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Índice 0-6

            if (selectedDays.get(dayOfWeek)) { // Si el día está seleccionado
                if (completedDates.contains(dateToCheck)) {
                    currentStreak++;
                    missedDays = 0; // Reiniciar días fallidos
                } else {
                    missedDays++;
                    if (missedDays >= 5) { // Perder la racha si no cumple 5 días consecutivos seleccionados
                        currentStreak = 0;
                        break;
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        streak = currentStreak;
        if (streak > longestStreak) {
            longestStreak = streak;
        }
    }

    public void updateStreakBasedOnSelectedDays() {
        int missedDays = 0;
        int currentStreak = 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < 30; i++) { // Revisar hasta 30 días atrás
            String dateToCheck = sdf.format(calendar.getTime());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Índice 0-6 para días

            if (selectedDays.get(dayOfWeek)) {
                if (completedDates.contains(dateToCheck)) {
                    currentStreak++;
                    missedDays = 0; // Reiniciar fallos consecutivos
                } else {
                    missedDays++;
                    if (missedDays >= 5) { // Si falla en 5 días consecutivos seleccionados
                        currentStreak = 0;
                        break;
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        streak = currentStreak;
        longestStreak = Math.max(longestStreak, currentStreak);
    }


}
