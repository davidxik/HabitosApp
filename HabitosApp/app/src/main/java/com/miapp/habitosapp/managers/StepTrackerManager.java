package com.miapp.habitosapp.managers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepTrackerManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int stepsAtStart = -1;

    private String lastRecordedDate;
    private final StepTrackerCallback callback;

    public StepTrackerManager(Context context, StepTrackerCallback callback) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.stepSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) : null;
        this.callback = callback;
    }

    public void startTracking() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

            lastRecordedDate = getCurrentDate();
        } else {
            Log.e("StepTrackerManager", "Sensor no disponible.");
            if (callback != null) {
                callback.onStepUpdate(-1);
            }
        }
    }

    public void stopTracking() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d("StepTrackerManager", "onSensorChanged llamado. Valor actual: " + event.values[0]);

        // Verificar si es un nuevo día
        String currentDate = getCurrentDate();
        if (!currentDate.equals(lastRecordedDate)) {
            resetDailySteps(); // Reiniciar pasos diarios
            lastRecordedDate = currentDate; // Actualizar la fecha registrada
        }

        // Calcular los pasos desde el inicio del día
        if (stepsAtStart == -1) {
            stepsAtStart = (int) event.values[0];
            Log.d("StepTrackerManager", "Inicializando stepsAtStart: " + stepsAtStart);
        }
        totalSteps = (int) event.values[0] - stepsAtStart;
        Log.d("StepTrackerManager", "Pasos diarios calculados: " + totalSteps);

        // Notificar los pasos actualizados
        if (callback != null) {
            callback.onStepUpdate(totalSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public int getTotalSteps() {
        return totalSteps;
    }

    // Reinicia los pasos diarios
    private void resetDailySteps() {
        stepsAtStart = -1; // Reiniciar el contador
        totalSteps = 0; // Reiniciar pasos diarios
        Log.d("StepTrackerManager", "Reinicio de pasos diarios.");
    }

    // Obtiene la fecha actual en formato "dd/MM/yyyy"
    private String getCurrentDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    public interface StepTrackerCallback {
        void onStepUpdate(int steps);
    }
}