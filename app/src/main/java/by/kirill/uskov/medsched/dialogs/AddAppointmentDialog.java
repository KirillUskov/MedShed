package by.kirill.uskov.medsched.dialogs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.SelectTimeActivity;
import by.kirill.uskov.medsched.SplashActivity;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.utils.DBUtils;
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
    private ArrayList<String> eventsTime;

    private AutoCompleteTextView patient;
    private EditText dateTextView;
    private EditText sTime;
    private EditText eTime;
    private EditText procedure;

    private Button add;
    private Button cancel;

    private Context context;

    private Handler handler = new Handler();
    private Runnable runnable;

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

        sTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //setList();
                IntermediateEvent.getInstance()
                                    .setDate(dateTextView.getText().toString())
                                    .setPatient(patient.getText().toString())
                                    .setProcedure(procedure.getText().toString());
                /*setList();
                IntermediateEvent.getInstance()
                                    .setEventTime(eventsTime);*/
                startActivity(new Intent(getContext(), SelectTimeActivity.class));
                runnable = new Runnable() {
                    public void run() {
                        setIntermediateItemValues();
                        handler.postDelayed(this, 500);
                    }
                };
                handler.postDelayed(runnable, 500);
                return true;
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

    private void setPatients() {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Pat");

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (patients.size() > 0) {
                    patients.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Patient patient = ds.getValue(Patient.class);
                    assert patient != null;
                    patients.add(patient.name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(vListener);
    }

    public void addAppointment() {
        String patientName = patient.getText().toString();
        String startTime = sTime.getText().toString();
        String endTime = eTime.getText().toString();
        String appointmentDate = dateTextView.getText().toString();
        String appointmentProcedure = procedure.getText().toString();

        Event newEvent = new Event(patientName, appointmentProcedure, appointmentDate, startTime, endTime, AppointmentStatus.NO.name());
        dbEvents.add(newEvent);

        handler.removeCallbacks(runnable);
        dismiss();
    }
}
