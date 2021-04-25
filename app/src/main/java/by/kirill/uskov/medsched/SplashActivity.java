package by.kirill.uskov.medsched;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.utils.DBUtils;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private FirebaseUser user;

    private DBUtils dbUtil;
    private DBUtils.DBEvents dbEvents;

    private DatabaseReference databaseReference;

    private ArrayList<Event> appointments;

    private ConnectivityManager connectivityManager;
    private BroadcastReceiver broadcastReceiver = new ConnectivityReceiver();

    private boolean exitFromSplash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        exitFromSplash = false;
        if (openNextActivities()) {
        } else {
            try {
                Thread.sleep(500);
            } catch (Exception e) {

            }
            new AlertDialog.Builder(this)
                    .setTitle("Отсутствует подключение к сети")
                    .setMessage("Проверьте наличие подключения к сети!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setExitSplash(true);
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void setExitSplash(boolean state) {
        exitFromSplash = state;
        Log.d(TAG, String.valueOf(exitFromSplash));
        if(exitFromSplash) {
            finish();
        }
    }

    private boolean openNextActivities() {
        if(isNetworkConnected()) {
            IntermediateEvent.getInstance().setIntermediateEventNull();

            if (isUserLoggedIn()) {
                CurrentUserModel userModel = CurrentUserModel.getInstance()
                        .setUsername(user.getDisplayName())
                        .setUserEmail(user.getEmail())
                        .setPhotoUri(user.getPhotoUrl());
                appointments = new ArrayList<>();
                setAppointments();
                Application.getInstance().setTodayAppointments(appointments);
                startActivity(new Intent(getApplicationContext(), TodayActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    private boolean isUserLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    private void setAppointments() {
        databaseReference = FirebaseDatabase.getInstance().getReference(CurrentUserModel.getInstance().getCodeForFirebase() + "@Sched");

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
                        }
                        appointments.add(event);
                    }
                }
                Collections.sort(appointments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });
    }

    private boolean isNetworkConnected() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED);
    }

    private class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}