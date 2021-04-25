package by.kirill.uskov.medsched.models;

import java.util.ArrayList;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;

public class Application {

    private String selectedDate;

    private ArrayList<Event> todayAppointments;

    private Patient patient;

    private static Application application;

    private Application() {
        todayAppointments = new ArrayList<>();
    }

    public static Application getInstance() {
        if(application == null) {
            application = new Application();
        }
        return application;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void setTodayAppointments(ArrayList<Event> appointments) {
        todayAppointments = appointments;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public ArrayList<Event> getTodayAppointments() {
        return todayAppointments;
    }

    public Patient getPatient() {
        return patient;
    }
}

