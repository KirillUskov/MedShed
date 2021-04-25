package by.kirill.uskov.medsched.models;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;

public class CalendarEvent implements Serializable, Comparable<CalendarEvent>  {
    private String date, patient, procedure;
    private String startTime, endTime;
    private double duration;
    private String id;

    public CalendarEvent() {
    }

    public CalendarEvent(String patient, String date, String startTime, String endTime) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        setDuration();
    }

    public CalendarEvent(String patient, String procedure, String date, String startTime, String endTime) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.procedure = procedure;
        setDuration();
    }


    public String getPatient() {
        return patient;
    }

    public String getDate() {
        return date;
    }

    public String getProcedure() {
        return procedure;
    }

    public String getId() {
        return id;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getDuration() {
        return duration;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setDuration() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date start = format.parse(startTime);
            Date end = format.parse(endTime);
            duration = ( end.getTime() - start.getTime() ) / 3600000d; // in hours
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // String patient, String procedure, String date, String startTime, String endTime
    public static CalendarEvent copyFrom(Event event) {
        return new CalendarEvent(event.getPatient(), event.getProcedure(), event.getDate(), event.getStartTime(), event.getEndTime());
    }

    @Override
    public int compareTo(CalendarEvent o) {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            java.sql.Time time1 = new java.sql.Time(formatter.parse(getStartTime()).getTime());
            java.sql.Time time2 = new Time(formatter.parse(o.getStartTime()).getTime());
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
