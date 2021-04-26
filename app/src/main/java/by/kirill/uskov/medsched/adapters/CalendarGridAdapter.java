package by.kirill.uskov.medsched.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class CalendarGridAdapter extends ArrayAdapter {

    private ArrayList<Date> dates;
    private Calendar currentDate;
    private ArrayList<Event> events;
    private LayoutInflater layoutInflater;


    public CalendarGridAdapter(@NonNull Context context, ArrayList<Date> dates, Calendar currentDate, ArrayList<Event> events) {
        super(context, R.layout.single_cell_layout);
        this.dates = dates;
        this.currentDate = currentDate;
        this.events = events;
        layoutInflater = LayoutInflater.from(context);
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);
        int dayNo = dateCalendar.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCalendar.get(Calendar.MONTH) + 1;
        int displayYear = dateCalendar.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);

        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.single_cell_layout, parent, false);
        }

        TextView dayNumber = view.findViewById(R.id.calendar_day);

        TextView eventNum = view.findViewById(R.id.events_id);
        if (displayMonth != currentMonth || displayYear != currentYear) {
            if(!ThemeUtil.getInstance().isDarkTheme()) {
                dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
            }
            dayNumber.setText(String.valueOf(dayNo));
            eventNum.setVisibility(View.INVISIBLE);
        } else {
            dayNumber.setText(String.valueOf(dayNo));
            if (events.size() == 0) {
                eventNum.setVisibility(View.INVISIBLE);
            } else {

                Calendar eventCalendar = Calendar.getInstance();
                ArrayList<String> dayEvents = new ArrayList<>();
                int eventNumber = 0;

                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    String eventDate = event.getDate().substring(0, event.getDate().indexOf(".")).replace(".", "");
                    int eventDayNumber = Integer.parseInt(eventDate);
                    if (dayNo == eventDayNumber) {
                        eventNumber++;
                    }
                }
                //
                if(ThemeUtil.getInstance().isDarkTheme()) {
                    dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                }
                eventNum.setVisibility(View.VISIBLE);
                eventNum.setText(String.valueOf(eventNumber));
                //
                if (eventNumber == 0) {
                    eventNum.setVisibility(View.INVISIBLE);
                }
            }
        }
        return view;
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public int getPosition(@Nullable Object date) {
        return dates.indexOf(date);
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }
}
