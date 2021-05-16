package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import by.kirill.uskov.medsched.adapters.CalendarEventAdapter;
import by.kirill.uskov.medsched.customLayout.CustomCalendarView;
import by.kirill.uskov.medsched.dialogs.AddAppointmentDialog;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.CalendarEvent;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private DatabaseReference databaseReference;
    private DBUtils dbUtil;
    private BottomNavigationView bottomNavigationView;
    private CustomCalendarView customCalendarView;
    private RecyclerView selectedDayAppointments;

    private TextView noAppointmentsTextView;

    private CalendarEventAdapter adapter;

    private String selectedDate;

    private ArrayList<CalendarEvent> appointments = new ArrayList<>();
    private ArrayList<Event> allAppointments = new ArrayList<>();

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            selectedDate = by.kirill.uskov.medsched.models.Application.getInstance().getSelectedDate();
            ArrayList<Event> events = new ArrayList<>();
            setAppointmentsToSelectedMonth();
            for (CalendarEvent e : appointments) {
                events.add(new Event(e.getPatient(), e.getDate(), e.getStartTime(), e.getEndTime(), AppointmentStatus.NO.toString()));
            }
            by.kirill.uskov.medsched.models.Application.getInstance().setTodayAppointments(events);
            by.kirill.uskov.medsched.models.Application.getInstance().setAllAppointments(allAppointments);
            handler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbUtil = new DBUtils();

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_calendar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        customCalendarView = findViewById(R.id.custom_calendar_view);

        noAppointmentsTextView = findViewById(R.id.no_appointments_text_view);

        customCalendarView.setFragmentManager(getSupportFragmentManager());

        selectedDayAppointments = findViewById(R.id.selected_day_appointments);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        selectedDayAppointments.setHasFixedSize(false);
        selectedDayAppointments.setLayoutManager(layoutManager);

        adapter = new CalendarEventAdapter(this, appointments);
        selectedDayAppointments.setAdapter(adapter);

        //setAppointmentsToMonth();

        handler.postDelayed(runnable, 50);
        setNavigation();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date dateD = new Date(System.currentTimeMillis());
        by.kirill.uskov.medsched.models.Application.getInstance().setSelectedDate(formatter.format(dateD));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 50);
    }

    @Override
    protected void onDestroy() {
        //mHandler.removeCallbacks(eventUpdaterRunnable);
        customCalendarView.kill();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void setRVVisibility() {
        if (appointments.size() == 0) {
            selectedDayAppointments.setVisibility(View.GONE);
            noAppointmentsTextView.setText("Записи отсутсвуют.\nНажмите + чтобы создать запись.");
            noAppointmentsTextView.setVisibility(View.VISIBLE);
        } else {
            selectedDayAppointments.setVisibility(View.VISIBLE);
            noAppointmentsTextView.setVisibility(View.GONE);
        }
    }

    public void addAppointmentClick(View view) {
        Application.getInstance(getApplicationContext()).setFragmentManager(getSupportFragmentManager());
        AddAppointmentDialog dialog = new AddAppointmentDialog(this);
        //SelectTimeDialog dialog = new SelectTimeDialog(this);
        dialog.show(getSupportFragmentManager(), "Добавить запись");
    }

    private void setAppointmentsToMonth() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getUserSchedCode());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (appointments.size() > 0) {
                        appointments.clear();
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Event event = ds.getValue(Event.class);
                        event.setId(ds.getKey());
                        String eventDate = event.getDate().replaceAll(" ", "");
                        if (selectedDate == null) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                            Date dateD = new Date(System.currentTimeMillis());
                            selectedDate = formatter.format(dateD);
                        }
                        allAppointments.add(event);
                        if (selectedDate != null) {
                            if (eventDate.contains(selectedDate)) {
                                appointments.add(CalendarEvent.copyFrom(event));
                            }
                            Collections.sort(appointments);
                            adapter.notifyDataSetChanged();
                        } else {
                            return;
                        }
                    }
                    Collections.sort(appointments);
                    adapter.notifyDataSetChanged();
                    setRVVisibility();
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

    private void setAppointmentsToSelectedMonth() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getUserSchedCode());

            databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (appointments.size() > 0) {
                            appointments.clear();
                        }
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Event event = ds.getValue(Event.class);
                            event.setId(ds.getKey());
                            String eventDate = event.getDate().replaceAll(" ", "");
                            if (selectedDate == null) {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                                Date dateD = new Date(System.currentTimeMillis());
                                selectedDate = formatter.format(dateD);
                            }
                            allAppointments.add(event);
                            if (selectedDate != null) {
                                if (eventDate.contains(selectedDate)) {
                                    appointments.add(CalendarEvent.copyFrom(event));
                                }
                                Collections.sort(appointments);
                                adapter.notifyDataSetChanged();
                            } else {
                                return;
                            }
                        }
                        Collections.sort(appointments);
                        adapter.notifyDataSetChanged();
                        setRVVisibility();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void setNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.calendarSchedule);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.calendarSchedule:
                        return true;
                    case R.id.patientsActivity:
                        startActivity(new Intent(getApplicationContext(), PatientsActivity.class));
                        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();
                        return true;
                    case R.id.currentDaySchedule:
                        startActivity(new Intent(getApplicationContext(), TodayActivity.class));
                        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }
}