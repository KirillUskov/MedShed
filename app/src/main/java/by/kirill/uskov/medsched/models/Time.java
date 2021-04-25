package by.kirill.uskov.medsched.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;

public class Time implements Comparable<Time> {
    private String time;
    private boolean isSelected;

    public Time() {}

    public Time(String time,boolean selected) {
        this.time = time;
        isSelected = selected;
    }


    public Time setSelected(boolean selected) {
        isSelected = selected;
        return this;
    }

    public Time setTime(String time) {
        this.time = time;
        return this;
    }

    public String getTime() {
        return time;
    }

    public String getStartTime() {
        return getTime().substring(0, getTime().indexOf(" ")).replace(" ", "");
    }

    public String getEndTime() {
        return getTime().substring(getTime().lastIndexOf(" ")).replace(" ", "");
    }


    public boolean getSelected() {
        return isSelected;
    }

    @Override
    public int compareTo(Time o) {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            String startTime1 = getTime().substring(getTime().indexOf(" ")).replace(" ", "");
            String startTime2 = o.getTime().substring(o.getTime().indexOf(" ")).replace(" ", "");
            java.sql.Time time1 = new java.sql.Time(formatter.parse(startTime1).getTime());
            java.sql.Time time2 = new java.sql.Time(formatter.parse(startTime2).getTime());
            if (time1.before(time2)) {
                return -1;
            } else if (time1.after(time2)) {
                return 1;
            } else {
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
