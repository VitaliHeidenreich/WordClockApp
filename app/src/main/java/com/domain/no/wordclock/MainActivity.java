package com.domain.no.wordclock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
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


public class MainActivity extends AppCompatActivity implements SettingsDialog.ExampleDialogListener{

    //Statische Variablen zum Einstellen der APP --> TODEL
    public static String textBuffer;

    EspDevice ed = new EspDevice();
    BluetoothConnectivity bt = new BluetoothConnectivity();
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Handler handler;

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
        setContentView(R.layout.hauptprogramm);

        //Buttons
        btnSendMessage = findViewById(R.id.sendMessageBtn);

        //TestMessages
        sendText = findViewById(R.id.sendMessage);

        receiveText = findViewById(R.id.receiveMessage);

        textViewColor = findViewById(R.id.textViewColor);
        txtConnectionstatus = (TextView) findViewById(R.id.txtConnectionstatus);

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

        connectionStatus(false);
        connectToDevice();

        bt.setBluetoothManager((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE));
        bt.setBluetoothAdapter(bt.getBluetoothManager().getAdapter());
        bt.setBluetoothScanner(bt.getBluetoothAdapter().getBluetoothLeScanner());


        if (bt.getBluetoothAdapter() != null && !bt.getBluetoothAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            receiveText.append("Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");

            // auto scroll for text view
            final int scrollAmount = receiveText.getLayout().getLineTop(receiveText.getLineCount()) - receiveText.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                receiveText.scrollTo(0, scrollAmount);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        receiveText.setText("");
        //startScanningButton.setVisibility(View.INVISIBLE);
        //stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bt.getBluetoothScanner().startScan(leScanCallback);
            }
        });
        handler = new Handler();
        handler.postDelayed(this::stopScanning, 2000);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        receiveText.append("Stopped Scanning");
        //startScanningButton.setVisibility(View.VISIBLE);
        //stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bt.getBluetoothScanner().stopScan(leScanCallback);
            }
        });
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
                //sendString( "Hallo Welt! \n\r" );
                startScanning();
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
}
