package com.miapp.habitosapp.managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.miapp.habitosapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//Esta es la que comento en el video del sueño
//No esta al 100% comprobada

// Worker para monitorear inactividad y emitir notificaciones en días seleccionados
public class InactivityWorker extends Worker {

    private static final long DEFAULT_INACTIVITY_TIME = 6 * 60 * 60 * 1000; // Tiempo de inactividad predeterminado (6 h en milisegundos).

    // Inicializa el Worker con el contexto y los parámetros.
    public InactivityWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtiene el tiempo de inactividad y los días seleccionados desde los datos de entrada
        long inactivityTime = getInputData().getLong("INACTIVITY_TIME", DEFAULT_INACTIVITY_TIME);
        String selectedDaysString = getInputData().getString("SELECTED_DAYS");

        // Convierte los días seleccionados en una lista de enteros
        List<Integer> selectedDays = new ArrayList<>();
        if (selectedDaysString != null) {
            selectedDaysString = selectedDaysString.replace("[", "").replace("]", ""); // Limpia el formato del string.
            for (String day : selectedDaysString.split(", ")) {
                selectedDays.add(Integer.parseInt(day.trim())); // Convierte cada día en un entero
            }
        }

        // Verifica si hoy es uno de los días seleccionados
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (!selectedDays.contains(today)) {
            // Si hoy no es un día seleccionado, registra el evento y termina el Worker
            Log.d("InactivityWorker", "Hoy no es un día seleccionado. No se muestra notificación.");
            return Result.success();
        }

        // Si hoy es un día seleccionado, emite una notificación
        emitNotification();
        return Result.success();
    }

    // Crea y muestra una notificación para el usuario
    private void emitNotification() {
        String channelId = "inactivity_channel"; // ID del canal de notificación
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Configura el canal de notificaciones si es necesario (Android 8.0 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Inactividad", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Construye la notificación con un título y mensaje predeterminados
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification) // Ícono de la notificación.
                .setContentTitle("Hábito: Dormir")
                .setContentText("Hoy es un día seleccionado para monitorear tu descanso.")
                .setPriority(NotificationCompat.PRIORITY_HIGH); // Prioridad alta para la notificación.

        // Muestra la notificación al usuario
        notificationManager.notify(1, builder.build());
    }
}
