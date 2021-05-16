package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

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

public class TodayActivity extends AppCompatActivity implements RecyclerViewAdapter.OnEventListener {

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

    private TextView emptyView;

    private ArrayList<Event> appointments;

    private RecyclerViewAdapter swipeAdapter;

    private Event editedEvent;

    private Handler setDateHandler = new Handler();

    private Runnable setDateRunnable = new Runnable() {
        @Override
        public void run() {
            setViewDate();

            setDateHandler.postDelayed(this, 1000);
        }
    };


    private PopupMenu.OnMenuItemClickListener completedMenuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id;
            switch (item.getItemId()) {
                case R.id.edit_appointment:
                    id = swipeAdapter.getPosition();
                    Application.getInstance().setEvent(appointments.get(id));
                    startActivity(new Intent(getApplicationContext(), ViewAppointmentDataActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.delete_appointment:
                    id = swipeAdapter.getPosition();
                    removeEvent(appointments.get(id));
                    appointments.remove(id);
                    swipeAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    };

    private PopupMenu.OnMenuItemClickListener futureMenuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id;
            switch (item.getItemId()) {
                case R.id.edit_appointment:
                    id = swipeAdapter.getPosition();
                    Application.getInstance().setEvent(appointments.get(id));
                    startActivity(new Intent(getApplicationContext(), ViewAppointmentDataActivity.class));
                    overridePendingTransition(R.anim.nav_default_pop_exit_anim, R.anim.nav_default_pop_enter_anim);
                    finish();
                    break;
                case R.id.delete_appointment:
                    id = swipeAdapter.getPosition();
                    removeEvent(appointments.get(id));
                    appointments.remove(id);
                    swipeAdapter.notifyDataSetChanged();
                    break;
                case R.id.set_completed_appointment:
                    id = swipeAdapter.getPosition();
                    Event event = appointments.get(id);
                    event.setStatus(AppointmentStatus.DO.toString());
                    updateEvent(appointments.get(id));
                    Collections.sort(appointments);
                    swipeAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    };

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

        swipeAdapter = new RecyclerViewAdapter(appointments, this);
        futureAppointmentsRV.setAdapter(swipeAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        futureAppointmentsRV.setLayoutManager(layoutManager);

        futureAppointmentsRV.setHasFixedSize(false);
        futureAppointmentsRV.setEnabled(true);
        futureAppointmentsRV.setClickable(true);

        setAppointments();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        dayOfMonth = findViewById(R.id.dayOfMonth);
        dayOfWeek = findViewById(R.id.dayOfWeek);
        month = findViewById(R.id.monthText);

        emptyView = findViewById(R.id.empty_view);

        setViewDate();

        setDateHandler.postDelayed(setDateRunnable, 1000);

        setNavState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAppointments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setDateHandler.removeCallbacks(setDateRunnable);
    }

    private void setRVVisibility() {
        if (appointments.size() == 0) {
            futureAppointmentsRV.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            futureAppointmentsRV.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
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
                    swipeAdapter.notifyDataSetChanged();
                    setAppointmentsAmount();

                    Log.i(TAG, "setAppointments");
                    setRVVisibility();
                    Application.getInstance().setTodayAppointments(appointments);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }

            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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

                    }
                })
                .show();
    }

    private void updateEvent(Event event) {
        dbEvents.update(event);
    }

    private void setNavState() {
        bottomNavigationView.setSelectedItemId(R.id.currentDaySchedule);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.calendarSchedule:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();
                        return true;
                    case R.id.patientsActivity:
                        startActivity(new Intent(getApplicationContext(), PatientsActivity.class));
                        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();
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
        finish();
    }

    @Override
    public void onEventClick(int position, View v) {
        Log.i(TAG, "event click");
        //Application.getInstance().setEvent(appointments.get(position));
        PopupMenu menu = new PopupMenu(getApplicationContext(), v);
        if (appointments.get(position).getStatus() == AppointmentStatus.DO) {
            menu.getMenuInflater().inflate(R.menu.completed_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(completedMenuListener);
        } else {
            menu.getMenuInflater().inflate(R.menu.future_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(futureMenuListener);
        }
        menu.show();
    }
}