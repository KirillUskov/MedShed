package by.kirill.uskov.medsched.models;

import java.util.ArrayList;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;

public class Application {

    private String selectedDate;

    private ArrayList<Event> todayAppointments;

    private ArrayList<Event> allAppointments;

    private Patient patient;
    private Event event;

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

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setAllAppointments(ArrayList<Event> events) {
        allAppointments = events;
    }

    public ArrayList<Event> getAllAppointments() {
        return allAppointments;
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

    public Event getEvent() {
        return event;
    }

    public boolean isTodayAppointmentsIncludeAppointmentForPatient(String patientName) {
        for (Event appointment: todayAppointments) {
            if (appointment.getPatient().equals(patientName)) {
                return true;
            }
        }
        return false;
    }
}

