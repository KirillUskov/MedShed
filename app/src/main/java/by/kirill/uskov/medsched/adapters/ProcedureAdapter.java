package by.kirill.uskov.medsched.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import by.kirill.uskov.medsched.R;
import by.kirill.uskov.medsched.models.CalendarEvent;
import by.kirill.uskov.medsched.utils.DBUtils;

public class ProcedureAdapter extends RecyclerView.Adapter<ProcedureAdapter.ViewHolder> {
    private ArrayList<String> procedureList;
    private DBUtils.DBProcedures dbProcedures;

    public ProcedureAdapter(ArrayList<String> list) {
        procedureList = list;
        dbProcedures = new DBUtils().getProc();
        dbProcedures.setAll();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.procedure_item, parent, false);
        return new ProcedureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ProcedureAdapter.ViewHolder holder, int position) {
        Collections.sort(procedureList);
        holder.bind(procedureList.get(position));
    }

    @Override
    public int getItemCount() {
        return procedureList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView procedure;
        private ImageView editAction;
        private ImageView deleteAction;

        private String newProcedureText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            procedure = itemView.findViewById(R.id.procedure_text_view);

            editAction = itemView.findViewById(R.id.edit_image_view);
            deleteAction = itemView.findViewById(R.id.delete_image_view);

            editAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView procTV = itemView.findViewById(R.id.procedure_text_view);
                    String procedureText = procTV.getText().toString();
                    updateProcedure(itemView.getContext(), procedureText);
                }
            });

            deleteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView procTV = itemView.findViewById(R.id.procedure_text_view);
                    String procedureText = procTV.getText().toString();
                    procedureList.remove(procedureText);
                    dbProcedures.remove(procedureText);
                    Collections.sort(procedureList);
                    notifyDataSetChanged();

                    Toast.makeText(itemView.getContext(), "Процедура удалена!", Toast.LENGTH_LONG).show();
                }
            });
        }

        public void bind(String procedureText) {
            procedure.setText(procedureText);
        }

        public void updateProcedure(Context context, String text) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Обновить процедуру");

            EditText input = new EditText(context);
            // Specify the type of input expected
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(text);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newProcedureText = input.getText().toString();
                    procedureList.add(newProcedureText);
                    procedureList.remove(text);
                    if(newProcedureText != text) {
                        dbProcedures.update(text, newProcedureText);
                    }
                    Collections.sort(procedureList);
                    notifyDataSetChanged();
                    Toast.makeText(itemView.getContext(), "Процедура обновлена!", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newProcedureText = text;
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }
}
