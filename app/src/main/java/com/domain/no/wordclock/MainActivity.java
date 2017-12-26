package com.domain.no.wordclock;

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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
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
    private Button btnSendMessage;
    private Button btnPortD6Control;
    private Button btnPortD7Control;
    private Button btnConnect;
    private Toolbar myToolbar;

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
        btnPortD6Control = (Button) findViewById(R.id.pinD6Control);
        btnPortD7Control = (Button) findViewById(R.id.pinD7Control);
        btnConnect = (Button) findViewById(R.id.btnConnectToServer);

        //Testbutton
        btnTest = (Button) findViewById(R.id.btnTestButton);

        btnSendMessage.setOnClickListener(btnListener);
        btnPortD6Control.setOnClickListener(btnListener);
        btnPortD7Control.setOnClickListener(btnListener);
        btnConnect.setOnClickListener(btnListener);

        //Testbutton
        btnTest.setOnClickListener(btnListener);

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
                else if(view == btnPortD6Control){
                    printer.println("X");
                }
                else if(view == btnPortD7Control){
                    printer.println("Y");
                }
                else{}
            }
            else{
                Toast.makeText(MainActivity.this,"You are not connected to " + SERVERIP + ":" +SERVERPORT, Toast.LENGTH_SHORT).show();
            }

            //Testbutton
            if(view == btnTest){
                Esp8266Control abc = new Esp8266Control();
                Toast.makeText(MainActivity.this,"Das Kommando lautet: " + abc.getString(), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
