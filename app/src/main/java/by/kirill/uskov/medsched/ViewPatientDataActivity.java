package by.kirill.uskov.medsched;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import by.kirill.uskov.medsched.adapters.EventForPatientAdapter;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.entities.patients.SwipeRecyclerViewAdapter;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.PatientEvent;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class ViewPatientDataActivity extends AppCompatActivity {
    private static final String TAG = "ViewPatientDataActivity";


    private DBUtils dbUtil;
    private DBUtils.DBPatient dbPatient;

    private EditText patientName;
    private EditText patientBD;
    private EditText patientPhone;
    private EditText patientComment;

    private Button button;

    private RecyclerView recyclerView;

    private EventForPatientAdapter eventForPatientAdapter;

    private ArrayList<PatientEvent> appointments;
    private DatabaseReference databaseReference;

    private String userSchedCode;

    private Patient patient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_view_patient_data);

        patientName = findViewById(R.id.patient_name_edit_text);
        patientBD = findViewById(R.id.patient_bd_date_edit_text);
        patientPhone = findViewById(R.id.patient_phone_edit_text);
        patientComment = findViewById(R.id.patient_comment_edit_text);

        recyclerView = findViewById(R.id.patient_appointments);

        button = findViewById(R.id.edit_button);

        dbUtil = new DBUtils(getApplicationContext());
        dbPatient = dbUtil.getP();

        appointments = new ArrayList<>();

        patient = Application.getInstance().getPatient();

        patientName.setText(patient.getName());
        patientBD.setText(patient.getBdDate());
        patientPhone.setText(patient.getPhone());
        patientComment.setText(patient.getComment());

        patientName.setClickable(false);
        patientName.setEnabled(false);
        patientBD.setClickable(false);
        patientBD.setEnabled(false);
        patientPhone.setClickable(false);
        patientPhone.setEnabled(false);
        patientComment.setClickable(false);
        patientComment.setEnabled(false);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(false);


        eventForPatientAdapter = new EventForPatientAdapter(appointments);
        recyclerView.setAdapter(eventForPatientAdapter);
        setAppointments(patient.getName());

        Collections.sort(appointments);
        eventForPatientAdapter.notifyDataSetChanged();

    }

    private void setAppointments(String nameOfPatient) {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Sched");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (appointments.size() > 0) {
                    appointments.clear();
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    PatientEvent patientEvent = new PatientEvent(event);
                    event.setId(ds.getKey());
                    if (patientEvent.getPatient().contains(nameOfPatient) && patientEvent.getStatus() == AppointmentStatus.DO) {
                        appointments.add(patientEvent);
                    }
                }
                Collections.sort(appointments);

                eventForPatientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    public void setNewMode(View view) {
        String text = button.getText().toString().toUpperCase();
        if(text.equals("ИЗМЕНИТЬ")) {
            setEditTextState(true);
            button.setText("ОБНОВИТЬ");
        } else {
            // public Patient(String name, String bdDate, String phone, String comment) {
            Patient newPatient = new Patient(patientName.getText().toString(),
                                            patientBD.getText().toString(),
                                            patientPhone.getText().toString(),
                                            patientComment.getText().toString());
            newPatient.setId(patient.getId());
            updateAppointment(newPatient);
            setEditTextState(false);
            button.setText("ИЗМЕНИТЬ");
        }
    }

    public void setEditTextState(boolean state) {
        patientName.setClickable(state);
        patientName.setEnabled(state);
        patientBD.setClickable(state);
        patientBD.setEnabled(state);
        patientPhone.setClickable(state);
        patientPhone.setEnabled(state);
        patientComment.setClickable(state);
        patientComment.setEnabled(state);
    }

    public void updateAppointment(Patient patient) {
        dbPatient.update(patient);
    }

    public void close(View view) {
        startActivity(new Intent(getApplicationContext(), PatientsActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}