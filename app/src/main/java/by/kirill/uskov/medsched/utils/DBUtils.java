package by.kirill.uskov.medsched.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.Procedure;

public class DBUtils {
    private CurrentUserModel user;
    private DatabaseReference databaseReference;
    private String userPatCode, userSchedCode, userProceduresCode;
    private Context context;

    public DBUtils(Context context) {
        setUser();
        this.context = context;
    }

    public DBUtils() {
        setUser();
    }

    private void setUser() {
        user = CurrentUserModel.getInstance();
        String userName = user.getCodeForFirebase().replace(".", "");
        userName = userName.replace("#", "");
        userName = userName.replace("$", "");
        userPatCode = userName + "@Pat";
        userSchedCode = userName + "@Sched";
        userProceduresCode = userName + "@Proc";
    }

    public CurrentUserModel getUser() {
        return user;
    }

    public String getUserSchedCode() {
        return userSchedCode;
    }

    public String getUserPatCode() {
        return userPatCode;
    }

    public String getuserProceduresCode() {
        return userProceduresCode;
    }

    public DBPatient getP() {
        return new DBPatient();
    }

    public DBEvents getE() {
        return new DBEvents();
    }

    public DBProcedures getProc() {
        return new DBProcedures();
    }

    /* NEW DB class*/
    public class DBPatient {
        public boolean isException;
        private ArrayList<Patient> patients;

        public DBPatient() {
            patients = new ArrayList<>();
            databaseReference = FirebaseDatabase.getInstance().getReference(userPatCode);
            setUser();
        }

        public boolean set() {
            databaseReference = FirebaseDatabase.getInstance().getReference(userPatCode);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (patients.size() > 0) {
                        patients.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Patient patient = ds.getValue(Patient.class);
                        Log.i("DataSnapshot ", patient.getName());
                        patient.setId(ds.getKey());
                        patients.add(patient);
                    }
                    Collections.sort(patients);
                    Log.i("DBPatients", String.valueOf(patients.size()));
                    isException = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            /*databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.i("TASK", String.valueOf(task.getResult().exists()));
                        DataSnapshot dataSnapshot = task.getResult();
                        if (patients.size() > 0) {
                            patients.clear();
                        }
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Patient patient = ds.getValue(Patient.class);
                            Log.i("DataSnapshot ", patient.getName());
                            patient.setId(ds.getKey());
                            patients.add(patient);
                        }
                        Collections.sort(patients);
                        Log.i("DBPatients", String.valueOf(patients.size()));
                        isException = false;
                    }

                }
            });*/
            Log.i("DBUtils patients.size()", String.valueOf(patients.size()));
            return !isException;
        }

        public boolean remove(Patient patient) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userPatCode);

