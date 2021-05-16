package by.kirill.uskov.medsched.entities.patients;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

import lombok.Getter;

@IgnoreExtraProperties
public class Patient implements Serializable, Comparable<Patient> {
    @Getter public String name, bdDate, phone, comment;
    private String id;

    private Patient() {
    }

    public Patient(String name, String bdDate, String phone, String comment) {
        this.name = name;
        this.bdDate = bdDate;
        this.phone = phone;
        this.comment = comment;
    }

    public Patient(String name, String bdDate, String phone) {
        this.name = name;
        this.bdDate = bdDate;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getBdDate() {
        return bdDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(Patient o) {
        int length = o.getName().length() < getName().length() ? o.getName().length() : getName().length();
        for (int i = 0; i < length - 1; i++) {
            //Log.i("Names", o.getName() + " - " + getName());
            int char1 = (int) o.getName().charAt(i);
            int char2 = (int) getName().charAt(i);
            if (char1 > char2) {
                //Log.i("CHAR1", char1 + " - " + char2);
                return -1;
            } else if (char1 < char2)  {
                //Log.i("CHAR2", char1 + " - " + char2);
                return 1;
            } else {
                continue;
            }
        }
        return 1;
    }
}
