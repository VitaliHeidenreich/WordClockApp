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

    private EditText editConnectivity;
    //private EditText edit_port;
    private ExampleDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder((getActivity()));

        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View mView = mInflater.inflate(R.layout.layout_settings, null);

        editConnectivity = (EditText) mView.findViewById(R.id.editConnectivity);

        mBuilder.setView(mView)
                .setTitle("Settings")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String textToBeSended = editConnectivity.getText().toString();
                        listener.applyTexts(textToBeSended);
                    }
                });
        //sendMessageText = getView().findViewById(R.id.edit_ip);
        return mBuilder.create();
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            listener = (ExampleDialogListener) context;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(context.toString()+"bla");
        }
    }

    public interface ExampleDialogListener{
        void applyTexts(String textToBeSended);
    }
}
