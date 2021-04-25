package by.kirill.uskov.medsched.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.models.PatientEvent;

public class EventForPatientAdapter extends RecyclerView.Adapter<EventForPatientAdapter.ViewHolder> {
    private ArrayList<PatientEvent> appointments;

    public EventForPatientAdapter(ArrayList<PatientEvent> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patients_event_layout, parent, false);
        return new EventForPatientAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(appointments.get(position));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView date;
        private TextView procedure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.appointment_date);
            procedure = itemView.findViewById(R.id.appointment_procedure);
        }

        public void bind(PatientEvent event) {
            date.setText(event.getDate());
            procedure.setText(event.getProcedure());
        }
    }


}
