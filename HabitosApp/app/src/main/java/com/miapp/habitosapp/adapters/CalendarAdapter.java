package com.miapp.habitosapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miapp.habitosapp.R;

import java.util.List;

// Gestiona la lista de fechas para mostrar en un RecyclerView.
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<String> dateList;// Lista de fechas a mostrar.
    private OnDateSelectedListener listener;// Listener para manejar la selección de fechas.


    // Interfaz para notificar cuando se selecciona una fecha.
    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    // Permite establecer un listener desde la actividad o fragmento.
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    // Inicializa el adaptador con la lista de fechas.
    public CalendarAdapter(List<String> dateList) {
        this.dateList = dateList;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflam el diseño de cada elemento de la lista.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        // Obten la fecha correspondiente a la posición actual.
        String date = dateList.get(position);
        holder.dateTextView.setText(date);

        // Notifica al listener si se selecciona una fecha.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateSelected(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    // ViewHolder que representa el diseño de cada fecha.
    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;// TextView para mostrar la fecha.

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);// Vincula el TextView del diseño.
        }
    }
}
