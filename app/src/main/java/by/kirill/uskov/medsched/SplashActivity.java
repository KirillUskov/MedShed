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

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import by.kirill.uskov.medsched.entities.events.Event;
import by.kirill.uskov.medsched.enums.AppointmentStatus;
import by.kirill.uskov.medsched.models.Application;
import by.kirill.uskov.medsched.models.CurrentUserModel;
import by.kirill.uskov.medsched.models.IntermediateEvent;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.FileUtil;
import by.kirill.uskov.medsched.utils.ThemeUtil;

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
        String path = by.kirill.uskov.medsched.Application.PREFS_PATH;

        int mode = 0;
        try {
            mode = Integer.parseInt(new FileUtil(path).read(openFileInput(path)));
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        ThemeUtil.getInstance().setSTheme(mode);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_splash);
        exitFromSplash = false;
        if (openNextActivities()) {
        } else {
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

    @Override
    protected void onStart() {
        super.onStart();

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
                startActivity(new Intent(getApplicationContext(), TodayActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
            return true;
        }
        return false;
    }

    private boolean isUserLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
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