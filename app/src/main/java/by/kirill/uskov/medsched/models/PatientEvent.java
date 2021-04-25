package by.kirill.uskov.medsched.models;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

public class PatientEvent implements Comparable<PatientEvent> {

    @Getter
    @Setter
    protected String date, patient, procedure, status;
    protected String startTime, endTime;
    protected double duration;
    protected String id;

    public PatientEvent() {
    }

    public PatientEvent(Event event) {
        this.patient = event.getPatient();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.date = event.getDate();
        this.status = event.getStatus().toString();
        this.procedure = event.getProcedure();
        setDuration();
    }

    public PatientEvent(String patient, String date, String startTime, String endTime, String status) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.status = status;
        setDuration();
    }

    public PatientEvent(String patient, String procedure, String date, String startTime, String endTime, String status) {
        this.patient = patient;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.procedure = procedure;
        this.status = status;
        setDuration();
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDuration() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date start = format.parse(startTime);
            Date end = format.parse(endTime);
            duration = (end.getTime() - start.getTime()) / 3600000d; // in hours
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(PatientEvent o) {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = formatter.parse(getDate());
            date2 = formatter.parse(o.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date1.before(date2)) {
            return -1;
        } else if (date1.after(date2)) {
            return 1;
        } else {
            return 0;
        }
    }

}
