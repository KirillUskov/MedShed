package by.kirill.uskov.medsched.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.SelectTimeActivity;
import by.kirill.uskov.medsched.SplashActivity;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.models.Procedure;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class AddAppointmentDialog extends AppCompatDialogFragment {

    private static final String TAG = "AddAppointmentDialog";
    private DBUtils dbUtil;
    private DBUtils.DBEvents dbEvents;
    private DBUtils.DBPatient dbPatient;

    private DatabaseReference databaseReference;

    private ArrayList<String> patients;
    private ArrayList<Patient> patientsObj = new ArrayList<>();
    private ArrayList<String> eventsTime;
    private ArrayList<String> procedureList = new ArrayList<>();

    private AutoCompleteTextView patient;

    private AutoCompleteTextView procedure;

    private TextView dateTextView;
    private TextView sTime;
    private TextView eTime;

    private ImageView timePickerImgView;
    private ImageView datePickerImgView;

    private Button add;
    private Button cancel;

    private Context context;

    private Handler handler = new Handler();
    private Runnable runnable;

    private DatePickerDialog datePickerDialog;

    public AddAppointmentDialog(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "Create");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        dbUtil = new DBUtils(getContext());
        dbEvents = dbUtil.getE();
        dbPatient = dbUtil.getP();
        dbEvents.setAll();
        setProcedures();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_schedule, null);

        builder.setView(view);

        patient = view.findViewById(R.id.autoCompleteName);
        dateTextView = view.findViewById(R.id.editTextDate);
        sTime = view.findViewById(R.id.editTextStartTime);
        eTime = view.findViewById(R.id.editTextEndTime);
        procedure = view.findViewById(R.id.procedure);

        add = view.findViewById(R.id.addButton);
        cancel = view.findViewById(R.id.cancelButton);

        timePickerImgView = view.findViewById(R.id.time_picker);
        datePickerImgView = view.findViewById(R.id.calendar_picker);

        patients = new ArrayList<>();
        eventsTime = new ArrayList<>();

        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("__.__.____");
        FormatWatcher formatWatcher = new MaskFormatWatcher( // форматировать текст будет вот он
                MaskImpl.createTerminated(slots)
        );
        formatWatcher.installOn(dateTextView);

        Slot[] date = new UnderscoreDigitSlotsParser().parseSlots("__:__");
        formatWatcher = new MaskFormatWatcher( // форматировать текст будет вот он
                MaskImpl.createTerminated(date)
        );
        formatWatcher.installOn(sTime);
        Slot[] secondTime = new UnderscoreDigitSlotsParser().parseSlots("__:__");
        formatWatcher = new MaskFormatWatcher( // форматировать текст будет вот он
                MaskImpl.createTerminated(secondTime)
        );
        formatWatcher.installOn(eTime);

        setPatients();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, patients);
        patient.setThreshold(1);
        patient.setAdapter(adapter);

        ArrayAdapter<String> procedureAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, procedureList);
        procedure.setThreshold(1);
        procedure.setAdapter(procedureAdapter);

        setOnClickListeners();


        if(Application.getInstance().getSelectedDate() != null) {
            dateTextView.setText(Application.getInstance().getSelectedDate());
        }
        return builder.create();
    }

    private void setOnClickListeners() {
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAppointment();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        timePickerImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntermediateEvent.getInstance()
                                    .setDate(dateTextView.getText().toString())
                                    .setPatient(patient.getText().toString())
                                    .setProcedure(procedure.getText().toString());
                startActivity(new Intent(getContext(), SelectTimeActivity.class));
                runnable = new Runnable() {
                    public void run() {
                        setIntermediateItemValues();
                        handler.postDelayed(this, 500);
                    }
                };
                handler.postDelayed(runnable, 500);
            }
        });

        datePickerImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatePickerDialog();
            }
        });
    }


    private void setIntermediateItemValues() {
        if (IntermediateEvent.getInstanceOrNull() != null && IntermediateEvent.getInstance().getStartTime() != null && IntermediateEvent.getInstance().getEndTime() != null) {
            sTime.setText(IntermediateEvent.getInstance().getStartTime());
            eTime.setText(IntermediateEvent.getInstance().getEndTime());
            IntermediateEvent.getInstance().setIntermediateEventNull();
        }
    }

    private void initDatePickerDialog() {
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
                dateTextView.setText(date);

            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Locale.setDefault(new Locale("ru"));
        int style = ThemeUtil.getInstance().isDarkTheme() ? android.app.AlertDialog.THEME_HOLO_DARK : android.app.AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(context, style, dateSetListener, year, month, day);

        datePickerDialog.show();
    }

    /*private void setList() {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Sched");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    if (event != null) {
                        Log.d(TAG, "ON DATA CHANGE getInstanceOrNull");

                        Log.d(TAG, "ON DATA CHANGE");
                        String date = IntermediateEvent.getInstanceOrNull().getDate();
                        if (event.getDate().replaceAll(" ", "").contains(date)) {
                            Log.d(TAG, date);
                            String time = event.getStartTime() + " - " + event.getEndTime();
                            eventsTime.add(time);
                        }
                        Collections.sort(eventsTime);
                        Log.d(TAG, "eventsTime.size():  " + eventsTime.size());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });

    }*/

    public void setProcedures() {
        databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getuserProceduresCode());
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (procedureList.size() > 0) {
                        procedureList.clear();
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Procedure procedure = ds.getValue(Procedure.class);
                        procedure.setId(ds.getKey());
                        procedureList.add(procedure.name);
                    }
                    Collections.sort(procedureList);
                }
                else {
                }
            }
        });
    }

    private void setPatients() {
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
                    patientsObj.add(patient);
                    patients.add(patient.name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(vListener);
    }

    @SuppressLint("ResourceAsColor")
    public void addAppointment() {
        try {
            boolean isError = false;
            String patientName = patient.getText().toString();
            // TODO add patient existing checking
            String startTime = sTime.getText().toString();
            String endTime = eTime.getText().toString();
            String appointmentDate = dateTextView.getText().toString();
            String appointmentProcedure = procedure.getText().toString();


            if (patientName.length() == 0) {
                isError = true;
                patient.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patient.setBackgroundResource(R.drawable.bg_custom_white);
            }
            if (appointmentProcedure.length() == 0) {
                isError = true;
                procedure.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                procedure.setBackgroundResource(R.drawable.bg_custom_white);
            }
            if(!(startTime.matches(".*\\d.*") && endTime.matches(".*\\d.*"))) {
                isError = true;
                sTime.setTextColor(Color.parseColor("#A82929"));
                eTime.setTextColor(Color.parseColor("#A82929"));
            } else {
                sTime.setTextColor(R.attr.mainTextColor);
                eTime.setTextColor(R.attr.mainTextColor);
            }
            if(!appointmentDate.matches(".*\\d.*")) {
                isError = true;
                dateTextView.setTextColor(Color.parseColor("#A82929"));
            } else {
                dateTextView.setTextColor(R.attr.mainTextColor);
            }

            if(!patients.contains(patientName)) {
                isError = true;
                patient.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
                Toast.makeText(getContext(), "Данного пациента не существует", Toast.LENGTH_LONG).show();
            }

            if(!isError) {
                String phoneNum = patientsObj.get(patients.indexOf(patientName)).getPhone();

                Event newEvent = new Event(patientName, appointmentProcedure, appointmentDate, startTime, endTime, AppointmentStatus.NO.name());
                newEvent.setPhoneNumber(phoneNum);


                if (dbEvents.isAppointmentsToDateIncludeAppointmentForPatient(appointmentDate, patientName) ||
                        dbEvents.isTodayAppointmentsIncludeAppointmentForPatient(
                                by.kirill.uskov.medsched.models.Application.getInstance().getAllAppointments(), patientName, appointmentDate)) {
                    Toast.makeText(getContext(), "Запись для данного пациента уже есть", Toast.LENGTH_LONG).show();
                } else {
                    dbEvents.add(newEvent);

                    handler.removeCallbacks(runnable);
                    Toast.makeText(getContext(), "Запись была создана", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            } else {
                if(patients.contains(patientName)) {
                    Toast.makeText(getContext(), "Заполните все обязательные поля", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Toast.makeText(getContext(), "Запись не была создана", Toast.LENGTH_LONG).show();
        }
    }
}
