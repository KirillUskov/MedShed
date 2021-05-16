package by.kirill.uskov.medsched;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import by.kirill.uskov.medsched.adapters.ProcedureAdapter;
import by.kirill.uskov.medsched.models.Procedure;
import by.kirill.uskov.medsched.utils.DBUtils;
import by.kirill.uskov.medsched.utils.ThemeUtil;

public class ViewProcedureActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;

    private DBUtils dbUtil;
    private DBUtils.DBProcedures dbProcedures;

    private ArrayList<String> procedureList = new ArrayList<>();

    private RecyclerView procedureRV;

    private TextView noProceduresTextView;

    private ProcedureAdapter procedureAdapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.getInstance().onActivityCreateSetTheme(this);

        dbUtil = new DBUtils();
        dbProcedures = dbUtil.getProc();
        dbProcedures.setAll();

        setContentView(R.layout.activity_view_procedures);
        procedureRV = findViewById(R.id.procedures_recycler_view);
        noProceduresTextView = findViewById(R.id.no_procedures_available_text_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        procedureRV.setLayoutManager(layoutManager);

        procedureAdapter = new ProcedureAdapter(procedureList);

        setList();
        procedureRV.setAdapter(procedureAdapter);
    }

    private void setRVVisibility() {
        if (procedureList.size() == 0) {
            procedureRV.setVisibility(View.INVISIBLE);
            noProceduresTextView.setVisibility(View.VISIBLE);
        } else {
            procedureRV.setVisibility(View.VISIBLE);
            noProceduresTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void setList() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getuserProceduresCode());
            databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (procedureList.size() > 0) {
                            procedureList.clear();
                        }
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Procedure procedure = ds.getValue(Procedure.class);
                            procedure.setId(ds.getKey());
                            procedureList.add(procedure.name);
                        }
                        Collections.sort(procedureList);
                        procedureAdapter.notifyDataSetChanged();
                        setRVVisibility();
                    } else {
                    }
                }
            });
        } catch (RuntimeException e) {
            Log.e("ViewProcedureActivity", e.getMessage());
        }
    }

    public void addNewProcedure(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить процедуру");

        EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                procedureList.add(input.getText().toString());
                setRVVisibility();
                procedureAdapter.notifyDataSetChanged();
                dbProcedures.add(input.getText().toString());
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

   /*public void setList() {
        databaseReference = FirebaseDatabase.getInstance().getReference(dbUtil.getuserProceduresCode());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (procedureList.size() > 0) {
                    procedureList.clear();
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Procedure procedure = ds.getValue(Procedure.class);
                    procedure.setId(ds.getKey());
                    procedureList.add(procedure.name);
                }
                Collections.sort(procedureList);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }*/

    public void backToSettings(View view) {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        finish();
    }
}