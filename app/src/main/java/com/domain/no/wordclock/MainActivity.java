package com.domain.no.wordclock;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity /*implements SettingsDialog.SettingsDialogListener*/{


    //BT;
    String address = null , name=null;
    private static final String TAG = "MainActivity";
    //BT
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket mBluetoothSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //BT
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            //When discovery finds a device
            if( action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED) ){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    //case BluetoothAdapter.STATE_OFF: Log.d(TAG, "onReceiver1: state off"); break;
                    //case BluetoothAdapter.STATE_TURNING_OFF: Log.d(TAG, "onReceiver1: state turning off"); break;
                    //case BluetoothAdapter.STATE_ON: Log.d(TAG, "onReceiver1: state on"); break;
                    //case BluetoothAdapter.STATE_TURNING_ON: Log.d(TAG, "onReceiver1: state turning off"); break;
                    default:
                        break;
                }
            }
        }
    };

    //Objekte anlegen
    private EditText sendText;
    private EditText receiveText;
    private TextView textViewColor;
    private TextView txtConnectionstatus;

    private Button btnSendMessage;
    private Button btnConnect;
    private Button btnSetColor;
    private Button btnDisconnect;

    private Toolbar myToolbar;

    private Button tbtn;

    ColorPickerDialog colorPickerDialog;
    int color = Color.parseColor("#33b5e5");

    //Testbutton
    private Button btnTest;

//    @Override
//    public void applyTexts(String ipAdress, String portNumber) {
//        receiveText.setText(ipAdress + "\n" + portNumber);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        //BT
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Buttons
        btnSendMessage = (Button) findViewById(R.id.sendMessageBtn);
        btnConnect = (Button) findViewById(R.id.btnConnectBT);


        //TestMessages
        sendText = (EditText) findViewById(R.id.sendMessage);

        receiveText = (EditText) findViewById(R.id.receiveMessage);

        textViewColor = (TextView) findViewById(R.id.textViewColor);
        txtConnectionstatus = (TextView) findViewById(R.id.txtConnectionstatus);
        btnDisconnect = (Button) findViewById(R.id.btnDisConnectBT);

        //Testbutton
        btnSetColor = (Button) findViewById(R.id.btnSetColor);
        tbtn = (Button) findViewById(R.id.tbtn);
        btnSendMessage.setOnClickListener(btnListener);
        btnConnect.setOnClickListener(btnListener);
        btnDisconnect.setOnClickListener(btnListener);

        //Testbutton
        btnSetColor.setOnClickListener(btnListener);

        //Empfangsbox soll nicht editierbar sein
        receiveText.setKeyListener(null);


        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        tbtn.setOnClickListener(btnListener);

        txtConnectionstatus.setText("You are not connected!\nPress \"CONNECT\"");

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setw() throws IOException
    {
        bluetooth_connect_device();
    }

    private void bluetooth_connect_device() throws IOException
    {
        try
        {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            address = mBluetoothAdapter.getAddress();
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size()>0)
            {
                for (BluetoothDevice bt : pairedDevices)
                {
                    address = bt.getAddress();
                    name = bt.getName();
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch(Exception we){Toast.makeText(getApplicationContext(),"Nope!(1)", Toast.LENGTH_SHORT).show();}

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
        mBluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(mUUID);//create a RFCOMM (SPP) connection
        mBluetoothSocket.connect();

        try
        {
            txtConnectionstatus.setText("BT Name: "+name+"\nBT Address: "+address);
        }
        catch(Exception e){Toast.makeText(getApplicationContext(),"Nope! (2)", Toast.LENGTH_SHORT).show();}
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //unregisterReceiver(BroadcastReceiver);
    }

    public void openDialog()
    {
        SettingsDialog settingDialog = new SettingsDialog();
        settingDialog.show(getSupportFragmentManager(),"Example Dialog");
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
            Toast.makeText(MainActivity.this,"Klicket on info", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }



    // Auswertung der Tasten
    private View.OnClickListener btnListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            //turn bluetooth on and off
            if( view==btnConnect )
            {
                if(mBluetoothAdapter==null)
                {
                    Log.d(TAG, "MyMassage: No BT!");
                }
                if(!mBluetoothAdapter.isEnabled())
                {
                    Intent enableTBIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableTBIntent);
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1,BTIntent);
                }
                try
                {
                    setw();
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this,"Verbindungsfehler!", Toast.LENGTH_SHORT).show();
                }
            }

            else if ( view==btnDisconnect )
            {
                if(mBluetoothAdapter==null)
                {
                    Log.d(TAG, "Sorry! You have no BT on your device!");
                }
                if ( mBluetoothAdapter.isEnabled() )
                {
                    mBluetoothAdapter.disable();
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1,BTIntent);
                }
            }
            else if ( view==tbtn )
            {
                sendString( "Hallo Welt!\n\r" );
            }
            else if ( view == btnSendMessage)
            {
                if ( mBluetoothAdapter.isEnabled() )
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
                colorPickerDialog.setTitle("Farbauswahl");
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
            if ( mBluetoothSocket != null )
            {
                mBluetoothSocket.getOutputStream().write(str.getBytes());
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
