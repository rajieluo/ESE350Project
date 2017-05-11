package com.ese350.roger.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    File dataFile = null;
    FileOutputStream outputStream = null;
    Context appcontext;
    Activity activity;
    Boolean runReceiveThread = true;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};



    public void sendBtMsg(String msg2send){
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }
            String msg = msg2send;
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    final class senderThread implements Runnable {

        private String message;
        public senderThread(String msg) {
            message = msg;
        }

        public void run() {
            sendBtMsg(message);
        }
    }

    private File createFile(String filename) {
        File appDir = getFilesDir();
        File file = new File(appDir, filename);
        file.setWritable(true);
        return file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appcontext = getApplicationContext();
        activity = MainActivity.this;

        ActivityCompat.requestPermissions((Activity) getWindow().getContext() ,PERMISSIONS_STORAGE , REQUEST_EXTERNAL_STORAGE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Handler handler = new Handler();

        setContentView(R.layout.activity_main);

        final TextView titleText = (TextView) findViewById(R.id.titleText);
        final TextView errorText = (TextView) findViewById(R.id.errorText);
        final TextView responseText = (TextView) findViewById(R.id.responseText);
        final TextView bpmText = (TextView) findViewById(R.id.bpmText);
        final TextView outputText = (TextView) findViewById(R.id.outputText);

        final Button sendMsgButton = (Button) findViewById(R.id.sendMsgButton);
        final Button stopButton = (Button) findViewById(R.id.stopButton);
        final Button historyButton = (Button) findViewById(R.id.historyButton);
        final Button predictionButton = (Button) findViewById(R.id.predictionButton);

        final class receiverThread implements Runnable {

            public receiverThread() {
            }

            public void run() {

                handler.post(new Runnable()
                {
                    public void run()
                    {
                        responseText.setText("starting receiver");
                    }
                });
                while (runReceiveThread) {
                    try {
                        InputStream mmInputStream = mmSocket.getInputStream();
                        final int bytesAvailable = mmInputStream.available();

                        if (bytesAvailable > 0) {
                            sendMsgButton.setVisibility(View.GONE);
                            stopButton.setVisibility(View.VISIBLE);
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(readBuffer);
                            final String data = new String(readBuffer, "US-ASCII");
                            if (data.startsWith("stop")) {
                                runReceiveThread = false;
                                sendMsgButton.setVisibility(View.VISIBLE);
                                stopButton.setVisibility(View.GONE);
                            }
                            else if (data.startsWith("BPM: ")) {
                                handler.post(new Runnable() {
                                    public void run() {
                                        bpmText.setText(data);
                                    }
                                });
                            } else {
                                final String[] dataFields = data.split(",");
                                if (dataFile == null) {
                                    String outFileName = dataFields[1] + ".txt";
                                    dataFile = createFile(outFileName);
                                    dataFile.setReadable(true);
                                    dataFile.setWritable(true);
                                }
                                if (outputStream == null) {
                                    outputStream = openFileOutput(dataFile.getName(),
                                            Context.MODE_PRIVATE);
                                }
                                try {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            titleText.setText("movement: " + dataFields[3] + "avg BPM: " + dataFields[4]);
                                        }
                                    });
                                    outputStream.write(data.getBytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (runReceiveThread == false) {
                    try {
                        outputStream.close();
                        outputStream = null;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            errorText.setText("Null BTAdapter!");
        }

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread sendThread = new Thread(new senderThread("getData"));
                sendThread.start();
                Thread receiveThread = new Thread(new receiverThread());
                receiveThread.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread sendThread = new Thread(new senderThread("stopData"));
                sendThread.start();
                runReceiveThread = false;
                try{
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendMsgButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                responseText.setText("Stopped tracking");
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(appcontext, HistoryActivity.class);
                startActivity(intent);
            }
        });

        predictionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                outputText.setText(getMLData());
            }
        });

        if (mBluetoothAdapter != null) {
            if(!mBluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0)
            {
                for(final BluetoothDevice device : pairedDevices)
                {
                    if(device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                    {
                        mmDevice = device;
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                errorText.setText(device.getName());
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    private String getMLData() {
        Context appContext = getApplicationContext();
        File[] filesArr = appContext.getFilesDir().listFiles();
        ArrayList<DataPoint> heartrateDataArrayList = new ArrayList<>();
        ArrayList<Double> heartrateDataDoubleArrayList = new ArrayList<>();

        ArrayList<ArrayList<Integer>> history = new ArrayList<>();

        for (File file : filesArr) {
            try {
                String filePath = file.getAbsolutePath();
                FileInputStream fileInputStream = null;
                try{
                    fileInputStream = new FileInputStream(filePath);
                } catch (Exception E){
                    fileInputStream = new FileInputStream("/sdcard/Documents/smartsleep/" + file.getName());
                }

                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                StringBuilder string = new StringBuilder();
                String readLine = reader.readLine();
                Boolean firstLine = true;
                while (readLine != null) {
                    if (firstLine) {
                        firstLine = false;
                    } else {
                        string.append("\n");
                    }
                    string.append(readLine);
                    readLine = reader.readLine();
                };

                String infoString = string.toString();
                String[] infoArray = infoString.split("\\r?\\n");
                for (int i = 0; i < infoArray.length; i++) {
                    String[] lineArray = infoArray[i].split(",");
                    if (lineArray.length > 4) {
                        heartrateDataArrayList.add(new DataPoint(i, Double.valueOf(lineArray[4])));
                        heartrateDataDoubleArrayList.add(Double.valueOf(lineArray[4]));
                    }
                }

                if (infoArray.length > 90) {
                    ArrayList<Integer> remCycleTimes = smartsleep.getPeak(heartrateDataDoubleArrayList);
                    history.add(remCycleTimes);
                }
                reader.close();
                inputStreamReader.close();
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double[] prediction = predict.predict(history);
        Boolean isStartTime = true;
        StringBuilder displayCyclesString = new StringBuilder("");
        double startInt = 0;
        double totalRem = 0;
        for (Double i : prediction) {
            String iMinutes = String.valueOf(i % 60);
            String iHours = String.valueOf(((int) i.doubleValue()) / 60);
            if (isStartTime) {
                String timeString = "Start of cycle: " + iHours + ":" + iMinutes;
                isStartTime = false;
                displayCyclesString.append(timeString).append("\n");
                startInt = i;
            } else {
                String timeString = "End of cycle: " + iHours + ":" + iMinutes;
                isStartTime = true;
                displayCyclesString.append(timeString).append("\n");
                totalRem += i - startInt;
            }
        }
        return displayCyclesString.toString();
    }


//    private File getFile(String filename) {
//        File[] filesArr = context.getFilesDir().listFiles();
//        for (File f : filesArr) {
//            if (f.getName() == filename) {
//
//                return f;
//            }
//        }
//        return null;
//    }

}


