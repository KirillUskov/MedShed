package by.kirill.uskov.medsched.entities.patients;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import by.kirill.uskov.medsched.R;

public class SwipeRecyclerViewAdapter extends RecyclerView.Adapter<SwipeRecyclerViewAdapter.SwipeViewHolder> {
        private Context mContext;
        private ArrayList<Patient> patients;

        public SwipeRecyclerViewAdapter(Context context, ArrayList<Patient> patients){
            mContext = context;
            this.patients = patients;
        }

        @NonNull
        @Override
        public SwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.swipe_patient_layout, parent,false);
            return new SwipeViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull SwipeViewHolder holder, int position) {
            Patient patient = patients.get(position);
            holder.bind(patient);
        }
        @Override
        public int getItemCount() {
            return patients.size();
        }

        public void setPatients(ArrayList<Patient> patients) {
            this.patients = patients;
            notifyDataSetChanged();
        }

        public class SwipeViewHolder extends RecyclerView.ViewHolder {
            private TextView name;
            private TextView phone;

            public SwipeViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.patient_name);
                phone = itemView.findViewById(R.id.patient_phone);
            }

            public void bind(Patient patient) {
                name.setText(patient.name);
                phone.setText(patient.phone);
            }
        }

}
