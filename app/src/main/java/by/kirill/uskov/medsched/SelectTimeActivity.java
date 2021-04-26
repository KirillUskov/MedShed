package by.kirill.uskov.medsched;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import by.kirill.uskov.medsched.adapters.SelectTimeAdapter;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.models.Time;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.DateUtil;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class SelectTimeActivity extends AppCompatActivity {
    private static final String TAG = "SelectTimeDialog";

    private RecyclerView timeRV;
    private Button setButton;

    private DatabaseReference databaseReference;

    private ArrayList<Time> freeTime;
    private ArrayList<Time> selectedTime;
    private ArrayList<String> eventsTime;
    private ArrayList<String> deletedTimes = new ArrayList<>();

    private SimpleDateFormat time = new SimpleDateFormat("HH:mm");

    private SelectTimeAdapter adapter;

    private Handler handler = new Handler();

    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.select_time_layout);

        timeRV = findViewById(R.id.free_time_rv);
        setButton = findViewById(R.id.set_time_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        timeRV.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        timeRV.setHasFixedSize(false);
        timeRV.setLayoutManager(layoutManager);

        eventsTime = new ArrayList<>();
        freeTime = new ArrayList<>();
        selectedTime = new ArrayList<>();

        for (String t : DateUtil.getTimeList()) {
            freeTime.add(new Time(t, false));
        }

        adapter = new SelectTimeAdapter(Application.getInstance().getContext(), freeTime);
        timeRV.setAdapter(adapter);
        adapter.setFreeTime(freeTime);

        setOnClickListener();
        Log.i("SET LISTS", "1");
        setList();

        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                selectedTime = adapter.getSelected();

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void setOnClickListener() {
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick");
                if (selectedTime.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Время не выбрано!", Toast.LENGTH_LONG);
                } else {
                    boolean isProblem = false;
                    try {
                        if (selectedTime.size() == 1) {
                            Date d1 = time.parse(selectedTime.get(0).getStartTime());
                            Date d2 = time.parse(selectedTime.get(0).getEndTime());
                            IntermediateEvent.getInstance()
                                    .setStartTime(selectedTime.get(0).getStartTime())
                                    .setEndTime(selectedTime.get(0).getEndTime());
                            handler.removeCallbacks(runnable);
                            finish();
                        }
                        for (int i = 0; i < selectedTime.size() - 1; i++) {

                            Date d1 = time.parse(selectedTime.get(i).getEndTime());
                            Date d2 = time.parse(selectedTime.get(i + 1).getStartTime());

                            Log.i(TAG, selectedTime.get(i).getEndTime() + " - " + selectedTime.get(i + 1).getStartTime());
                            if (d1.getTime() != d2.getTime()) {
                                Toast.makeText(getApplicationContext(), String.format("Выбраны не последовательные временные промежутки:{0} и {1}",
                                                time.format(d1), time.format(d2)), Toast.LENGTH_LONG);
                                isProblem = true;
                            }

                        }
                    } catch (ParseException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    if (!isProblem) {
                        IntermediateEvent.getInstance()
                                .setStartTime(selectedTime.get(0).getStartTime())
                                .setEndTime(selectedTime.get(selectedTime.size() - 1).getEndTime());
                        handler.removeCallbacks(runnable);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Выбирите последовательные даты!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void setList() {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Sched");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                    Log.d(TAG, "ON DATA CHANGE getInstanceOrNull");
                    if(IntermediateEvent.getInstanceOrNull() == null) {
                        return;
                    }
                    Log.d(TAG, "ON DATA CHANGE");
                    String date = IntermediateEvent.getInstanceOrNull().getDate();
                    if (event.getDate().replaceAll(" ", "").contains(date)) {
                        Log.d(TAG, date);
                        String time = event.getStartTime() + " - " + event.getEndTime();
                        eventsTime.add(time);
                    }
                }
                Collections.sort(eventsTime);
                Log.d(TAG, "eventsTime.size():  " + eventsTime.size());

                boolean isWritable = false;
                if (eventsTime.size() > 0) {
                    for (String tE : eventsTime) {
                        String eventStart = tE.substring(0, tE.indexOf(" ")).replace(" ", "");
                        String eventEnd = tE.substring(tE.lastIndexOf(" ")).replace(" ", "");

                        Log.d(TAG,  eventStart + " - " + eventEnd);

                        for (Time tT : freeTime) {
                            String startTime = tT.getStartTime();
                            String endTime = tT.getEndTime();
                            if (startTime.contains(eventStart)) {
                                isWritable = true;
                            }
                            if (isWritable) {
                                deletedTimes.add(startTime + " - " + endTime);
                            }
                            if (endTime.contains(eventEnd)) {
                                isWritable = false;
                            }
                        }

                        ArrayList<Time> availableTime = new ArrayList<>();

                        for (Time t: freeTime) {
                            availableTime.add(t);
                        }

                        Log.d(TAG, "DELETE");
                        Log.d("deletedTimes", String.valueOf(deletedTimes.size()));
                        for (int i = 0; i < deletedTimes.size(); i++) {
                            Log.d(TAG, deletedTimes.get(i));
                            for (Time t: availableTime) {
                                if (t.getTime().contains(deletedTimes.get(i))) {
                                    Log.d(TAG, t.getTime());
                                    freeTime.remove(t);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    adapter.setFreeTime(freeTime);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, error.getMessage());
                handler.removeCallbacks(runnable);
            }
        });

    }

    private void setAvailableTime() {
        boolean isWritable = false;
        if (eventsTime.size() > 0) {
            for (String tE : eventsTime) {
                String eventStart = tE.substring(0, tE.indexOf(" ")).replace(" ", "");
                String eventEnd = tE.substring(tE.lastIndexOf(" ")).replace(" ", "");

                Log.d(TAG, eventStart + " - " + eventEnd);

                for (Time tT : freeTime) {
                    String startTime = tT.getStartTime();
                    String endTime = tT.getEndTime();
                    if (startTime.contains(eventStart)) {
                        isWritable = true;
                    }
                    if (isWritable) {
                        deletedTimes.add(startTime + " - " + endTime);
                    }
                    if (endTime.contains(eventEnd)) {
                        isWritable = false;
                    }
                }

                ArrayList<Time> availableTime = new ArrayList<>();

                for (Time t : freeTime) {
                    availableTime.add(t);
                }

                Log.d(TAG, "DELETE");
                Log.d("deletedTimes", String.valueOf(deletedTimes.size()));
                for (int i = 0; i < deletedTimes.size(); i++) {
                    Log.d(TAG, deletedTimes.get(i));
                    for (Time t : availableTime) {
                        if (t.getTime().contains(deletedTimes.get(i))) {
                            Log.d(TAG, t.getTime());
                            freeTime.remove(t);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            adapter.setFreeTime(freeTime);
            adapter.notifyDataSetChanged();
        }
    }

}