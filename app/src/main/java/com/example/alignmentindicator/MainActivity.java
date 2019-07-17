package com.example.alignmentindicator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.nfc.Tag;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.AlignCar.AlignmentCar.UnityPlayerActivity;
//import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    //  private UnityPlayer m;
    private TextView mBluetoothStatus;
    private TextView X,Y,Z;
    private TextView A,B,C;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private Button mUnity;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private String readMessage;

    String xV,yV,zV,X1,Y1,Z1;
    String aV,bV,cV,A1,B1,C1;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private int x,y,z;
    int bytesA = -1,bytesR = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//         Create the UnityPlayer
//        m = new UnityPlayer(this);
//        int glesMode = m.getSettings().getInt("gles_mode", 1);
//        boolean trueColor8888 = false;
//        m.init(glesMode, trueColor8888);

        setContentView(R.layout.activity_main);

        // Add the Unity view
//        FrameLayout layout = (FrameLayout) findViewById(R.id.frameLayout);
////        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        layout.addView(m.getView());
//        m.windowFocusChanged(true);
//        m.resume();

        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
//        mX = (TextView) findViewById(R.id.valueX);
//        mY = (TextView) findViewById(R.id.valueY);
//        mZ = (TextView) findViewById(R.id.valueZ);

        X = (TextView) findViewById(R.id.valueX);
        Y = (TextView) findViewById(R.id.valueY);
        Z = (TextView) findViewById(R.id.valueZ);

        A = (TextView) findViewById(R.id.valueA);
        B = (TextView) findViewById(R.id.valueB);
        C = (TextView) findViewById(R.id.valueC);
//        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
//        mReadBuffer1 = (TextView) findViewById(R.id.readBuffer1);
//        mAvailable = (TextView) findViewById(R.id.availableB);
        mScanBtn = (Button) findViewById(R.id.scan);
        mOffBtn = (Button) findViewById(R.id.off);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
//        mUnity = (Button) findViewById(R.id.Unity);

//        mLED1 = (CheckBox)findViewById(R.id.checkboxLED1);

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //Log.e("mHandler", "mHandler Running");
                String temp;
                if (msg.what == MESSAGE_READ) {

                    // construct a string from the valid bytes in the buffer
                    byte[] readBuf;
                    try {

                        //Log.e("Before readBuf", "Before");
                        readBuf = (byte[]) msg.obj;
                        //Log.e("After readBuf", "After");
                        //readMessage = new String(readBuf, 0, msg.arg1);
                        //Log.e("Message", "Message: "+readMessage);
                        //Log.e("Before readMessage", "Before");
                        //String readMessage1 = new String((byte [])msg.obj, "UTF-8");
                        //Log.e("Message", "Message: "+readMessage1);

                        if(readMessage.contains("()")){
                            if(readMessage.indexOf("A=") == 0){
                                aV = readMessage;
                                aV = aV.substring(2);
                                if(aV.contains("-")){
                                    A1 = readMessage.substring(readMessage.indexOf("-"),readMessage.indexOf("()"));
                                }
                                else{
                                    A1 = readMessage.substring(readMessage.indexOf("=")+1,readMessage.indexOf("()"));
                                }

//                                mZ.setText(zV);
                                A.setText(A1);
                            }
                            else if(readMessage.indexOf("B=") == 0){
                                bV = readMessage;
                                bV = bV.substring(2);
                                if(bV.contains("-")){
                                    B1 = readMessage.substring(readMessage.indexOf("-"),readMessage.indexOf("()"));
                                }
                                else{
                                    B1 = readMessage.substring(readMessage.indexOf("=")+1,readMessage.indexOf("()"));
                                }

//                                mZ.setText(zV);
                                B.setText(B1);
                            }
                            else if(readMessage.indexOf("C=") == 0){
                                cV = readMessage;
                                cV = cV.substring(2);
                                if(cV.contains("-")){
                                    C1 = readMessage.substring(readMessage.indexOf("-"),readMessage.indexOf("()"));
                                }
                                else{
                                    C1 = readMessage.substring(readMessage.indexOf("=")+1,readMessage.indexOf("()"));
                                }

//                                mZ.setText(zV);
                                C.setText(C1);
                            }
                            else if(readMessage.indexOf("X=") == 0) {
                                xV = readMessage;
                                xV = xV.substring(2);
                                if(readMessage.contains("-")){
                                    X1 = readMessage.substring(readMessage.indexOf("-"),readMessage.indexOf("()"));
                                }
                                else{
                                    X1 = readMessage.substring(readMessage.indexOf("=")+1,readMessage.indexOf("()"));
                                }
//                                mX.setText(xV);
                                X.setText(X1);
                            }
                            else if(readMessage.indexOf("Y=") == 0){
                                yV = readMessage;
                                yV = yV.substring(2);
                                if(yV.contains("-")){
                                    Y1 = readMessage.substring(readMessage.indexOf("-"),readMessage.indexOf("()"));
                                }
                                else{
                                    Y1 = readMessage.substring(readMessage.indexOf("=")+1,readMessage.indexOf("()"));
                                }
//                                mY.setText(yV);
                                Y.setText(Y1);
                            }
                            else if(readMessage.indexOf("Z=") == 0) {
                                zV = readMessage;
                                zV = zV.substring(2);
                                if (zV.contains("-")) {
                                    Z1 = readMessage.substring(readMessage.indexOf("-"), readMessage.indexOf("()"));
                                } else {
                                    Z1 = readMessage.substring(readMessage.indexOf("=") + 1, readMessage.indexOf("()"));
                                }

//                                mZ.setText(zV);
                                Z.setText(Z1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        }

        else {

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v){ listPairedDevices(v); }});

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }

    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            String connecting = "Connecting...";
            mBluetoothStatus.setText(connecting);
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        private ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;

        }

        public void run() {

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    byte[] buffer = new byte[1024];  // buffer store for the stream
                    // Read from the InputStream
                    bytesA = mmInStream.available();
//                    mAvailable.setText(bytes);

                    if(bytesA != 0) {
                        try {
                            SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                            bytesA = mmInStream.available(); // how many bytes are ready to be read?
                            //Log.e("Available", "Available Bytes: "+bytesA);
//                        mAvailable.setText(bytesA);
//                        bytesA = mmInStream.available();

                            bytesR = mmInStream.read(buffer); // record how many bytes we actually read
                            //Log.e("Received", "Received Bytes: "+bytesR);
//                        mReadBuffer.setText("We Reading "+bytes+" bytes");

                            readMessage = new String(buffer, 0, bytesR);

                            Log.e("Avein", "Received Message: " + readMessage);

                            mHandler.obtainMessage(MESSAGE_READ, bytesR, -1)
                                    .sendToTarget(); // Send the obtained bytes to the UI activity

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        SystemClock.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
