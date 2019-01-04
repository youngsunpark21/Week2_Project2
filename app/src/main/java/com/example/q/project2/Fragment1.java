package com.example.q.project2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class Fragment1 extends Fragment {
    public Fragment1() {
        //Required empty public constructor
    }

    public ArrayList<Map<String, String>> dataList;
    public ListView mListview;
    private ContactListViewAdapter adapter;
    static final int REQUEST_PERMISSION_KEY = 2051;
    ArrayList<String> StoreContacts;
    View view;
    LayoutInflater inflater;
    ViewGroup container;
    Bundle savedInstanceState;


    @Override
    public View onCreateView(LayoutInflater inflater1, ViewGroup container1, Bundle savedInstanceState1) {
        inflater = inflater1;
        container = container1;
        savedInstanceState = savedInstanceState1;
        view = inflater.inflate(R.layout.fragmentlayout1, container, false);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

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
