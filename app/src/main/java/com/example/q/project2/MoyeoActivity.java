package com.example.q.project2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MoyeoActivity extends AppCompatActivity {

    public ArrayList<MoyeoItem> nameList = new ArrayList<MoyeoItem>();

    static MoyeoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moyeo);

        Intent intent = getIntent();
        ArrayList<String> newnames = intent.getStringArrayListExtra("names");

        if (newnames != null ){
            for (int i = 0; i < newnames.size(); i++) {
                MoyeoItem newItem = new MoyeoItem(newnames.get(i));
                nameList.add(newItem);
            }
        }
//
//        Bundle names = getIntent().getExtras();
//        Log.d("dqj", String.valueOf(names));

        final GridView inviteView = (GridView) findViewById(R.id.moyeoList);

        ImageButton refreshButt = findViewById(R.id.refreshButton);
        refreshButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread refreshThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://socrip4.kaist.ac.kr:2080/moyeo/refresh");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);

                            InputStream inputStream = connection.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                            JSONArray replyArray = new JSONArray(bufferedReader.readLine());
                            for (int i = 0; i < replyArray.length(); i++) {
                                JSONObject replyObject = (JSONObject) replyArray.get(i);
                                String replyName = (String) replyObject.get("name");
                                MoyeoItem item = new MoyeoItem(replyName);
                                nameList.add(item);
                                Log.d("fqew", replyName);
                            }


//                            adapter.notifyDataSetChanged();
//                            inviteView.setAdapter(adapter);

                            finish();
                            Intent intent = new Intent(getBaseContext(), MoyeoActivity.class);
                            intent.putExtra("names", nameList);
                            startActivity(intent);

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    inviteView.setAdapter(adapter);
//                                }
//                            });

//                            MoyeoAdapter adapter = new MoyeoAdapter(getBaseContext(), R.layout.moyeo_item, nameList);
//                            inviteView.setAdapter(adapter);

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                refreshThread.start();
            }
        });

        adapter = new MoyeoAdapter(this, R.layout.moyeo_item, nameList);
        inviteView.setAdapter(adapter);
    }
}
