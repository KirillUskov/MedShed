package by.kirill.uskov.medsched.entities.events;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import by.kirill.uskov.medsched.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Event> events;
    private int position;

    public RecyclerViewAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.swipe_event_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView appointmentPatient;
        private TextView appointmentStartTime;
        private TextView appointmentEndTime;
        private TextView appointmentProcedure;
        private ImageView statusIcon;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            appointmentPatient = itemView.findViewById(R.id.patientName);
            appointmentStartTime = itemView.findViewById(R.id.startTime);
            appointmentEndTime = itemView.findViewById(R.id.endTime);
            appointmentProcedure = itemView.findViewById(R.id.event);
            statusIcon = itemView.findViewById(R.id.eventStatus);
        }


        public void bind(Event event) {
            switch (event.getStatus()) {
                case DO:
                    statusIcon.setImageResource(R.drawable.ic_done);
                    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                        appointmentPatient.setTextColor(Color.parseColor("#737373"));
                        appointmentProcedure.setTextColor(Color.parseColor("#FF5C5C5C"));
                    } else {
                        appointmentPatient.setTextColor(Color.parseColor("#B8B8B8"));
                        appointmentProcedure.setTextColor(Color.parseColor("#D8D8D8"));
                        appointmentStartTime.setTextColor(Color.parseColor("#D8D8D8"));
                        appointmentEndTime.setTextColor(Color.parseColor("#E3E3E3"));
                    }
                    break;
                case NO:
                    TypedValue typedValue = new TypedValue();
                    Resources.Theme theme = context.getTheme();
                    theme.resolveAttribute(R.attr.mainTextColor, typedValue, true);
                    @ColorInt int mainTextColor = typedValue.data;
                    appointmentPatient.setTextColor(mainTextColor);
                    theme.resolveAttribute(R.attr.secondTextColor, typedValue, true);
                    @ColorInt int secondTextColor = typedValue.data;
                    appointmentProcedure.setTextColor(secondTextColor);
                    appointmentStartTime.setTextColor(Color.parseColor("#5C5C5C"));
                    appointmentEndTime.setTextColor(Color.parseColor("#B8B8B8"));
                    statusIcon.setImageResource(R.drawable.ic_round);
                    break;
            }
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