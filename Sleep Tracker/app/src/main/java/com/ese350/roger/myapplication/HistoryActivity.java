package com.ese350.roger.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class HistoryActivity extends AppCompatActivity {

    File[] filesArr;
    Context appContext;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appContext = getApplicationContext();
        super.onCreate(savedInstanceState);
        setupPage();
    }

    private void setupPage() {
        setContentView(R.layout.activity_history);
        context = HistoryActivity.this;
        filesArr = appContext.getFilesDir().listFiles();

        final ArrayList<String> fileNamesArray = new ArrayList<String>();
        for (File f : filesArr) {
            fileNamesArray.add(f.getName());
        }
        TextView titleText = (TextView) findViewById(R.id.titleView);

        if (filesArr.length == 0) {
            titleText.setText("No documents found");
        } else {
//            titleText.setText(filesArr[0].getName() + "," + filesArr[1].getName() + "," + filesArr[0].getPath());
            titleText.setText("History");
        }

        ListView listView = (ListView) findViewById(R.id.historyListView);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, fileNamesArray);
        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = fileNamesArray.get(position);
                Intent intent = new Intent(HistoryActivity.this, InformationActivity.class);
                intent.putExtra("EXTRA_FILENAME", fileName);
                intent.putExtra("EXTRA_ARRINDEX", position);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupPage();
    }
}
