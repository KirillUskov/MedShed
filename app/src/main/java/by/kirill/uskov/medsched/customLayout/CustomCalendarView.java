package by.kirill.uskov.medsched.customLayout;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.adapters.CalendarEventAdapter;
import by.kirill.uskov.medsched.adapters.CalendarGridAdapter;
import by.kirill.uskov.medsched.dialogs.AddAppointmentDialog;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CalendarEvent;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.DateUtil;

public class CustomCalendarView extends LinearLayout {
    private static final String TAG = "CustomCalendarView";
    private static final int MAX_DAYS = 42;

    private DBUtils dbUtil;

    private DatabaseReference databaseReference;

    private ImageView nextButton, previousButton;
    private TextView currentDate;
    private GridView calendar;

    private Date globalCurrentDateText = new Date();
    Locale locale = new Locale("ru");
    private Calendar c = Calendar.getInstance(locale);

    private ArrayList<Date> dates = new ArrayList<>();
    private ArrayList<Event> appointments = new ArrayList<>();
    private String date;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat eventDateFormat = new SimpleDateFormat("MM.yyyy");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM yyyy");
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    private Context context;

    private CalendarGridAdapter gridAdapter;

    private Handler mHandler = new Handler();

    private FragmentManager fragmentManager;


    public CustomCalendarView(Context context) {
        super(context);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initialLayout();
        dbUtil = new DBUtils();
        setUpCalendar();

        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c.add(Calendar.MONTH, -1);
                setUpCalendar();
                date = eventDateFormat.format(globalCurrentDateText);
                setAppointmentsToMonth();
                gridAdapter.notifyDataSetChanged();
            }
        });

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c.add(Calendar.MONTH, 1);
                setUpCalendar();
                date = eventDateFormat.format(globalCurrentDateText);
                setAppointmentsToMonth();
                gridAdapter.setEvents(appointments);
            }
        });

        calendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                date = formatter.format(dates.get(position));
                Application.getInstance().setSelectedDate(date);
                date = eventDateFormat.format(globalCurrentDateText);
                setAppointmentsToMonth();
            }
        });

        /*calendar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                date = formatter.format(dates.get(position));
                Application.getInstance().setSelectedDate(date);
                by.kirill.uskov.medsched.Application.getInstance(context).setFragmentManager(fragmentManager);
                AddAppointmentDialog dialog = new AddAppointmentDialog(context);
                dialog.show(fragmentManager, "Добавить запись");
                return false;
            }
        });*/
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initialLayout() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.control_calendar, this);
        nextButton = view.findViewById(R.id.calendar_next_button);
        previousButton = view.findViewById(R.id.calendar_prev_button);


        currentDate = view.findViewById(R.id.calendar_date_display);
        calendar = view.findViewById(R.id.calendar_grid);

        // If date has an events, so it will have bg - day_bg

    }

    private void setUpCalendar() {
        String currentDateText = yearFormat.format(c.getTime());
        int month = Integer.parseInt(monthFormat.format(c.getTime())) - 1;
        String monthText = DateUtil.mon[month];
        currentDate.setText(monthText + " " + currentDateText);

        globalCurrentDateText = c.getTime();

        dates.clear();
        Calendar monthCalendar = (Calendar) c.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth);

        boolean b = true;
        while (dates.size() < MAX_DAYS) {

            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
            if (b) {
                dates.remove(0);
                b = false;
            }
        }

        setAppointmentsToMonth();

        gridAdapter = new CalendarGridAdapter(context, dates, c, appointments);
        calendar.setAdapter(gridAdapter);

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

                        date = eventDateFormat.format(globalCurrentDateText);
                        String eventDate = event.getDate().replaceAll(" ", "");
                        if (date != null) {
                            if (eventDate.contains(date)) {
                                appointments.add(event);
                            }
                        }
                    }
                    Collections.sort(appointments);
                    gridAdapter.setEvents(appointments);
                    gridAdapter.notifyDataSetChanged();
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
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
    }

    public void kill() {
    }
}
