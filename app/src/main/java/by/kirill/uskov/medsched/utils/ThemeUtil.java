package by.kirill.uskov.medsched.utils;

import android.app.Activity;
import android.content.Intent;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.SettingsActivity;

public class ThemeUtil {
    private static ThemeUtil timeUtil;

    private static int sTheme = 0;

    public final static int THEME_MATERIAL_LIGHT = 0;
    public final static int THEME_MATERIAL_DARK = 1;

    private ThemeUtil() {

    }

    public static ThemeUtil getInstance() {
        if(timeUtil == null) {
            timeUtil = new ThemeUtil();
        }
        return timeUtil;
    }

    public ThemeUtil changeToTheme(Activity activity) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return this;
    }

    public ThemeUtil onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            case THEME_MATERIAL_LIGHT:
                activity.setTheme(R.style.Theme_MedSched);
                break;
            case THEME_MATERIAL_DARK:
                activity.setTheme(R.style.Theme_MedSched_Dark);
                break;
            default:
                activity.setTheme(R.style.Theme_MedSched);
                break;
        }
        return this;
    }

    public void setSTheme(int i) {
        sTheme = i;
    }

    public boolean isDarkTheme() {
        return sTheme == 1;
    }
}
