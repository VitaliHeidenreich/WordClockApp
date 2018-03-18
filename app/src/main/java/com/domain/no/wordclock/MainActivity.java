package com.domain.no.wordclock;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    PrintStream printer= null;
    Handler UIHandler;
    Thread CteateSocketThread = null;
    Socket socket = null;

    //Objekte anlegen
    private EditText sendText;
    private EditText receiveText;
    private TextView textViewColor;

    private Button btnSendMessage;
    private Button btnConnect;
    private Button btnSetColor;

    private Toolbar myToolbar;

    ColorPickerDialog colorPickerDialog;
    int color = Color.parseColor("#33b5e5");

    //Testbutton
    private Button btnTest;

    public static final int SERVERPORT = 23;
    public static final String SERVERIP = "192.168.1.200";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        sendText = (EditText) findViewById(R.id.sendMessage);
        btnSendMessage = (Button) findViewById(R.id.sendMessageBtn);
        receiveText = (EditText) findViewById(R.id.receiveMessage);
        btnConnect = (Button) findViewById(R.id.btnConnectToServer);
        textViewColor = (TextView) findViewById(R.id.textViewColor);


        //Testbutton
        btnSetColor = (Button) findViewById(R.id.btnSetColor);
        btnSendMessage.setOnClickListener(btnListener);
        btnConnect.setOnClickListener(btnListener);


        //Testbutton
        btnSetColor.setOnClickListener(btnListener);

        //Empfangsbox soll nicht editierbar sein
        receiveText.setKeyListener(null);

        // Für die Server-Client-Kommunikation
        UIHandler = new Handler();
        this.CteateSocketThread = new Thread(new CteateSocketThread());
        this.CteateSocketThread.start();

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater myMenuInflater = getMenuInflater();
        myMenuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_setting){
            Toast.makeText(MainActivity.this,"Klicket on settings", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()==R.id.action_about_us){
            Toast.makeText(MainActivity.this,"Klicket on info", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    class CteateSocketThread implements Runnable{
        public void run(){
            try{
                socket = new Socket(InetAddress.getByName(SERVERIP), SERVERPORT);
                CreateReaderAndPrinterThread commThread = new CreateReaderAndPrinterThread(socket);
                new Thread(commThread).start();
                return;

            }catch(UnknownHostException e1){
                e1.printStackTrace();
                System.out.println("UnknownHostException: " + e1.toString());
                return;
            }catch(IOException e2) {
                e2.printStackTrace();
                return;
            }
        }
    }

    class CreateReaderAndPrinterThread implements Runnable{

        private Socket clientSocket;
        private BufferedReader input;

        public CreateReaderAndPrinterThread(Socket clientSocket){
            this.clientSocket = clientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                printer = new PrintStream(clientSocket.getOutputStream(),true);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            while(!Thread.currentThread().isInterrupted()){
                try{
                    String read = input.readLine();
                    if(read!=null){
                        UIHandler.post(new updateUIThread(read));
                    }
                    else
                    {
                        CteateSocketThread = new Thread(new CteateSocketThread());
                        CteateSocketThread.start();
                        return;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable{
        private String nachricht;
        public updateUIThread(String str)
        {
            this.nachricht = str;
        }
        @Override
        public void run(){
            receiveText.setText(receiveText.getText() + nachricht + "\n");
        }
    }

    // Auswertung der Tasten
    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view==btnConnect){
                if(socket!=null){
                    try{
                        socket.close();
                        Toast.makeText(MainActivity.this,"Reconnect to " + SERVERIP + ":" +SERVERPORT, Toast.LENGTH_SHORT).show();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                CteateSocketThread = new Thread(new CteateSocketThread());
                CteateSocketThread.start();
            }

            else if(socket!=null){
                if(view == btnSendMessage){
                    printer.println(sendText.getText());
                    sendText.setText("");
                }
                // Einstellung der Farbe
                else if(view == btnSetColor){
                    // Auswahl der Farbe
                    Toast.makeText(MainActivity.this,"Set Color", Toast.LENGTH_SHORT).show();
                    colorPickerDialog = new ColorPickerDialog(MainActivity.this, color);
                    //colorPickerDialog.setAlphaSliderVisible(true);
                    colorPickerDialog.setHexValueEnabled(true);
                    colorPickerDialog.setTitle("Farbauswahl");
                    colorPickerDialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void onColorChanged(int i) {
                            color = i;
                            textViewColor.setText("+++#"+Integer.toHexString(color).substring(2)+"$");
                            printer.println("+++#"+Integer.toHexString(color).substring(2)+"$");
                        }
                    });
                    colorPickerDialog.show();
                }
                else{}
            }
            else{
                Toast.makeText(MainActivity.this,"You are not connected to " + SERVERIP + ":" +SERVERPORT, Toast.LENGTH_SHORT).show();
            }


        }
    };
}
