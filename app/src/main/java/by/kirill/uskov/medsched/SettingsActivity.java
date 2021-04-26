package by.kirill.uskov.medsched;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;

import by.kirill.uskov.medsched.utils.FileUtil;
import by.kirill.uskov.medsched.utils.ThemeUtil;

import static androidx.core.content.ContentProviderCompat.requireContext;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private FirebaseUser user;
    private CircularImageView avatar;
    private TextView username;
    private TextView email;

    private Switch darkSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_settings);

        darkSwitch = findViewById(R.id.dark_theme_swithcer);
        avatar = findViewById(R.id.user_avatar);
        username = findViewById(R.id.username_textView);
        email = findViewById(R.id.user_email_textView);


        String path = by.kirill.uskov.medsched.Application.PREFS_PATH;

        int mode = 0;
        try {
            mode = Integer.parseInt(new FileUtil(path).read(openFileInput(path)));
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        boolean isShouldBeDark = mode == 1;
        boolean isSwitcherChecked = darkSwitch.isChecked();
        boolean res = isSwitcherChecked == isShouldBeDark;
        if (!res) {
            boolean isDark = ThemeUtil.getInstance().isDarkTheme();
            darkSwitch.setChecked(isDark);
        }

        darkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ThemeUtil.getInstance().setSTheme(isChecked ? 1 : 0);
                ThemeUtil.getInstance().changeToTheme(SettingsActivity.this);
                String path = Application.PREFS_PATH;
                try {
                    new FileUtil(path).write(openFileOutput(path, MODE_PRIVATE), isChecked ? "1" : "0");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        String nameText = user.getDisplayName();
        String emailText = user.getEmail();
        Uri uri = user.getPhotoUrl();

        avatar.setBorderWidth(6l);
        Picasso.get().load(uri).placeholder(R.drawable.man_head).into(avatar);
        //avatar.setImageURI(uri);
        username.setText(nameText);
        email.setText(emailText);
    }

    public void backToTodayAppointments(View view) {
        startActivity(new Intent(this, TodayActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    public void logout(View view) {
        GoogleSignIn.getClient(
                    this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                    .signOut();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SplashActivity.class));
        overridePendingTransition(0,0);
        finish();

    }
}