            Query applesQuery = databaseReference.orderByKey().equalTo(patient.getId());

            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                    }
                    isException = false;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("onCancelled", databaseError.toException().toString());
                    isException = true;
                }
            });
            return !isException;
        }

        public void update(Patient patient) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userPatCode);

            HashMap hashMap = new HashMap();
            hashMap.put("bdDate", patient.getBdDate());
            hashMap.put("comment", patient.getComment());
            hashMap.put("name", patient.getName());
            hashMap.put("phone", patient.getPhone());

            Log.i("ID", patient.getId());
            databaseReference.child(patient.getId()).updateChildren(hashMap);
        }

        public void undo(Patient patient) {
            databaseReference = FirebaseDatabase.getInstance().getReference(userPatCode);
            databaseReference.push().setValue(patient);
        }

        public void add(Patient patient) {
            databaseReference = FirebaseDatabase.getInstance().getReference(userPatCode);
            databaseReference.push().setValue(patient);
        }

        public ArrayList<Patient> getList() {
            set();
            Collections.sort(patients);
            return patients;
        }

    }

    /* NEW DB class*/
    public class DBEvents {

        private ArrayList<Event> appointments;
        public boolean isException;

        public DBEvents() {
            appointments = new ArrayList<>();
            databaseReference = FirebaseDatabase.getInstance().getReference(userSchedCode);
        }

        public boolean set() {
            databaseReference = FirebaseDatabase.getInstance().getReference(userSchedCode);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (appointments.size() > 0) {
                        appointments.clear();
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Event event = ds.getValue(Event.class);
                        event.setId(ds.getKey());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        Date date = new Date(System.currentTimeMillis());

                        if (event.getDate().replaceAll(" ", "").contains(formatter.format(date))) {
                            // If today appointment has "MOVE" status (it was moved on today), we set status "NO"
                            if (event.getStatus() == AppointmentStatus.MOVE) {
                                event.setStatus(AppointmentStatus.NO.toString());
                                update(event);
                            }
                            appointments.add(event);
                        }
                    }
                    Collections.sort(appointments);
                    isException = false;
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("onCancelled", error.getMessage());
                    isException = true;
                }
            });

            return !isException;
        }

        public boolean setAll() {
            databaseReference = FirebaseDatabase.getInstance().getReference(userSchedCode);
            databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (appointments.size() > 0) {
                            appointments.clear();
                        }
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Event event = ds.getValue(Event.class);
                            event.setId(ds.getKey());

                            appointments.add(event);
                        }
                        Collections.sort(appointments);
                        isException = false;
                    }
                }
            });

            return !isException;
        }

        public boolean remove(Event event) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userSchedCode);

            Query applesQuery = databaseReference.orderByKey().equalTo(event.getId());

            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                        isException = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("onCancelled", databaseError.toException().toString());
                    isException = true;
                }
            });
            Collections.sort(appointments);
            return !isException;
        }

        public boolean removeEventsList(ArrayList<Event> eventList) {
            Log.i("267 dbEvents REMOVE", "eventList size = " + String.valueOf(eventList.size()));
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userSchedCode);
            for (Event event : eventList) {
                Query applesQuery = databaseReference.orderByKey().equalTo(event.getId());
                Log.i("270 dbEvents REMOVE", "event ID = " + String.valueOf(event.getId()));
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            isException = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("onCancelled", databaseError.toException().toString());
                        isException = true;
                    }
                });
                Collections.sort(appointments);

            }
            return !isException;
        }

        public void undo(Event event) {
            databaseReference = FirebaseDatabase.getInstance().getReference(userSchedCode);
            databaseReference.push().setValue(event);

            Collections.sort(appointments);
        }

        public void update(Event event) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userSchedCode);

            HashMap hashMap = new HashMap();
            hashMap.put("date", event.getDate());
            hashMap.put("patient", event.getPatient());
            hashMap.put("procedure", event.getProcedure());
            hashMap.put("status", event.getStatus());
            hashMap.put("startTime", event.getStartTime());
            hashMap.put("endTime", event.getEndTime());

            Log.i("Id", event.getId());
            databaseReference.child(event.getId()).updateChildren(hashMap);
        }

        public void add(Event event) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userSchedCode);
            databaseReference.push().setValue(event);
        }

        public ArrayList<Event> getList() {
            set();
            Collections.sort(appointments);
            return appointments;
        }

        public ArrayList<Event> getPatientEvents(String patientName) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(userSchedCode);
            ArrayList<Event> events = new ArrayList<>();
            for (Event event : appointments) {
                if (event.getPatient().equals(patientName)) {
                    events.add(event);
                }
            }
            Log.i("DBEvents", "getPatientEvents.size = " + events.size());
            return events;
        }

        public boolean isTodayAppointmentsIncludeAppointmentForPatient(ArrayList<Event> list, String patientName, String date) {
            Log.i("dbEvents", "appointments size: " + appointments.size());
            for (Event appointment : list) {
                if (appointment.getPatient().equals(patientName) && appointment.getDate().equals(date)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isAppointmentsToDateIncludeAppointmentForPatient(String appointmentDate, String patientName) {
            for (Event event : appointments) {
                if (event.getDate().equals(appointmentDate) && event.getPatient().equals(patientName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* PROCEDURES */
    public class DBProcedures {

        private ArrayList<Procedure> procedures = new ArrayList<>();
        public boolean isException = false;

        public boolean setAll() {
            try {
                databaseReference = FirebaseDatabase.getInstance().getReference(userProceduresCode);
                databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            if (procedures.size() > 0) {
                                procedures.clear();
                            }
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Procedure procedure = ds.getValue(Procedure.class);
                                procedure.setId(ds.getKey());
                                procedures.add(procedure);
                            }
                            Collections.sort(procedures);
                            isException = false;
                        }
                    }
                });

                return !isException;
            } catch (RuntimeException e) {
                Log.i("DBProcedures", e.getMessage());
                return false;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public ArrayList<String> getProcedures() {
            ArrayList<String> procNames = new ArrayList<>();
            if (procedures.size() > 0) {
                procedures.forEach(procedure -> procNames.add(procedure.name));
            }
            return procNames;
        }

        public void add(String procedureName) {
            databaseReference = FirebaseDatabase.getInstance().getReference(userProceduresCode);

            Procedure procedure = new Procedure(procedureName);
            databaseReference.push().setValue(procedure);
        }

        public void update(String oldProcedureName, String newProcedureName) {
            for (Procedure p:procedures) {
                if(p.name.equals(oldProcedureName)) {
                    p.setName(newProcedureName);
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(userProceduresCode);

                    HashMap hashMap = new HashMap();
                    hashMap.put("name", p.getName());

                    Log.i("Id", p.getId());
                    databaseReference.child(p.getId()).updateChildren(hashMap);
                }

            }
        }

        public void remove(String procedureName) {
            setAll();
            for (Procedure p:procedures) {
                if(p.getName().equals(procedureName)) {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(userProceduresCode);

                    Query applesQuery = databaseReference.orderByKey().equalTo(p.getId());

                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("onCancelled", databaseError.toException().toString());
                        }
                    });
                }

            }
        }

    }


    public ArrayList<Event> getPatientAppointments(String nameOfPatient) {
        ArrayList<Event> appointments = new ArrayList<>();
        ArrayList<Event> allAppointments = getE().getList();

        for (Event appointment : allAppointments) {
            if (appointment.getPatient().contains(nameOfPatient) && appointment.getStatus() == AppointmentStatus.DO) {
                appointments.add(appointment);
            }
        }
        return appointments;
    }

}
