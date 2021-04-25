package by.kirill.uskov.medsched.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.database.ChildEventListener;
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

public class DBUtils {
    private CurrentUserModel user;
    private DatabaseReference databaseReference;
    private String userPatCode, userSchedCode, userProcedures;
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
        userPatCode = user.getCodeForFirebase() + "@Pat";
        userSchedCode = user.getCodeForFirebase() + "@Sched";
        userProcedures = user.getCodeForFirebase() + "@Proc";
    }

    public CurrentUserModel getUser() {
        return user;
    }

    public String getUserSchedCode() {
        return userSchedCode;
    }

    public DBPatient getP() {
        return new DBPatient();
    }

    public DBEvents getE() {
        return new DBEvents();
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

            ValueEventListener vListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (patients.size() > 0) {
                        patients.clear();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Patient patient = ds.getValue(Patient.class);
                        patient.setId(ds.getKey());
                        assert patient != null;
                        patients.add(patient);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    isException = true;
                }
            };
            databaseReference.addValueEventListener(vListener);
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
    }


    public ArrayList<Event> getPatientAppointments(String nameOfPatient) {
        ArrayList<Event> appointments = new ArrayList<>();
        ArrayList<Event> allAppointments = getE().getList();

        for (Event appointment: allAppointments) {
            if(appointment.getPatient().contains(nameOfPatient) && appointment.getStatus() == AppointmentStatus.DO) {
                appointments.add(appointment);
            }
        }
        return appointments;
    }

}
