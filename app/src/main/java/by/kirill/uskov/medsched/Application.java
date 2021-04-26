package by.kirill.uskov.medsched;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

public class Application {
    public static final String PREFS_PATH = "prefs.txt";
    private static Application application;
    private Context context;
    private FragmentManager fragmentManager;

    private Application(Context context) {
        this.context = context;
    }

    public static Application getInstance(Context context) {
        if(application == null) {
            application = new Application(context);
        }
        return application;
    }

    public static Application getInstance() {
        return application;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public Context getContext() {
        return context;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
}
