package com.domain.no.wordclock;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SettingsDialog.ExampleDialogListener{

    //Statische Variablen zum Einstellen der APP --> TODEL
    public static String textBuffer;

    EspDevice ed = new EspDevice();
    BluetoothConnectivity bt = new BluetoothConnectivity();


    //Objekte anlegen
    private EditText sendText;
    private EditText receiveText;
    private TextView textViewColor;
    private TextView txtConnectionstatus;

    //Buttons
    private Button btnSendMessage;
    private Button btnSetColor;
    private Button tbtn;

    private Toolbar myToolbar;

    ColorPickerDialog colorPickerDialog;
    int color = Color.parseColor("#33b5e5");

    //Für die Suche von Geräten
    String mArrayAdapter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        //Buttons
        btnSendMessage = findViewById(R.id.sendMessageBtn);


        //TestMessages
        sendText = findViewById(R.id.sendMessage);

        receiveText = findViewById(R.id.receiveMessage);

        textViewColor = findViewById(R.id.textViewColor);
        txtConnectionstatus = findViewById(R.id.txtConnectionstatus);

        //Testbutton
        btnSetColor = findViewById(R.id.btnSetColor);
        tbtn =  findViewById(R.id.tbtn);

        btnSendMessage.setOnClickListener(btnListener);

        //Testbutton
        btnSetColor.setOnClickListener(btnListener);

        //Empfangsbox soll nicht editierbar sein
        receiveText.setKeyListener(null);


        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        tbtn.setOnClickListener(btnListener);

        txtConnectionstatus.setText("You are not connected!\nPress \"CONNECT\"");
        txtConnectionstatus.setTextColor(Color.rgb(255,0,0));
        txtConnectionstatus.setBackgroundColor(Color.rgb(236, 168,178));
        connectToDevice();
    }

    public void onStart(){
        super.onStart();
        try {bt.getBluetoothSocket().close();} catch (Exception e) {}
        connectToDevice();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void connectToDevice()
    {
        if( ed.getConAddress() != null )
        {
            BluetoothDevice dispositivo = bt.getBluetoothAdapter().getRemoteDevice(ed.getConAddress());
            //connects to the device's address and checks if it's available
            try
            {
                bt.setBluetoothSocket(dispositivo.createInsecureRfcommSocketToServiceRecord(bt.getUUID()));//create a RFCOMM (SPP) connection
                bt.getBluetoothSocket().connect();
                connectionStatus(true);
            }
            catch(Exception e){Toast.makeText(getApplicationContext(),"Nope! (2)", Toast.LENGTH_SHORT).show();}
        }
        else{
            Toast.makeText(MainActivity.this,"No WordUhr device found. Only following devices are connected:\n" + mArrayAdapter, Toast.LENGTH_SHORT).show();
        }
    }

    public String getSetting()
    {
        return textBuffer;
    }

    public void openDialog()
    {
        SettingsDialog settingDialog = new SettingsDialog();
        settingDialog.show(getSupportFragmentManager(),"My Dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater myMenuInflater = getMenuInflater();
        myMenuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId()==R.id.action_setting){
            openDialog();
        }
        if(item.getItemId()==R.id.action_about_us)
        {
            Toast.makeText(MainActivity.this,"No info yet", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Auswertung der Tasten
    private View.OnClickListener btnListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            if ( view==tbtn )
            {
                sendString( "Hallo Welt! \n\r" );
            }
            else if ( view == btnSendMessage)
            {
                if ( bt.getBluetoothAdapter().isEnabled() )
                {
                    sendString( sendText.getText().toString() );
                    sendText.setText("");
                }
            }
            else if(view == btnSetColor){
                // Auswahl der Farbe
                colorPickerDialog = new ColorPickerDialog(MainActivity.this, color);
                //colorPickerDialog.setAlphaSliderVisible(true);
                colorPickerDialog.setHexValueEnabled(true);
                colorPickerDialog.setTitle("Color picker");
                colorPickerDialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        color = i;
                        textViewColor.setText("+++#"+Integer.toHexString(color).substring(2)+"$");
                        sendString("+++#"+Integer.toHexString(color).substring(2)+"$");
                    }
                });
                colorPickerDialog.show();
            }
            else{
                Toast.makeText(MainActivity.this,"Sorry! NOP for this button!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void sendString( String str )
    {
        try
        {
            if ( bt.getBluetoothSocket() != null )
            {
                bt.getBluetoothSocket().getOutputStream().write(str.getBytes());
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            connectionStatus(false);
        }
    }

    @Override
    public void applyTexts(String textToBeSended){
        textBuffer = textToBeSended;
        receiveText.setText(receiveText.getText().toString() + "\n" + textToBeSended);
    }

    private void connectionStatus( boolean con ){
        if(con){
            txtConnectionstatus.setText("You are connected to:\nName: "+ed.getConName()+"\nAddress: "+ed.getConAddress());
            txtConnectionstatus.setTextColor(Color.rgb(30,155,30));
            txtConnectionstatus.setBackgroundColor(Color.rgb(200, 250,200));
        }
        else{
            txtConnectionstatus.setText("Ups, connection lost!\nPlz restart the app.");
            txtConnectionstatus.setTextColor(Color.rgb(255,0,0));
            txtConnectionstatus.setBackgroundColor(Color.rgb(236, 168,178));
        }
    }
}
