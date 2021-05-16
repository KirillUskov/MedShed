package by.kirill.uskov.medsched.entities.events;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import by.kirill.uskov.medsched.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

@IgnoreExtraProperties
public class Event implements Serializable, Comparable<Event> {
    @Getter
    @Setter
    protected String date, patient, procedure, status;
    protected String startTime, endTime;
    protected double duration;
    protected String id;
    private String phoneNumber;

    public Event() {
    }

    public Event(String patient, String date, String startTime, String endTime, String status) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.status = status;
        setDuration();
    }

    public Event(String patient, String procedure, String date, String startTime, String endTime, String status) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.procedure = procedure;
        this.status = status;
        setDuration();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPatient() {
        return patient;
    }

    public AppointmentStatus getStatus() {
        return AppointmentStatus.valueOf(status);
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

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPatient(String patient) {
        this.patient = patient;
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

    @Override
    public int compareTo(Event o) {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            Time time1 = new Time(formatter.parse(getStartTime()).getTime());
            Time time2 = new Time(formatter.parse(o.getStartTime()).getTime());
            AppointmentStatus status1 = getStatus();
            AppointmentStatus status2 = o.getStatus();
            if (status1.getWeigh() < status2.getWeigh()) {
                return -1;
            } else if (status2.getWeigh() < status1.getWeigh()) {
                return 1;
            } else if (time1.before(time2)) {
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
