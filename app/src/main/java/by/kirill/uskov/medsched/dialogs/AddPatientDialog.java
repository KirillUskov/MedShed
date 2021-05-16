package by.kirill.uskov.medsched.dialogs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

import by.kirill.uskov.medsched.Application;
import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class AddPatientDialog extends AppCompatDialogFragment {
    private DBUtils dbUtil;
    private DBUtils.DBPatient dbPatient;

    private EditText patientName;
    private EditText patientPhone;
    private EditText patientComment;

    private TextView patientBDDate;
    private LinearLayout patientBDDateLinearLayout;
    private ImageView datePicker;

    private Button addButton;
    private Button cancelButton;

    private Patient patient;

    private DatePickerDialog datePickerDialog;


    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        dbUtil = new DBUtils(getContext());
        dbPatient = dbUtil.getP();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_patient, null);

        builder.setView(view);

        patientName = view.findViewById(R.id.patientName);
        patientBDDate = view.findViewById(R.id.patientBDDate);
        patientPhone = view.findViewById(R.id.patientPhone);
        patientComment = view.findViewById(R.id.patientComment);

        addButton = view.findViewById(R.id.addPatientButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        patientBDDateLinearLayout = view.findViewById(R.id.patient_bd_date_linear_layout);

        addButton.setBackgroundResource(R.drawable.bg_custom_green_button);

        datePicker = view.findViewById(R.id.calendar_picker);

        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("+7(___)___-__-__");
        FormatWatcher formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(slots)
        );
        formatWatcher.installOn(patientPhone);

        Slot[] date = new UnderscoreDigitSlotsParser().parseSlots("__.__.____");
        formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(date)
        );
        formatWatcher.installOn(patientBDDate);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPatientClick();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDatePickerDialog();
                datePickerDialog.show();
            }
        });

        return builder.create();
    }
    private void initDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String monthText = String.valueOf(month);
                if(monthText.length() == 1) {
                    monthText = "0" + monthText;
                }
                String dayText = String.valueOf(dayOfMonth);
                if(dayText.length() == 1) {
                    dayText = "0" + dayText;
                }
                String date = dayText + "." + monthText + "." + year;
                patientBDDate.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Locale.setDefault(new Locale("ru"));
        int style = ThemeUtil.getInstance().isDarkTheme() ? android.app.AlertDialog.THEME_HOLO_DARK : android.app.AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, year, month, day);
    }

    public void addPatientClick() {
        try {
            boolean isException = false;
            String name = patientName.getText().toString();
            String dbDate = patientBDDate.getText().toString();
            String phone = patientPhone.getText().toString();
            String comment = patientComment.getText().toString();
            if (comment.length() > 0) {
                patient = new Patient(name, dbDate, phone, comment);
            } else {
                patient = new Patient(name, dbDate, phone);
            }
            if (name.length() == 0) {
                isException = true;
                patientName.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientName.setBackgroundResource(R.drawable.bg_custom_white);
            }
            if (!dbDate.matches(".*\\d.*")) {
                isException = true;
                patientBDDateLinearLayout.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientBDDateLinearLayout.setBackgroundResource(R.drawable.bg_custom_white);
            }
            if (phone.length() == 0) {
                isException = true;
                patientPhone.setBackgroundResource(R.drawable.bg_custom_error_input_layout);
            } else {
                patientPhone.setBackgroundResource(R.drawable.bg_custom_white);
            }
            if (isException) {
                Toast.makeText(getContext(), "Заполните все обязательные поля корректно", Toast.LENGTH_SHORT).show();
            } else {
                dbPatient.add(patient);
                Toast.makeText(getContext(), "Пациент был создан!", Toast.LENGTH_LONG).show();
                dismiss();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Пациент не был создан!", Toast.LENGTH_SHORT).show();
        }
    }
}
