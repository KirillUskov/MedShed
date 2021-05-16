package by.kirill.uskov.medsched.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.models.Application;
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



    @SuppressLint("ResourceType")
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

        // set selected date data
        String selectedDate = Application.getInstance().getSelectedDate();
        if (selectedDate == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Date dateD = new Date(System.currentTimeMillis());
            selectedDate = formatter.format(dateD);
        }
        String selectedDateDay = selectedDate.substring(0,2);
        String selectedDateMonth = selectedDate.substring(3, 4);

        if (displayMonth != currentMonth || displayYear != currentYear) {
            if(!ThemeUtil.getInstance().isDarkTheme()) {
                dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
            }
            dayNumber.setText(String.valueOf(dayNo));
            eventNum.setVisibility(View.INVISIBLE);
            if(ThemeUtil.getInstance().isDarkTheme()) {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.dark_grey));
            } else {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey_1));
            }
        } else {
            dayNumber.setText(String.valueOf(dayNo));
            if (events.size() == 0) {
                eventNum.setVisibility(View.INVISIBLE);
                if (selectedDateDay.equals(String.valueOf(dayNo)) || selectedDateDay.equals(String.valueOf("0" + dayNo))) {
                    view.setBackgroundColor(getContext().getResources().getColor(R.color.selected_day));
                    dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                    eventNum.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                } else {
                    if(ThemeUtil.getInstance().isDarkTheme()) {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.dark_grey_2));
                        dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                    } else {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey_4));
                        dayNumber.setTextColor(getContext().getResources().getColor(R.color.grey_700));
                    }
                }
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
                eventNum.setVisibility(View.VISIBLE);
                eventNum.setText(String.valueOf(eventNumber));
                //
                if (eventNumber == 0) {
                    eventNum.setVisibility(View.INVISIBLE);
                }
                if (selectedDateDay.equals(String.valueOf(dayNo)) || selectedDateDay.equals(String.valueOf("0" + dayNo))) {
                    view.setBackgroundColor(getContext().getResources().getColor(R.color.selected_day));
                    dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                    eventNum.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                } else {
                    if(ThemeUtil.getInstance().isDarkTheme()) {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.dark_grey_2));
                        dayNumber.setTextColor(getContext().getResources().getColor(R.color.light_grey));
                        eventNum.setTextColor(getContext().getResources().getColor(R.color.grey_200));
                    } else {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey_4));
                        dayNumber.setTextColor(getContext().getResources().getColor(R.color.grey_700));
                        eventNum.setTextColor(getContext().getResources().getColor(R.color.grey_500));
                    }
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
