package by.kirill.uskov.medsched;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private DBUtils.DBEvents dbEvents;

    private EditText patientName;
    private EditText patientBD;
    private EditText patientPhone;
    private EditText patientComment;

    private ImageView messageImgView;

    private Button button;

    private RecyclerView recyclerView;
    private EventForPatientAdapter eventForPatientAdapter;

    private DatabaseReference databaseReference;

    private ArrayList<PatientEvent> appointments;
    private ArrayList<Event> patientAppointments;

    private String userSchedCode;

    private Patient patient = null;

    private DatePickerDialog datePickerDialog;

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

        messageImgView = findViewById(R.id.message_img_view);

        dbUtil = new DBUtils(getApplicationContext());
        dbPatient = dbUtil.getP();
        dbEvents = dbUtil.getE();

        appointments = new ArrayList<>();
        patientAppointments = new ArrayList<>();

        patient = Application.getInstance().getPatient();

        patientName.setText(patient.getName());
        patientBD.setText(patient.getBdDate());
        patientPhone.setText(patient.getPhone());
        patientComment.setText(patient.getComment());

        setEditTextState(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(false);

        eventForPatientAdapter = new EventForPatientAdapter(appointments);
        recyclerView.setAdapter(eventForPatientAdapter);
        setAppointments(patient.getName());

        Collections.sort(appointments);
        eventForPatientAdapter.notifyDataSetChanged();

        messageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("address", patientPhone.getText().toString());*/
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Здравствуйте!\nНапоминаю Вам о записи");
                sendIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, patientPhone.getText().toString());
                sendIntent.putExtra("address", patientPhone.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
        }
        });
        patientBD.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                initDatePicker();
                datePickerDialog.show();
            }

        });
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String monthText = String.valueOf(month);
                if(monthText.length() == 1) {
                    monthText = "0" + monthText;
                }
                String dayText = String.valueOf(dayOfMonth);
                if(dayText.length() == 1) {
                    dayText = "0" + dayText;
                }
                String date = dayText + "." + monthText + "." + year;
                patientBD.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int style = ThemeUtil.getInstance().isDarkTheme() ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private void setAppointments(String nameOfPatient) {
        databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getUserSchedCode());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (appointments.size() > 0) {
                    appointments.clear();
                }
                if (patientAppointments.size() > 0) {
                    patientAppointments.clear();
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    PatientEvent patientEvent = new PatientEvent(event);
                    event.setId(ds.getKey());
                    if (patientEvent.getPatient().equals(nameOfPatient) && patientEvent.getStatus() == AppointmentStatus.DO) {
                        appointments.add(patientEvent);
                    }
                    if(event.getPatient().equals(nameOfPatient)) {
                        patientAppointments.add(event);
                    }
                    Log.i(TAG, "patientAppointments.size = " + patientAppointments.size());
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
            boolean isException = false;
            // public Patient(String name, String bdDate, String phone, String comment) {
            if(patientName.getText().toString().length() == 0) {
                isException = true;
                patientName.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientName.setBackgroundResource(R.drawable.bg_custom_input_layout);
            }
            if(patientBD.getText().toString().length() == 0) {
                isException = true;
                patientBD.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientBD.setBackgroundResource(R.drawable.bg_custom_input_layout);
            }
            if(patientPhone.getText().toString().length() == 0) {
                isException = true;
                patientPhone.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientPhone.setBackgroundResource(R.drawable.bg_custom_input_layout);
            }
            if (isException) {
                Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_LONG).show();
                return;
            }
            Patient newPatient = new Patient(patientName.getText().toString(),
                                            patientBD.getText().toString(),
                                            patientPhone.getText().toString(),
                                            patientComment.getText().toString());
            newPatient.setId(patient.getId());
            Log.i(TAG, String.valueOf(newPatient.getName() != patient.getName()));
            if(newPatient.getName() != patient.getName()) {
                updateAllPatientsAppointments(newPatient);
                patient = new Patient(newPatient.getName(),
                                        newPatient.getBdDate(),
                                        newPatient.getPhone(),
                                        newPatient.getComment());
                Application.getInstance().setPatient(patient);
                setAppointments(patient.getName());
            }
            updatePatient(newPatient);
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
        messageImgView.setVisibility(!state ? View.VISIBLE : View.INVISIBLE);
        messageImgView.setEnabled(!state);
    }

    public void updatePatient(Patient patient) {
        dbPatient.update(patient);
    }

    public void updateAllPatientsAppointments(Patient newPatient) {
        for (Event appointment:patientAppointments) {
            appointment.setPatient(newPatient.getName());
            Log.i("NEW patient name", newPatient.getName());
            dbEvents.update(appointment);
        }
    }

    public void close(View view) {
        finish();
    }
}