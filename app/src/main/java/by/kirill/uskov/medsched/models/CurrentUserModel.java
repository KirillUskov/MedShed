package by.kirill.uskov.medsched.models;

import android.net.Uri;
import android.util.Log;

public class CurrentUserModel {

    private static CurrentUserModel currentUserModel;

    private String username, userEmail, photoUri;
    private String codeForFirebase;

    private CurrentUserModel() {
    }

    public static CurrentUserModel getInstance() {
        if (currentUserModel == null) {
            currentUserModel = new CurrentUserModel();
        }
        return currentUserModel;
    }

    public CurrentUserModel setUsername(String username) {
        this.username = username;
        return this;
    }

    public CurrentUserModel setPhotoUri(Uri uri) {
        photoUri = uri.toString();
        return this;
    }

    public CurrentUserModel setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        setCodeForFirebase();
        return this;
    }

    private void setCodeForFirebase() {
        codeForFirebase = userEmail.substring(0, userEmail.indexOf("@"));
        Log.i("CurrentUserModel", codeForFirebase);
    }

    public String getCodeForFirebase() {
        return codeForFirebase;
    }

    public String getPhotoUri() {
        return photoUri;
    }

}
