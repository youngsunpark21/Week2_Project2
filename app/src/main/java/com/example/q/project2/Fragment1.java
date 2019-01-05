package com.example.q.project2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class Fragment1 extends Fragment {
    public Fragment1() {
        //Required empty public constructor
    }

    public ListView mListview;
    private ContactListViewAdapter adapter;
    static final int REQUEST_PERMISSION_KEY = 2051;
    ArrayList<ContactItem> StoreContacts;
    View view;
    LayoutInflater inflater;
    ViewGroup container;
    Bundle savedInstanceState;


    @Override
    public View onCreateView(LayoutInflater inflater1, final ViewGroup container1, Bundle savedInstanceState1) {
        inflater = inflater1;
        container = container1;
        savedInstanceState = savedInstanceState1;
        view = inflater.inflate(R.layout.fragmentlayout1, container, false);

        //permission 채크하기
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {
            } else {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1);
            }
        }

        //핸드폰에 있는 연락처 불러오기
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mListview = (ListView) view.findViewById(R.id.contactList);
            adapter = new ContactListViewAdapter(getActivity());

            String[] proj = {
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.DATA

            };

            Uri uriBlank = Uri.parse("android.resource://com.example.q.myapplication/drawable/bob");

            Cursor c = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null
                    , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc");

            if (c.moveToFirst()) {
                //toast text
                Toast.makeText(getActivity(), "데이터를 불러왔습니다", Toast.LENGTH_LONG).show();
                do {
                    String uri = c.getString(0);
                    String name = c.getString(1);
                    String number = c.getString(2);
                    if (uri != null) {
                        adapter.addItem(Uri.parse(uri), name, number);
                    } else {
                        adapter.addItem(uriBlank, name, number);
                    }
                } while (c.moveToNext());
            }
            c.close();

            mListview.setAdapter(adapter);

            //연락처 보내기
            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.frag1_fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("fab", "CLICKED");

                    //Json 되기
                    //ArrayList<ContactItem> data = new ArrayList<>();
                    int icon = R.drawable.blank;

                    final String[] proj = {
                            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.DATA

                    };

                    Cursor c = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null
                            , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc");

                    final JSONArray jsonArray = new JSONArray();

                    while (c.moveToNext()) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name", c.getString(1));
                            jsonObject.put("phonenum", c.getString(2));

                            jsonArray.put(jsonObject);

                            Log.d("DATA", String.valueOf(jsonArray));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //지우기
                    Thread deleteContacts = new Thread() {
                        @Override
                        public void run(){
                            try {
                                URL url = null;
                                url = new URL("http://socrip4.kaist.ac.kr:2080/contact/backup/delete");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("DELETE");

                                Log.d("BACKDELETE", String.valueOf(connection.getResponseCode()));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    deleteContacts.start();

                    Thread backupContacts = new Thread() {
                        @Override
                        public void run() {
                            //보내주기 using thread
                            try {
                                URL url = null;
                                url = new URL("http://socrip4.kaist.ac.kr:2080/contact/backup/get");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                connection.setRequestProperty("Accept","application/json");
                                connection.setDoOutput(true);
                                //connection.setDoInput(true);

                                //connection.connect();

                                Log.d("CONTACTRETURN", String.valueOf(connection.getResponseCode()));

                                OutputStream os = connection.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                writer.write(jsonArray.toString());
                                writer.flush();
                                writer.close();
                                os.close();


                                Log.d("CONTACTRETURN1", String.valueOf(connection.getResponseCode()));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (ProtocolException e) {
                                e.printStackTrace();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    backupContacts.start();
                }
            });
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCreateView(inflater, container, savedInstanceState);
        }

    }

}
