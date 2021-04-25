package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import by.kirill.uskov.medsched.customListeners.RecyclerTouchListener;
import by.kirill.uskov.medsched.dialogs.AddPatientDialog;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.entities.patients.SwipeRecyclerViewAdapter;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.utils.DBUtils;

public class PatientsActivity extends AppCompatActivity {

    private DBUtils dbUtil;
    private DBUtils.DBPatient dbPatient;

    private BottomNavigationView bottomNavigationView;

    private DatabaseReference databaseReference;

    private RecyclerView patientsRV;

    private ArrayList<Patient> patients;
    private Patient editedPatient;

    private SwipeRecyclerViewAdapter swipeAdapter;

    private RecyclerTouchListener touchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        dbUtil = new DBUtils(getApplicationContext());

        dbPatient = dbUtil.getP();
        patients = dbPatient.getList();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        patientsRV = findViewById(R.id.patientsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        patientsRV.setHasFixedSize(false);


        swipeAdapter = new SwipeRecyclerViewAdapter(this, patients);
        patientsRV.setAdapter(swipeAdapter);

        getAllPatients();

        patientsRV.setLayoutManager(layoutManager);

        setButtons();
        setNavigation();
    }

    private void setButtons() {
        touchListener = new RecyclerTouchListener(this, patientsRV);
        touchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {

            }
        })
                .setSwipeOptionViews(R.id.delete_task, R.id.edit_task)
                .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                    @Override
                    public void onSwipeOptionClicked(int viewID, int position) {
                        switch (viewID) {
                            case R.id.delete_task:
                                editedPatient = patients.get(position);
                                removePatient(editedPatient);
                                getAllPatients();
                                swipeAdapter.setPatients(patients);
                                Snackbar.make(patientsRV, "Пациент " + editedPatient.getName() + " был удален!", BaseTransientBottomBar.LENGTH_LONG)
                                        .setAction("Отменить", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoPatientEditing(editedPatient);
                                                getAllPatients();
                                            }
                                        })
                                        .show();
                                break;
                            case R.id.edit_task:
                                Patient patient = patients.get(position);
                                Application.getInstance().setPatient(patient);
                                startActivity(new Intent(getApplicationContext(), ViewPatientDataActivity.class));
                                overridePendingTransition(0,0);
                                finish();
                                break;
                        }
                    }
                });
        patientsRV.addOnItemTouchListener(touchListener);
    }

    /*private void getAllPatients() {
        if(dbPatient.set()) {
            patients = dbPatient.getList();
            swipeAdapter.notifyDataSetChanged();
        }
    }*/

    private void getAllPatients() {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Pat");

        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (patients.size() > 0) {
                    patients.clear();
                }
                for(DataSnapshot ds: snapshot.getChildren()) {
                    Patient patient = ds.getValue(Patient.class);
                    assert patient != null;
                    patient.setId(ds.getKey());
                    patients.add(patient);
                    Collections.sort(patients);
                    swipeAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(vListener);
    }

    private void removePatient(Patient patient) {
        if(dbPatient.remove(patient)) {
            patients = dbPatient.getList();
            swipeAdapter.notifyDataSetChanged();
        }
    }

    private void undoPatientEditing(Patient patient) {
        dbPatient.undo(patient);
        patients = dbPatient.getList();
        swipeAdapter.notifyDataSetChanged();
    }

    private void updatePatient(Patient patient) {
        dbPatient.undo(patient);
        patients = dbPatient.getList();
        swipeAdapter.notifyDataSetChanged();
    }

    public void addPatientClick(View view) {
        AddPatientDialog addPatientDialog = new AddPatientDialog();
        addPatientDialog.show(getSupportFragmentManager(), "Добавить пациента");
    }

    private void setNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.patientsActivity);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.calendarSchedule:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.patientsActivity:
                        return true;
                    case R.id.currentDaySchedule:
                        startActivity(new Intent(getApplicationContext(), TodayActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }
}