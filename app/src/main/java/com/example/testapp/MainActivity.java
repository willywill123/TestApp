package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private static boolean IN_KEYTEST = false;
    private static boolean IN_TYPETEST = false;
    private static String typeText = "The quick brown fox jumped over the lazy dog.";
    ArrayList<String> typeTextArr = new ArrayList<String>();
    private static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        //final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final TextView updateKey = findViewById(R.id.updateKey);
        final TextView connectText = findViewById(R.id.connectText);
        final TextView typingTestText = findViewById(R.id.typingTestText);
        final TextView typingTestText2 = findViewById(R.id.typingTestText2);
        typingTestText2.setVisibility(View.GONE);
        typingTestText.setVisibility(View.GONE);
        updateKey.setVisibility(View.GONE);

        //final Button buttonToggle = findViewById(R.id.buttonToggle);
        final Button keyTestButton = findViewById(R.id.keyTestButton);
        keyTestButton.setEnabled(false);
        final Button returnButton = findViewById(R.id.returnButton);
        returnButton.setEnabled(false);
        //buttonToggle.setEnabled(false);
        final Button typingTestButton = findViewById(R.id.typingTestButton);
        typingTestButton.setEnabled(false);
        //final TextView updateKey = findViewById(R.id.updateKey);
        //final ImageView imageView = findViewById(R.id.imageView);
        //imageView.setBackgroundColor(Color.BLACK);

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                //buttonToggle.setEnabled(true);
                                keyTestButton.setEnabled(true);
                                typingTestButton.setEnabled(true);
                                connectText.setVisibility(View.GONE);


                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                //buttonConnect.setEnabled(true);
                                keyTestButton.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:

                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        /*Context context = getApplicationContext();
                        CharSequence text = arduinoMsg;
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();*/
                        //updateKey = (TextView)findViewById(R.id.updateKey);
                        //int convertedmsg = Integer.valueOf(arduinoMsg);

                        if (!arduinoMsg.isEmpty())
                        {
                            if (IN_TYPETEST)
                            {
                                //typeText;
                                //typeTextArr;
                                if (typeText.charAt(counter) == arduinoMsg.charAt(0))
                                {

                                    String next = "<font color = '#000000'>" + typeText.charAt(counter) + "</font>";
                                    typeTextArr.add(next);

                                }
                                else {
                                    String next = "<font color = '#EE0000'>" + typeText.charAt(counter) + "</font>";
                                    typeTextArr.add(next);
                                }
                                String build = "";
                                for (String c: typeTextArr)
                                {
                                    build += c;
                                }
                                counter++;
                                typingTestText.setText(Html.fromHtml(build));
                            }
                            if (IN_KEYTEST) {


                                if (arduinoMsg.charAt(0) == 0x08) {
                                    updateKey.setText("You pressed the BACKSPACE key.");
                                } else if (arduinoMsg.charAt(0) == 0x09) {
                                    updateKey.setText("You pressed the TAB key.");
                                } else if (arduinoMsg.charAt(0) == 0x1B) {
                                    updateKey.setText("You pressed the ESC key.");
                                } else if (arduinoMsg.charAt(0) == 0x20) {
                                    updateKey.setText("You pressed the SPACE key.");
                                } else if (arduinoMsg.charAt(0) == 0x0D) {
                                    updateKey.setText("You pressed the ENTER key.");
                                } else {
                                    updateKey.setText(String.format("You pressed the %s key.", arduinoMsg));
                                }
                                //updateKey.setText(arduinoMsg);
                            }
                        }

                        //updateKey.setText(arduinoMsg);
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });


        //go to key test screen
        keyTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(view.getContext(), KeyTestActivity.class);
                Context context = getApplicationContext();
                CharSequence text = "hello world";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                startActivity(intent);*/
                updateKey.setVisibility(View.VISIBLE);
                keyTestButton.setEnabled(false);
                typingTestButton.setEnabled(false);
                returnButton.setEnabled(true);
                IN_KEYTEST = true;
                }
        });
        //go to typing test
        typingTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnButton.setEnabled(true);
                keyTestButton.setEnabled(false);
                typingTestButton.setEnabled(false);
                typingTestText.setVisibility(View.VISIBLE);
                typingTestText2.setVisibility(View.VISIBLE);
                IN_TYPETEST = true;
                counter = 0;
                //Color.parseColor("#bdbdbd");

                //String first = "<font color = '#EEFFBB'>This text is </font>";

                //String next = "<font color = '#EE0000'>red</font>";
                //typingTestText.setText(Html.fromHtml(first + next));
                //typingTestText.setTextColor(Color.parseColor("#bdbdbd"));


            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnButton.setEnabled(false);
                keyTestButton.setEnabled(true);
                typingTestButton.setEnabled(true);
                updateKey.setVisibility(View.GONE);
                typingTestText.setVisibility(View.GONE);
                typingTestText2.setVisibility(View.GONE);
                IN_KEYTEST = false;
                IN_TYPETEST = false;

            }
        });

    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */

                    // IoT: Changed bluetooth behavior to always send latest input data, regardless
                    // of new line character.
                    char buf = (char) mmInStream.read();
                    String readMessage = String.valueOf(buf);
                    handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                    /*
                    buffer[bytes] = (byte) mmInStream.read();
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }

                    */
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
            //this is a comment
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}


