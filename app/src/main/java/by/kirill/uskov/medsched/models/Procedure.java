package by.kirill.uskov.medsched.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Procedure implements Serializable, Comparable<Procedure> {
    public String id;
    public String name;

    public Procedure(String name) {
        this.name = name;
    }

    public Procedure() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Procedure o) {
        int length = o.name.length() < name.length() ? o.name.length() : name.length();
        for (int i = 0; i < length - 1; i++) {
            int char1 = (int) o.name.charAt(i);
            int char2 = (int) name.charAt(i);
            if (char1 > char2) {
                return -1;
            } else if (char1 < char2)  {
                return 1;
            } else {
                continue;
            }
        }
        return 1;
    }
}
