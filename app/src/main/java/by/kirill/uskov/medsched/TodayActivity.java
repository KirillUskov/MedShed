package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Locale;

import by.kirill.uskov.medsched.constants.ContextMenuValue;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.events.RecyclerViewAdapter;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.DateUtil;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class TodayActivity extends AppCompatActivity {

    private int editedEventId = 0;
    private static final String TAG = "TodayActivity";
    private DBUtils dbUtil;
    private DBUtils.DBEvents dbEvents;

    private DatabaseReference databaseReference;

    private TextView month;
    private TextView dayOfWeek;
    private TextView dayOfMonth;
    private TextView appointmentAmount;

    private RecyclerView futureAppointmentsRV;
    private BottomNavigationView bottomNavigationView;

    private ArrayList<Event> appointments;

    private RecyclerViewAdapter swipeAdapter;

    private Event editedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_today);

        dbUtil = new DBUtils(getApplicationContext());
        try {
            dbEvents = dbUtil.getE();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        appointmentAmount = findViewById(R.id.appointmentAmount);
        futureAppointmentsRV = findViewById(R.id.todaySchedule);

        appointments = new ArrayList<>();

        if (Application.getInstance().getTodayAppointments().size() > 0) {
            appointments = Application.getInstance().getTodayAppointments();
        } else if (dbEvents != null) {
            appointments = dbEvents.getList();
        }

        swipeAdapter = new RecyclerViewAdapter(this, appointments);
        futureAppointmentsRV.setAdapter(swipeAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        futureAppointmentsRV.setHasFixedSize(false);

        futureAppointmentsRV.setLayoutManager(layoutManager);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        dayOfMonth = findViewById(R.id.dayOfMonth);
        dayOfWeek = findViewById(R.id.dayOfWeek);
        month = findViewById(R.id.monthText);

        setAppointments();

        registerForContextMenu(futureAppointmentsRV);

        futureAppointmentsRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editedEventId = v.getId();
            }
        });
        setViewDate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavState();
    }

    private void setViewDate() {
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String fd = df.format(c);

        dayOfMonth.setText(fd.substring(0, fd.indexOf("-")));
        String m = fd.substring(fd.indexOf("-") + 1, fd.lastIndexOf("-"));
        int mId = Integer.parseInt(m) - 1;
        month.setText(DateUtil.mon[mId].toUpperCase());

        int dayId = c.getDay();
        dayOfWeek.setText(DateUtil.daysOfWeek[dayId]);
    }

    private void setAppointments() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Sched");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (appointments.size() > 0) {
                        appointments.clear();
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Event event = ds.getValue(Event.class);
                        event.setId(ds.getKey());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        Date date = new Date(System.currentTimeMillis());

                        if (event.getDate().replaceAll(" ", "").contains(formatter.format(date))) {
                            // If today appointment has "MOVE" status (it was moved on today), we set status "NO"
                            if (event.getStatus() == AppointmentStatus.MOVE) {
                                event.setStatus(AppointmentStatus.NO.toString());
                                updateEvent(event);
                            }
                            appointments.add(event);
                            swipeAdapter.notifyDataSetChanged();
                        }
                    }
                    Collections.sort(appointments);

                    //TEST
                    swipeAdapter.setEvents(appointments);
                    setAppointmentsAmount();
                    swipeAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, ContextMenuValue.EDIT, 0, ContextMenuValue.EDIT_TEXT);
        menu.add(0,ContextMenuValue.VIEW, 0, ContextMenuValue.VIEW_TEXT);
        menu.add(0, ContextMenuValue.DELETE, 0, ContextMenuValue.DELETE_TEXT);
        switch (v.getId()) {
            case R.id.todaySchedule:
                menu.add(0, ContextMenuValue.PUSH_TO_EXPIRED, 0, ContextMenuValue.PUSH_TO_EXPIRED_TEXT);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id;
        switch (item.getItemId()){
            case ContextMenuValue.EDIT:
                /*
                    Open edit appointment act
                 */
                return true;
            case ContextMenuValue.VIEW:
                //this.item = String.valueOf(item.getItemId());
                //viewAppintmentInformantion();
                return true;
            case ContextMenuValue.DELETE:
                id = swipeAdapter.getPosition();
                removeEvent(appointments.get(id));
                return true;
            case ContextMenuValue.PUSH_TO_EXPIRED:
                id = swipeAdapter.getPosition();
                Event event = appointments.get(id);
                event.setStatus(AppointmentStatus.DO.toString());
                updateEvent(event);
                return true;
            default:
                return false;
        }
    }

    private void setAppointmentsAmount() {
        int count = 0;
        for (Event appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.NO) {
                count++;
            }
        }
        appointmentAmount.setText("Осталось: " + count);
    }

    public void undoAppointmentEditing(Event event) {
        dbEvents.undo(event);
        appointments = dbEvents.getList();
        Collections.sort(appointments);
        swipeAdapter.notifyDataSetChanged();
    }

    private void removeEvent(Event event) {
        if (dbEvents.remove(event)) {
            appointments = dbEvents.getList();
        }
        Collections.sort(appointments);
        swipeAdapter.notifyDataSetChanged();

        Snackbar.make(futureAppointmentsRV, "Запись пациента " + event.getPatient() + " была удалена!", BaseTransientBottomBar.LENGTH_LONG)
                .setAction("Отменить", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoAppointmentEditing(event);
                        setAppointments();
                    }
                })
                .show();
    }

    private void updateEvent(Event event) {
        dbEvents.update(event);
        appointments = dbEvents.getList();
        Collections.sort(appointments);
        swipeAdapter.notifyDataSetChanged();
    }

    private void setNavState() {
        bottomNavigationView.setSelectedItemId(R.id.currentDaySchedule);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.calendarSchedule:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.patientsActivity:
                        startActivity(new Intent(getApplicationContext(), PatientsActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.currentDaySchedule:
                        return true;
                }
                return false;
            }
        });
    }


    public void goToTheSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}