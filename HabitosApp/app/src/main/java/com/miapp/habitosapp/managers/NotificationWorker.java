package com.miapp.habitosapp.managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.miapp.habitosapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Worker que envía notificaciones basadas en días configurados para hábitos.

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "habit_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        // Obtiene los datos enviados
        String habitTitle = getInputData().getString("HABIT_TITLE");
        String selectedDaysString = getInputData().getString("SELECTED_DAYS");

        // Procesa los días seleccionados
        List<Integer> selectedDaysList = new ArrayList<>();
        if (selectedDaysString != null && !selectedDaysString.isEmpty()) {
            selectedDaysString = selectedDaysString.replace("[", "").replace("]", ""); // Quitar corchetes
            for (String day : selectedDaysString.split(", ")) {
                selectedDaysList.add(Integer.parseInt(day));
            }
        }

        // Verifica si hoy está en los días seleccionados
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        Log.d("NotificationWorker", "Worker iniciado con hábito: " + habitTitle + " y días seleccionados: " + selectedDaysList);

        if (selectedDaysList.contains(today)) {
            Log.d("NotificationWorker", "Es el día correcto. Mostrando notificación...");
            createNotificationChannel();
            showNotification(habitTitle);
        } else {
            Log.d("NotificationWorker", "Hoy no corresponde con los días configurados.");
        }

        return Result.success();
    }


    private boolean isToday(int dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d("NotificationWorker", "Hoy es: " + today + ", Día configurado: " + dayOfWeek);
        return today == dayOfWeek;
    }


    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Habit Notifications";
            String description = "Notificaciones para los hábitos";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("NotificationWorker", "Canal de notificación creado correctamente.");
            } else {
                Log.e("NotificationWorker", "Error: NotificationManager es null al crear el canal.");
            }
        }
    }

    private void showNotification(String habitTitle) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("¡Es hora de tu hábito!")
                .setContentText(habitTitle != null ? habitTitle : "Tienes un hábito pendiente")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (manager != null) {
            try {
                manager.notify(NOTIFICATION_ID, builder.build());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Hubo un problema al enviar la notificación.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

