package by.kirill.uskov.medsched;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class ViewAppointmentDataActivity extends AppCompatActivity {
    private static final String TAG = "ViewAppointmentDataActivity";
    private DBUtils dbUtil;
    private DBUtils.DBEvents dbEvents;
    private DBUtils.DBPatient dbPatient;

    private DatabaseReference databaseReference;

    private Button updateButton;
    private Button closeButton;
    private Button viewPatientButton;

    private EditText procedure;
    private TextView dateTextView;
    private TextView sTime;
    private TextView eTime;

    private ImageView dateImgView;
    private ImageView timeImgView;

    private Spinner statusSpinner;

    private AutoCompleteTextView patientNameAutocomplete;

    private Event event;
    private Patient patient = null;

    private ArrayList<String> statuses;
    private ArrayList<Patient> patients = new ArrayList<>();
    private ArrayList<String> patsNames = new ArrayList<>();

    private DatePickerDialog datePickerDialog;

    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapterAutotext;

    //private Runnable runnable;

    private boolean currentState;

    private Handler handler = new Handler();

    private Event newEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_view_appointment_data);

        event = Application.getInstance().getEvent();

        dbUtil = new DBUtils(getApplicationContext());
        dbEvents = dbUtil.getE();
        dbEvents.setAll();
        dbPatient = dbUtil.getP();

        updateButton = findViewById(R.id.edit_button);
        closeButton = findViewById(R.id.close_button);
        viewPatientButton = findViewById(R.id.view_patient);

        patientNameAutocomplete = findViewById(R.id.patient_name_autocomplete_text);
        procedure = findViewById(R.id.procedure_name);
        dateTextView = findViewById(R.id.editTextDate);
        sTime = findViewById(R.id.editTextStartTime);
        eTime = findViewById(R.id.editTextEndTime);

        statusSpinner = findViewById(R.id.spinner_status);
        statusSpinner.setBackgroundResource(ThemeUtil.getInstance().isDarkTheme() ?
                android.R.drawable.editbox_dropdown_dark_frame :
                android.R.drawable.editbox_dropdown_light_frame);

        dateImgView = findViewById(R.id.calendar_picker);
        timeImgView = findViewById(R.id.time_picker);

        dateTextView.setEnabled(false);
        sTime.setEnabled(false);
        eTime.setEnabled(false);

        getAllPatients();

        patientNameAutocomplete.setText(event.getPatient());
        procedure.setText(event.getProcedure());
        dateTextView.setText(event.getDate());
        sTime.setText(event.getStartTime());
        eTime.setText(event.getEndTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        statuses = new ArrayList<>();
        statuses.add(AppointmentStatus.DO.getName());
        statuses.add(AppointmentStatus.MOVE.getName());
        statuses.add(AppointmentStatus.CANCEL.getName());
        statuses.add(AppointmentStatus.NO.getName());

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        statusSpinner.setAdapter(adapter);

        Log.i("EVENT STATUS", event.getStatus().toString());
        int position = adapter.getPosition(event.getStatus().getName());
        Log.i("EVENT STATUS POSITION", String.valueOf(position));
        statusSpinner.setSelection(position);

        for (Patient p : patients) {
            patsNames.add(p.getName());
        }
        adapterAutotext = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, patsNames);
        patientNameAutocomplete.setThreshold(1);
        patientNameAutocomplete.setAdapter(adapterAutotext);

        setEditTextState(false);

        viewPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPatient(v);
            }
        });
        /*runnable = new Runnable() {
            public void run() {
                setIntermediateItemValues();
                handler.postDelayed(this, 100);
            }
        };*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEditTextState(currentState);
        try {
            setIntermediateItemValues();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //patientNameAutocomplete.removeCallbacks(runnable);
    }

    private void initDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String monthText = String.valueOf(month);
                if (monthText.length() == 1) {
                    monthText = "0" + monthText;
                }
                String dayText = String.valueOf(dayOfMonth);
                if (dayText.length() == 1) {
                    dayText = "0" + dayText;
                }
                String date = dayText + "." + monthText + "." + year;
                dateTextView.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Locale.setDefault(new Locale("ru"));
        int style = ThemeUtil.getInstance().isDarkTheme() ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    public void setNewMode(View view) {
        String text = updateButton.getText().toString().toUpperCase();
        if (text.equals("ИЗМЕНИТЬ")) {
            currentState = true;
            setEditTextState(true);
            updateButton.setText("ОБНОВИТЬ");
        } else {
            // String patient, String date, String startTime, String endTime, String status
            newEvent = new Event(event.getPatient(),
                    event.getDate(),
                    sTime.getText().toString(),
                    eTime.getText().toString(),
                    event.getStatus().toString());
            newEvent.setId(event.getId());
            if (updateAppointment(newEvent)) {
                currentState = false;
                setEditTextState(false);
                updateButton.setText("ИЗМЕНИТЬ");
            }
        }
    }

    public void close(View view) {
        startActivity(new Intent(this, TodayActivity.class));
        //overridePendingTransition(0, 0);
        finish();
    }

    public void setEditTextState(boolean state) {
        viewPatientButton.setVisibility(state ? View.INVISIBLE : View.VISIBLE);
        patientNameAutocomplete.setClickable(state);
        patientNameAutocomplete.setEnabled(state);
        procedure.setClickable(state);
        procedure.setEnabled(state);
        dateTextView.setClickable(state);
        sTime.setClickable(state);
        eTime.setClickable(state);
        statusSpinner.setClickable(state);
        statusSpinner.setEnabled(state);
        timeImgView.setVisibility(!state ? View.INVISIBLE : View.VISIBLE);
        timeImgView.setEnabled(state);
        dateImgView.setEnabled(state);
        dateImgView.setVisibility(!state ? View.INVISIBLE : View.VISIBLE);
    }

    public boolean updateAppointment(Event editedEvent) {
        String patientNameText = patientNameAutocomplete.getText().toString();
        String startTime = sTime.getText().toString();
        String endTime = eTime.getText().toString();
        String appointmentDate = dateTextView.getText().toString();
        String appointmentProcedure = procedure.getText().toString();
        String status = statusSpinner.getSelectedItem().toString();
        status = status.equals(AppointmentStatus.NO.getName()) ? AppointmentStatus.NO.toString() :
                status.equals(AppointmentStatus.DO.getName()) ? AppointmentStatus.DO.toString() :
                        status.equals(AppointmentStatus.MOVE.getName()) ? AppointmentStatus.MOVE.toString() : AppointmentStatus.CANCEL.toString();
        Log.i(TAG, "Edited status:" + status);

        Event newEvent = new Event(patientNameText, appointmentProcedure, appointmentDate, startTime, endTime, status);

        for (Event e: dbEvents.getList()) {
            if(e.getDate().contains(appointmentDate) && (e.getStartTime().contains(startTime) || e.getEndTime().contains(endTime)))
            {
                Toast.makeText(getApplicationContext(), "Данное время занято, пожалуйста, измените время записи", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (!(newEvent.getPatient().equals(editedEvent.getPatient())) || !(newEvent.getDate().equals(editedEvent.getDate())) || !(newEvent.getStatus().equals(editedEvent.getStatus())) ||
                !(newEvent.getStartTime().equals(editedEvent.getStartTime())) || !(newEvent.getEndTime().equals(editedEvent.getEndTime())) || !(newEvent.getProcedure().equals(editedEvent.getProcedure()))) {

            /*if (!newEvent.getDate().equals(editedEvent.getDate())) {
                statusSpinner.setSelection(adapter.getPosition(AppointmentStatus.MOVE.getName()));
                //Toast.makeText(this, "Измените дату!", Toast.LENGTH_LONG).show();
            }*/
            if (!(newEvent.getDate().equals(editedEvent.getDate()))) {
                statusSpinner.setSelection(adapter.getPosition(AppointmentStatus.MOVE.getName()));
                newEvent.setStatus(AppointmentStatus.MOVE.toString());
            }
            newEvent.setId(editedEvent.getId());
            dbEvents.update(newEvent);
            Toast.makeText(this, "Запись успешно обновлена", Toast.LENGTH_LONG).show();

        }
        return true;

    }

    public void viewPatient(View view) {
        Patient patient = null;
        for (Patient p : patients) {
            Log.i("PAT NAME", "\"" + p.getName() + "\"");
            Log.i("IS PAT CORRECT ", String.valueOf(p.getName().equals(patientNameAutocomplete.getText().toString())));
            if (p.getName().equals(patientNameAutocomplete.getText().toString())) {
                // String name, String bdDate, String phone, String comment
                patient = new Patient(p.getName(), p.getBdDate(), p.getPhone(), p.getComment());
            }
        }

        Application.getInstance().setPatient(patient);
        startActivity(new Intent(getApplicationContext(), ViewPatientDataActivity.class));
        overridePendingTransition(0, 0);
    }

    private void getAllPatients() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getUserPatCode());

            ValueEventListener vListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (patients.size() > 0) {
                        patients.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Patient patient = ds.getValue(Patient.class);
                        assert patient != null;
                        patient.setId(ds.getKey());
                        patients.add(patient);
                        patsNames.add(patient.getName());
                        Collections.sort(patients);
                    }
                    adapterAutotext.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            databaseReference.addValueEventListener(vListener);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }


    public void setDate(View view) {
        initDatePickerDialog();
        datePickerDialog.show();
    }

    public void setTime(View view) {
        IntermediateEvent.getInstance()
                .setDate(dateTextView.getText().toString())
                .setPatient(patientNameAutocomplete.getText().toString())
                .setProcedure(procedure.getText().toString());
        startActivity(new Intent(this, SelectTimeActivity.class));

        //handler.postDelayed(runnable, 500);
    }

    private void setIntermediateItemValues() {
        if (IntermediateEvent.getInstanceOrNull() != null && IntermediateEvent.getInstance().getStartTime() != null && IntermediateEvent.getInstance().getEndTime() != null) {
            sTime.setText(IntermediateEvent.getInstance().getStartTime());
            eTime.setText(IntermediateEvent.getInstance().getEndTime());
            IntermediateEvent.getInstance().setIntermediateEventNull();
        }
    }
}