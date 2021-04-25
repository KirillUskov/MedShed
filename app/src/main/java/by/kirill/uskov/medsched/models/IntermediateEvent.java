package by.kirill.uskov.medsched.models;

import java.util.ArrayList;

public class IntermediateEvent {
    private static IntermediateEvent intermediateEvent;
    private String date, procedure, name;
    private String startTime;
    private String endTime;
    private ArrayList<String> eventTime;

    private IntermediateEvent(String date, String name, String procedure) {
        this.date = date;
        this.name = name;
        this.procedure = procedure;
    }

    private IntermediateEvent() {
    }


    public static IntermediateEvent getInstance(String date, String name, String procedure) {
        if (intermediateEvent == null) {
            intermediateEvent = new IntermediateEvent(date, name, procedure);
        }
        return intermediateEvent;
    }

    public static IntermediateEvent getInstance() {
        if (intermediateEvent == null) {
            intermediateEvent = new IntermediateEvent();
        }
        return intermediateEvent;
    }

    public static IntermediateEvent getInstanceOrNull() {
        return intermediateEvent;
    }

    public IntermediateEvent setEventTime(ArrayList<String> list) {
        new ArrayList<>();
        eventTime = list;
        return this;
    }

    public ArrayList<String> getEventTime() {
        return eventTime;
    }

    public IntermediateEvent setDate(String date) {
        this.date = date;
        return this;
    }

    public IntermediateEvent setPatient(String patient) {
        name = patient;
        return this;
    }
    public IntermediateEvent setProcedure(String procedure) {
        this.procedure = procedure;
        return this;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getProcedure() {
        return procedure;
    }

    public IntermediateEvent setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public IntermediateEvent setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setIntermediateEventNull() {
        intermediateEvent = null;
    }
}
