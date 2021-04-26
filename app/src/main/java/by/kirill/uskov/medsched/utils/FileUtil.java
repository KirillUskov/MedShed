package by.kirill.uskov.medsched.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private String filePath;

    public FileUtil(String filePath) {
        this.filePath = filePath;
    }

    public String read(FileInputStream fis) {
        try {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text = "";
            while ((text = br.readLine()) != null) {
                sb.append(text);
;            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void write(FileOutputStream fos,String text) {
        try {
            fos.write(text.getBytes());
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
