package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Procedure;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.FileUtil;
import by.kirill.uskov.medsched.utils.ThemeUtil;

import static androidx.core.content.ContentProviderCompat.requireContext;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    private DBUtils dbUtil;
    private DBUtils.DBEvents dbEvents;
    private DBUtils.DBProcedures dbProcedures;

    private ArrayList<String> procedureList = new ArrayList<>();
    private ArrayList<Event> appointments = new ArrayList<>();
    private HashMap<String, Integer> doneAppointmentsList = new HashMap<>();
    private HashMap<String, Integer> notDoneAppointmentsList = new HashMap<>();

    private int currentDay, currentMonth, currentYear;

    private DatabaseReference databaseReference;

    private FirebaseUser user;
    private CircularImageView avatar;
    private TextView username;
    private TextView email;
    private TextView proceduresTextView;

    private LineChart appointmentsChart;

    private LinearLayout procedures;

    private Switch darkSwitch;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (procedureList.size() > 0) {
                if (procedureList.size() < 3) {
                    proceduresTextView.setText(procedureList.get(0));
                } else {
                    proceduresTextView.setText(procedureList.get(0) + ", " + procedureList.get(1) + ", " + procedureList.get(2));
                }
            } else {
                proceduresTextView.setText("");
            }
            proceduresTextView.postDelayed(this::run, 10);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        dbUtil = new DBUtils();
        dbEvents = dbUtil.getE();
        dbEvents.setAll();
        setList();

        setContentView(R.layout.activity_settings);

        darkSwitch = findViewById(R.id.dark_theme_swithcer);
        avatar = findViewById(R.id.user_avatar);
        username = findViewById(R.id.username_textView);
        email = findViewById(R.id.user_email_textView);
        procedures = findViewById(R.id.procedures_layout);
        proceduresTextView = findViewById(R.id.procedure_list_text_view);

        appointmentsChart = findViewById(R.id.appointments_stat_chart);

        initChart();
        setDate();
        setAppointments();

        proceduresTextView.postDelayed(runnable, 10);

        String path = by.kirill.uskov.medsched.Application.PREFS_PATH;

        int mode = 0;
        try {
            mode = Integer.parseInt(new FileUtil(path).read(openFileInput(path)));
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        boolean isShouldBeDark = mode == 1;
        boolean isSwitcherChecked = darkSwitch.isChecked();
        boolean res = isSwitcherChecked == isShouldBeDark;
        if (!res) {
            boolean isDark = ThemeUtil.getInstance().isDarkTheme();
            darkSwitch.setChecked(isDark);
        }

        darkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ThemeUtil.getInstance().setSTheme(isChecked ? 1 : 0);
                ThemeUtil.getInstance().changeToTheme(SettingsActivity.this);
                String path = Application.PREFS_PATH;
                try {
                    new FileUtil(path).write(openFileOutput(path, MODE_PRIVATE), isChecked ? "1" : "0");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        procedures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewProcedureActivity.class));
                finish();
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        String nameText = user.getDisplayName();
        String emailText = user.getEmail();
        Uri uri = user.getPhotoUrl();

        avatar.setBorderWidth(6l);
        Picasso.get().load(uri).placeholder(R.drawable.bg_round_layout).into(avatar);
        //avatar.setImageURI(uri);
        username.setText(nameText);
        email.setText(emailText);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        YearMonth yearMonthObject = YearMonth.of(currentYear, currentMonth);
        int daysInMonth = yearMonthObject.lengthOfMonth();
        Log.i(TAG, "days in month: " + daysInMonth);
    }


    private void setChart() {
        ArrayList<Entry> yDoneValues = new ArrayList<>();
        ArrayList<Entry> yNotDoneValues = new ArrayList<>();

        ArrayList<Date> eventsDatesList = new ArrayList<>();
        String[] dates = new String[currentDay];

        Log.i(TAG, "appointments.size = " + appointments.size());

        try {
            Log.i(TAG, "current day = " + currentDay);

            for (int i = 1; i <= currentDay; i++) {
                String eventDate = i < 10 ? "0" + i : String.valueOf(i);
                String month = currentMonth < 10 ? "0" + currentMonth : String.valueOf(currentMonth);
                eventDate = eventDate + "." + month + "." + currentYear;

                Log.i(TAG, "date = " + eventDate);
                eventsDatesList.add(formatter.parse(eventDate));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        Collections.sort(eventsDatesList);

        for (Date date : eventsDatesList) {
            String dateText = formatter.format(date);
            if (doneAppointmentsList.containsKey(dateText)) {
                yDoneValues.add(new Entry(yDoneValues.size(), (float) doneAppointmentsList.get(dateText)));
            } else {
                yDoneValues.add(new Entry(yDoneValues.size(), 0));
            }
        }

        for (Date date : eventsDatesList) {
            String dateText = formatter.format(date);
            if (notDoneAppointmentsList.containsKey(dateText)) {
                yNotDoneValues.add(new Entry(yNotDoneValues.size(), (float) notDoneAppointmentsList.get(dateText)));
            } else {
                yNotDoneValues.add(new Entry(yNotDoneValues.size(), 0));
            }
        }

        dates = new String[eventsDatesList.size()];
        for (int i = 0; i < eventsDatesList.size(); i++) {
            String date = formatter.format(eventsDatesList.get(i));
            String dateDay = String.valueOf(date).substring(0, date.toString().indexOf("."));
            dates[i] = dateDay;
        }

        XAxis bottomAxis = appointmentsChart.getXAxis();
        bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        String[] finalDates = dates;
        bottomAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Log.i(TAG, "VALUE: " + value);
                return finalDates[(int) value];
            }
        });

        LineDataSet doneSet = new LineDataSet(yDoneValues, "Заверешены");
        doneSet.setColor(getResources().getColor(R.color.green_500));
        doneSet.setDrawCircles(false);
        doneSet.setDrawValues(false);
        doneSet.setLineWidth(1.5f);

        LineDataSet notDoneSet = new LineDataSet(yNotDoneValues, "Не завершены");
        notDoneSet.setColor(ThemeUtil.getInstance().isDarkTheme() ?
                                    getResources().getColor(R.color.red_200) :
                                        getResources().getColor(R.color.red_500));
        notDoneSet.setDrawCircles(false);
        notDoneSet.setDrawValues(false);
        notDoneSet.setLineWidth(1.5f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(doneSet);
        dataSets.add(notDoneSet);

        LineData lineData = new LineData(dataSets);
        appointmentsChart.setData(lineData);

        appointmentsChart.getXAxis().setTextColor(ThemeUtil.getInstance().isDarkTheme() ?
                                        getResources().getColor(R.color.grey_200) :
                                            getResources().getColor(R.color.dark_grey));
        appointmentsChart.getAxisLeft().setTextColor(ThemeUtil.getInstance().isDarkTheme() ?
                                        getResources().getColor(R.color.grey_200) :
                                            getResources().getColor(R.color.dark_grey));

        appointmentsChart.getLegend().setTextColor(ThemeUtil.getInstance().isDarkTheme() ?
                                        getResources().getColor(R.color.grey_500) :
                                            getResources().getColor(R.color.grey_700));


    }

    private void initChart() {
        Description desc = new Description();
        desc.setText("");
        appointmentsChart.setDescription(desc);
        appointmentsChart.setDragEnabled(false);
        appointmentsChart.setScaleEnabled(false);

        appointmentsChart.getAxisRight().setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setEventsLists() {
        if (notDoneAppointmentsList.size() > 0) {
            notDoneAppointmentsList.clear();
        }
        if (doneAppointmentsList.size() > 0) {
            doneAppointmentsList.clear();
        }
        for (Event e : appointments) {
            String month = currentMonth + "." + currentYear;
            if (e.getDate().contains(month)) {
                if (e.getStatus() != AppointmentStatus.DO) {
                    if (notDoneAppointmentsList.containsKey(e.getDate())) {
                        int oldNumber = notDoneAppointmentsList.get(e.getDate());
                        int newNumber = oldNumber + 1;
                        notDoneAppointmentsList.remove(e.getDate());
                        notDoneAppointmentsList.put(e.getDate(), newNumber);
                    } else {
                        notDoneAppointmentsList.put(e.getDate(), 1);
                    }
                } else {
                    if (doneAppointmentsList.containsKey(e.getDate())) {
                        int oldNumber = doneAppointmentsList.get(e.getDate());
                        int newNumber = oldNumber + 1;
                        doneAppointmentsList.replace(e.getDate(), oldNumber, newNumber);
                    } else {
                        doneAppointmentsList.put(e.getDate(), 1);
                    }
                }
            }
        }
        Log.i(TAG, "done: " + doneAppointmentsList.size());
        Log.i(TAG, "not done: " + notDoneAppointmentsList.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        proceduresTextView.removeCallbacks(runnable);
    }

    public void backToTodayAppointments(View view) {
        startActivity(new Intent(this, TodayActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    public void logout(View view) {
        GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SplashActivity.class));
        finish();

    }

    public void setList() {
        try {
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
                    } else {
                    }
                }
            });
        } catch (RuntimeException e) {
            Log.e("ViewProcedureActivity", e.getMessage());
        }
    }

    private void setAppointments() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getUserSchedCode());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (appointments.size() > 0) {
                        appointments.clear();
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Event event = ds.getValue(Event.class);

                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        Date date = new Date(System.currentTimeMillis());
                        Date dateOfEvent = null;
                        try {
                            dateOfEvent = formatter.parse(event.getDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (event.getDate().contains(currentMonth + "." + currentYear) && (dateOfEvent.before(date) || event.getDate().equals(formatter.format(date)))) {
                            event.setId(ds.getKey());
                            Log.i(TAG, event.getDate());
                            appointments.add(event);
                        }
                    }
                    Collections.sort(appointments);
                    setEventsLists();
                    setChart();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }

            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}