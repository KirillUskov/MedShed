package by.kirill.uskov.medsched.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.entities.patients.Patient;
import by.kirill.uskov.medsched.utils.DBUtils;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class AddPatientDialog extends AppCompatDialogFragment {
    private DBUtils dbUtil;
    private DBUtils.DBPatient dbPatient;

    private EditText patientName;
    private EditText patientBDDate;
    private EditText patientPhone;
    private EditText patientComment;

    private Button addButton;
    private Button cancelButton;

    private Patient patient;



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

        addButton.setBackgroundResource(R.drawable.bg_custom_green_button);

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
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }

    public void addPatientClick() {
        String name = patientName.getText().toString();
        String dbDate = patientBDDate.getText().toString();
        String phone = patientPhone.getText().toString();
        String comment = patientComment.getText().toString();
        if (comment.length() > 0) {
            patient = new Patient(name, dbDate, phone, comment);
        } else {
            patient = new Patient(name, dbDate, phone);
        }
        dbPatient.add(patient);
    }
}
