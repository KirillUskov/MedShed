package by.kirill.uskov.medsched.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.events.RecyclerViewAdapter;
import by.kirill.uskov.medsched.models.CalendarEvent;
import by.kirill.uskov.medsched.models.Time;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>{
    private Context context;
    private ArrayList<CalendarEvent> dayEvents = new ArrayList<>();

    public CalendarEventAdapter(Context context, ArrayList<CalendarEvent> events) {
        this.context = context;
        dayEvents = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = dayEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return dayEvents.size();
    }

    public void setList(ArrayList<CalendarEvent> list) {
        dayEvents = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView appointmentPatient;
        private TextView appointmentStartTime;
        private TextView appointmentEndTime;
        private TextView appointmentProcedure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentStartTime = itemView.findViewById(R.id.startTime);
            appointmentEndTime = itemView.findViewById(R.id.endTime);

            appointmentPatient = itemView.findViewById(R.id.patientName);
            appointmentProcedure = itemView.findViewById(R.id.event);
        }

        public void bind(CalendarEvent event) {
            String patientName = event.getPatient();
            if (patientName.length() > 20) {
                patientName = patientName.substring(0, 20) + "...";
            }
            appointmentPatient.setText(patientName);
            appointmentProcedure.setText(event.getProcedure());
            appointmentStartTime.setText(event.getStartTime());
            appointmentEndTime.setText(event.getEndTime());
        }
    }

}
