package by.kirill.uskov.medsched.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.events.RecyclerViewAdapter;
import by.kirill.uskov.medsched.models.Time;

public class SelectTimeAdapter extends RecyclerView.Adapter<SelectTimeAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Time> freeTime = new ArrayList<>();
    private ArrayList<Time> selected = new ArrayList<>();
    private int position;

    public SelectTimeAdapter(Context context, ArrayList<Time> freeTime) {
        this.context = context;
        this.freeTime = freeTime;
    }

    public void setFreeTime(ArrayList<Time> freeTime) {
        this.freeTime = freeTime;
        notifyDataSetChanged();
    }

    public ArrayList<Time> getAll() {
        return freeTime;
    }
    
    public ArrayList<Time> getSelected() {
        selected = new ArrayList<>();
        for (Time time: freeTime) {
            if(time.getSelected()) {
                selected.add(time);
            }
        }
        return selected;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.time_item, parent, false);
        return new SelectTimeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Time time = freeTime.get(position);
        holder.bind(time);
    }

    @Override
    public int getItemCount() {
        return freeTime.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView appointmentStartTime;
        private TextView appointmentEndTime;
        private ImageView imageCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appointmentEndTime = itemView.findViewById(R.id.end_time);
            appointmentStartTime = itemView.findViewById(R.id.start_time);
            imageCheckBox = itemView.findViewById(R.id.checkBox);
        }

        public void bind(Time time) {
            String timeText = time.getTime();

            imageCheckBox.setVisibility(time.getSelected() ? View.VISIBLE : View.INVISIBLE);

            String startTime = timeText.substring(0, timeText.indexOf(" ")).replaceAll(" ", "");
            String endTime = timeText.substring(timeText.lastIndexOf(" ")).replaceAll(" ", "");

            appointmentStartTime.setText(startTime);
            appointmentEndTime.setText(endTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    time.setSelected(!time.getSelected());
                    imageCheckBox.setVisibility(time.getSelected() ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }

    }
}
