package com.domain.no.wordclock;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import petrov.kristiyan.colorpicker.ColorPicker;


public class MainActivity extends AppCompatActivity implements SettingsDialog.ExampleDialogListener{

    //Statische Variablen zum Einstellen der APP --> TODEL
    public static String textBuffer;

    EspDevice ed = new EspDevice();
    BluetoothConnectivity bt = new BluetoothConnectivity();

    String messageLog;
    private int mHour, mMinute;

    // Time Picker
    TimePickerDialog timePickerDialog;

    //Objekte anlegen
    private EditText sendText;
    private EditText receiveText;
    private TextView textViewColor;
    private TextView txtConnectionstatus;

    //Buttons
    private Button btnSendMessage;
    private Button btnSetColor;
    private Button tbtn;
    private Button btnLED;
    private Button btnActualTime;

    private Toolbar myToolbar;

    private CheckBox checkBoxTimeSetting;

    ColorPickerDialog colorPickerDialog;
    int color = Color.parseColor("#33b5e5");

    //Für die Suche von Geräten
    String mArrayAdapter = "";

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hauptprogramm);

        //Buttons
        btnSendMessage = findViewById(R.id.sendMessageBtn);
        btnLED = findViewById(R.id.btnSetColorToDef);
        btnActualTime = findViewById(R.id.sendAndroidTime);

        //TestMessages
        sendText = findViewById(R.id.sendMessage);

        receiveText = findViewById(R.id.receiveMessage);

        textViewColor = findViewById(R.id.textViewColor);
        txtConnectionstatus = (TextView) findViewById(R.id.txtConnectionstatus);

        checkBoxTimeSetting = findViewById(R.id.checkBoxTimeSetting);

        //Testbutton
        btnSetColor = findViewById(R.id.btnSetColor);
        tbtn =  findViewById(R.id.tbtn);

        btnSendMessage.setOnClickListener(btnListener);

        //Testbutton
        btnSetColor.setOnClickListener(btnListener);
        btnLED.setOnClickListener(btnListener);
        btnActualTime.setOnClickListener(btnListener);

        //Empfangsbox soll nicht editierbar sein
        receiveText.setKeyListener(null);


        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        tbtn.setOnClickListener(btnListener);

        connectionStatus(false);
        connectToDevice();

        handler = new Handler();

        handler.postDelayed(readTextFromBT, 1);
    }

    public void onStart(){
        super.onStart();
        try { bt.getBluetoothSocket().close();} catch (Exception e) {}
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
                bt.setBluetoothSocket(dispositivo.createInsecureRfcommSocketToServiceRecord(bt.getUUID()));
                bt.getBluetoothSocket().connect();
                connectionStatus(true);
            }
            catch(Exception e){Toast.makeText(getApplicationContext(),"Nope! (2)", Toast.LENGTH_SHORT).show();}
        }
        else{
            Toast.makeText(MainActivity.this,
                    "No WordUhr device found. Only following devices are connected:\n"
                            + mArrayAdapter, Toast.LENGTH_SHORT).show();
        }
        // Input und Output anlegen
        try
        {
            bt.setTmpIn(bt.getBluetoothSocket().getInputStream());
            bt.setTmpOut(bt.getBluetoothSocket().getOutputStream());
        }
        catch (IOException ex){
            ex.printStackTrace();
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
    // ####################################################################################################
    // Auswertung der Tasten   ############################################################################
    // ####################################################################################################
    private View.OnClickListener btnListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            if ( view==tbtn )
            {
                sendString("XT000000$\n");
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
                        textViewColor.setText("XF"+Integer.toHexString(color).substring(2)+"$\n");
                        sendString("XF"+Integer.toHexString(color).substring(2)+"$\n");
                    }

                });
                colorPickerDialog.show();
            }
            else if (view == btnLED){
                openColorPickerDialog();
            }
            else if (view == btnActualTime){

                if(checkBoxTimeSetting.isChecked()) {
                    Calendar currentTime = Calendar.getInstance();
                    int hours   = currentTime.get(Calendar.HOUR_OF_DAY);
                    int minute  = currentTime.get(Calendar.MINUTE);
                    int sekunde = currentTime.get(Calendar.SECOND);
                    sendString("XT"
                            + ((hours<10)?("0" + hours):(hours))
                            + ((minute<10)?("0" + minute):(minute))
                            + ((sekunde<10)?("0" + sekunde):(sekunde)) +"$\n");
                }
                else{
                    // Get Current Time
                    final Calendar c = Calendar.getInstance();
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    sendString("XT"
                                            + ((hourOfDay<10)?("0" + hourOfDay):(hourOfDay))
                                            + ((minute<10)?("0" + minute):(minute))
                                            + "00" +"$\n");
                                }
                            }, mHour, mMinute, true);
                    timePickerDialog.show();
                }
            }
            else{
                Toast.makeText(MainActivity.this,"ERROR! BUTTON", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void sendString( String str )
    {
        try {
            if ( bt.getBluetoothSocket() != null ){
                bt.getTmpOut().write(str.getBytes());
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            connectionStatus(false);
        }
    }
    private String readString()
    {
        byte[] buffer = new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()
        String readMessage = null;
        try {
            if ( (bt.getTmpIn()!=null) && ( bt.getTmpIn().available() > 0) ) {
                bytes = bt.getTmpIn().read(buffer);
                readMessage = new String(buffer, 0, bytes);
                Log.d(textBuffer, ">>>>>>>>>>>>>>>>>>> input data");
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            connectionStatus(false);
        }
        return readMessage;
    }

    @Override
    public void applyTexts(String textToBeSended){
        textBuffer = textToBeSended;
        receiveText.setText(receiveText.getText().toString() + "\n" + textToBeSended);
    }

    private void connectionStatus( boolean con ){
        try{
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
        catch(Exception e){
            Toast.makeText(MainActivity.this,e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    void displayText(){
        String myStg = readString();
        if(myStg!=null)
        {
            receiveText.setText(receiveText.getText() + myStg);
        }
    }


    private Runnable readTextFromBT = new Runnable() {
        @Override
        public void run() {
            try
            {
                displayText();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            handler.postDelayed(this, 1);
        }
    };

    public void openColorPickerDialog() {
        final ColorPicker colorPicker = new ColorPicker(this);
        ArrayList<String> color = new ArrayList<>();
        color.add("#FF0000");
        color.add("#FF8000");
        color.add("#0000FF");
        color.add("#00FF00");
        color.add("#FF00FF");

        color.add("#00FFFF");
        color.add("#FFA500");
        color.add("#CD853F");
        color.add("#E6E6FA");
        color.add("#FFFFFF");

        colorPicker.setColors(color)
                .setColumns(5)
                .setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        // Todo
                    }

                    @Override
                    public void onCancel() {

                    }
                }).show();
    }
}
