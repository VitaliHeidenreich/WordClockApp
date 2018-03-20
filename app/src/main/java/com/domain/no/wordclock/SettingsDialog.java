package com.domain.no.wordclock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by vitali on 19.03.2018.
 */

public class SettingsDialog extends AppCompatDialogFragment {

    private EditText edit_ip;
    private EditText edit_port;
    private SettingsDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_settings, null);

        builder.setView(view)
                .setTitle("Settings")
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String ipAdress = edit_ip.getText().toString();
                        String portNumber = edit_port.getText().toString();
                        listener.applyTexts(ipAdress,portNumber);
                    }
                });

        edit_ip = view.findViewById(R.id.edit_ip);
        edit_port = view.findViewById(R.id.edit_port);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            listener = (SettingsDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "Error!");
        }
    }

    public interface SettingsDialogListener{
        void applyTexts(String ipAdress, String portNumber);
    }
}
