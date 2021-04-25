package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import by.kirill.uskov.medsched.adapters.CalendarEventAdapter;
import by.kirill.uskov.medsched.customLayout.CustomCalendarView;
import by.kirill.uskov.medsched.dialogs.AddAppointmentDialog;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.models.CalendarEvent;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private DatabaseReference databaseReference;
    private BottomNavigationView bottomNavigationView;
    private CustomCalendarView customCalendarView;
    private RecyclerView selectedDayAppointments;

    private CalendarEventAdapter adapter;

    private ArrayList<CalendarEvent> appointments = new ArrayList<>();

    private Handler mHandler = new Handler();

    private Runnable eventUpdaterRunnable = new Runnable() {
        public void run() {
            setAppointmentsToMonth();
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        customCalendarView = findViewById(R.id.custom_calendar_view);

        customCalendarView.setFragmentManager(getSupportFragmentManager());

        selectedDayAppointments = findViewById(R.id.selected_day_appointments);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        selectedDayAppointments.setHasFixedSize(false);
        selectedDayAppointments.setLayoutManager(layoutManager);

        adapter = new CalendarEventAdapter(this, appointments);
        selectedDayAppointments.setAdapter(adapter);
        setAppointmentsToMonth();

        mHandler.postDelayed(eventUpdaterRunnable, 500);

        setNavigation();
    }

    @Override
    protected void onPause() {
        // Удаляем Runnable-объект для прекращения задачи
        mHandler.removeCallbacks(eventUpdaterRunnable);
        customCalendarView.kill();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //mHandler.removeCallbacks(eventUpdaterRunnable);
        customCalendarView.kill();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Добавляем Runnable-объект
        mHandler.postDelayed(eventUpdaterRunnable, 500);
    }


    public void addAppointmentClick(View view) {
        Application.getInstance(getApplicationContext()).setFragmentManager(getSupportFragmentManager());
        AddAppointmentDialog dialog = new AddAppointmentDialog(this);
        //SelectTimeDialog dialog = new SelectTimeDialog(this);
        dialog.show(getSupportFragmentManager(), "Добавить запись");
    }

    private void setAppointmentsToMonth() {
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
                    String eventDate = event.getDate().replaceAll(" ", "");
                    String date = by.kirill.uskov.medsched.models.Application.getInstance().getSelectedDate();
                    if (date != null) {
                        if (eventDate.contains(date)) {
                            appointments.add(CalendarEvent.copyFrom(event));
                        }
                        Collections.sort(appointments);
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        return;
                    }
                }
                Collections.sort(appointments);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
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
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.currentDaySchedule:
                        startActivity(new Intent(getApplicationContext(), TodayActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }
}