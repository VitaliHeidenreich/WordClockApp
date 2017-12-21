package com.domain.no.wordclock;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    PrintStream printer= null;
    Handler UIHandler;
    Thread CteateSocketThread = null;

    //Objekte anlegen
    private EditText sendText;
    private EditText receiveText;
    private Button btnSendMessage;
    private Button btnPortD6Control;
    private Button btnPortD7Control;

    public static final int SERVERPORT = 23;
    public static final String SERVERIP = "192.168.1.200";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendText = (EditText) findViewById(R.id.sendMessage);
        btnSendMessage = (Button) findViewById(R.id.sendMessageBtn);
        receiveText = (EditText) findViewById(R.id.receiveMessage);
        btnPortD6Control = (Button) findViewById(R.id.pinD6Control);
        btnPortD7Control = (Button) findViewById(R.id.pinD7Control);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printer.println(sendText.getText());
                sendText.setText("");
            }
        });
        btnPortD6Control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printer.println("X");
            }
        });
        btnPortD7Control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printer.println("Y");
            }
        });

        //Empfangsbox soll nicht editierbar sein
        receiveText.setKeyListener(null);

        UIHandler = new Handler();
        this.CteateSocketThread = new Thread(new CteateSocketThread());
        this.CteateSocketThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class CteateSocketThread implements Runnable{
        public void run(){
            Socket socket = null;
            try{
                socket = new Socket(InetAddress.getByName(SERVERIP), SERVERPORT);
                CreateReaderAndPrinterThread commThread = new CreateReaderAndPrinterThread(socket);
                new Thread(commThread).start();
                return;
            }catch(IOException e){
                e.printStackTrace();
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
}
