package com.ese350.roger.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class InformationActivity extends AppCompatActivity {

    File[] filesArr;
    Context appContext;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getApplicationContext();
        context = InformationActivity.this;
        setContentView(R.layout.activity_information);
        Intent intent = getIntent();

        String filename = intent.getStringExtra("EXTRA_FILE");
        int index = intent.getIntExtra("EXTRA_ARRINDEX", 0);
        filesArr = appContext.getFilesDir().listFiles();
        final File file = filesArr[index];

        TextView fileText = (TextView) findViewById(R.id.fileText);
        TextView logText = (TextView) findViewById(R.id.logText);
        fileText.setText(file.getName());

        Date startTimeDate = new Date();
        String startTime = "";
        String endTime = "";

        ArrayList<DataPoint> movementDataArrayList = new ArrayList<>();
        ArrayList<DataPoint> heartrateDataArrayList = new ArrayList<>();
        ArrayList<Double> heartrateDataDoubleArrayList = new ArrayList<>();

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
                if (infoArray.length > 200) {
                    if (i % 2 == 1 && i != infoArray.length - 2) {
                        continue;
                    }
                }
                String[] lineArray = infoArray[i].split(",");
                if (lineArray.length > 4) {
                    movementDataArrayList.add(new DataPoint(i, Double.valueOf(lineArray[3])));
                    heartrateDataArrayList.add(new DataPoint(i, Double.valueOf(lineArray[4])));
                    heartrateDataDoubleArrayList.add(Double.valueOf(lineArray[4]));

                    if (i == 0) {
                        String startTimeDateString = lineArray[1];
                        DateFormat format = new SimpleDateFormat("dd MMM yyyy HH:MM:SS +0000", Locale.ENGLISH);
                        startTimeDate = format.parse(startTimeDateString);
                        startTime = lineArray[1].split(" ")[4];
                    } else if (i == infoArray.length - 2) {
                        endTime = lineArray[1].split(" ")[4];
                    }
                }
            }

            if (infoArray.length > 90) {
                ArrayList<Integer> remCycleTimes = smartsleep.getPeak(heartrateDataDoubleArrayList);
                Boolean isStartTime = true;
                StringBuilder displayCyclesString = new StringBuilder("");
                Long startTimeLong = startTimeDate.getTime();
                int startInt = 0;
                int totalRem = 0;
                for (Integer i : remCycleTimes) {
                    Long cycleTime = startTimeLong + i * 60000;
                    Date cycleDate = new Date(cycleTime);
                    if (isStartTime) {
                        String timeString = "Start of cycle: " + new SimpleDateFormat("HH:mm:ss")
                                .format(cycleDate);
                        isStartTime = false;
                        displayCyclesString.append(timeString).append("\n");
                        startInt = i;
                    } else {
                        String timeString = "End of cycle: " + new SimpleDateFormat("HH:mm:ss")
                                .format(cycleDate);
                        isStartTime = true;
                        displayCyclesString.append(timeString).append("\n");
                        totalRem += i - startInt;
                    }
                }
                logText.setText("Total REM : " + String.valueOf(totalRem) + "\n" + displayCyclesString);
            }

            reader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileText.setText("Unable to read:" + e.toString());
        }
        DataPoint[] movementDataArray = movementDataArrayList.toArray(new DataPoint[movementDataArrayList.size()]);
        DataPoint[] heartrateDataArray = heartrateDataArrayList.toArray(new DataPoint[movementDataArrayList.size()]);
//
        GraphView heartrateGraph = (GraphView) findViewById(R.id.heartrate_graph);
        GraphView movementGraph = (GraphView) findViewById(R.id.movement_graph);

        heartrateGraph.setTitle("Average Heartrate");
        movementGraph.setTitle("Movement");
//        movementGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));
//        heartrateGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));
//
        StaticLabelsFormatter heartrateLabels = new StaticLabelsFormatter(heartrateGraph);
        StaticLabelsFormatter movementLabels = new StaticLabelsFormatter(movementGraph);

        heartrateLabels.setHorizontalLabels(new String[]{startTime, endTime});
        movementLabels.setHorizontalLabels(new String[]{startTime, endTime});
        movementLabels.setVerticalLabels(new String[]{"",""});

        heartrateGraph.getGridLabelRenderer().setLabelFormatter(heartrateLabels);
        movementGraph.getGridLabelRenderer().setLabelFormatter(movementLabels);

        LineGraphSeries<DataPoint> movementSeries = new LineGraphSeries<>(movementDataArray);
        LineGraphSeries<DataPoint> heartrateSeries = new LineGraphSeries<>(heartrateDataArray);
//
        heartrateGraph.addSeries(heartrateSeries);
        movementGraph.addSeries(movementSeries);


        final Button deleteButton = (Button) findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    context.deleteFile(file.getName());
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private FileInputStream getFileReader(String filename) {
        try {
            FileInputStream fileInputStream = openFileInput(filename);
            return fileInputStream;
        } catch (Exception e) {
            return null;
        }
    }
}
