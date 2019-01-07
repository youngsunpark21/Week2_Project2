package com.example.q.project2;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

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

            //fab buttons
            FabSpeedDial fab = (FabSpeedDial) view.findViewById(R.id.frag1_fab);
            fab.setMenuListener(new SimpleMenuListenerAdapter(){
                @Override
                public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                    // TODO: Do something with yout menu items, or return false if you don't want to show them
                    return true;
                }
                @Override
                public boolean onMenuItemSelected(MenuItem menuItem) {

                    //버튼 액션들
                    switch (menuItem.getItemId()) {
                        case R.id.action_backup:
                            //1번 백업하기
                            //1-1Json 되기
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

                            //1-2 지우기
                            Thread deleteContacts = new Thread() {
                                @Override
                                public void run(){
                                    try {
                                        URL url = null;
                                        url = new URL("http://socrip4.kaist.ac.kr:2080/contact/backup/delete");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("DELETE");
                                        connection.setDoInput(true);

                                        InputStream inputStream = connection.getInputStream();
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        bufferedReader.close();
                                        Log.d("BACKDELETE",bufferedReader.readLine());

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
                                    //1-3 보내주기 using thread
                                    try {
                                        URL url = null;
                                        url = new URL("http://socrip4.kaist.ac.kr:2080/contact/backup/post");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("POST");
                                        connection.setDoOutput(true);
                                        connection.setDoInput(true);
                                        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                        connection.setRequestProperty("Accept","application/json");

                                        OutputStream os = connection.getOutputStream();
                                        os.write(jsonArray.toString().getBytes("UTF-8"));
                                        os.close();

                                        Log.d("backupwhat", jsonArray.toString());

                                        InputStream inputStream = connection.getInputStream();
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        Log.d("CONTACTRETURN2",bufferedReader.readLine());

                                        Log.d("CONTACTBACK1",String.valueOf(connection.getResponseCode()));

                                    } catch (Exception e) {
                                        Log.d("CONTACTRETURN5",e.getMessage());
                                    }

                                }
                            };
                            backupContacts.start();
                            Toast.makeText(getContext(), "백업 되었습니다", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_delete:
                            //2번 전체삭제
                            //2-1 정보 가져와서 지우기
                            getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, null, null);
                            //2-2 TODO:fragment 새로 고침
                            /* 이거 없어도 될수도
                            Thread deleteThread = new Thread() {
                                public void run() {
                                    try{
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mListview.setAdapter(adapter);
                                            }
                                        });
                                    } catch (Exception e) {

                                    }
                                }
                            };
                            deleteThread.start();*/
                            Toast.makeText(getContext(), "전체삭제 되었습니다", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_sync:
                            //3번 동기화 하기
                            //3-1 thread 만들기 - 연락처 가져오기
                            Thread contactThread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = new URL("http://socrip4.kaist.ac.kr:2080/contact/backup/get");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("GET");
                                        connection.setDoInput(true);

                                        connection.setConnectTimeout(10000);
                                        connection.setReadTimeout(10000);
                                        InputStream inputStream = connection.getInputStream();
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        Log.d("TESTDATA", String.valueOf(connection.getResponseCode()));
                                        //Log.d("TESTDATA",bufferedReader.readLine());

                                        //3-2 response 읽고 jsonarray만들기
                                        JSONArray inputArray = new JSONArray(bufferedReader.readLine());

                                        //3-3 jsonarray 하나씩 보며 저장하기
                                        for (int i = 0; i < inputArray.length(); i++) {
                                            JSONObject inputItem = (JSONObject) inputArray.get(i);
                                            String inputName = (String) inputItem.get("name");
                                            String inputNum = (String) inputItem.get("phonenum");
                                            ArrayList <ContentProviderOperation> list = new ArrayList < ContentProviderOperation > ();
                                            list.add(
                                                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                                            .build()
                                            );

                                            list.add(
                                                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, inputName)   //이름

                                                            .build()
                                            );

                                            list.add(
                                                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, inputNum)           //전화번호
                                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE  , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)   //번호타입(Type_Mobile : 모바일)

                                                            .build()
                                            );
                                            getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list);  //주소록추가
                                            list.clear();   //리스트 초기화
                                            Uri uri = Uri.parse("android.resource://com.example.q.myapplication/drawable/bob");
                                            adapter.addItem(uri, inputName, inputNum);
                                            //Log.d("jsonitem", String.valueOf(inputArray.get(i)));
                                        }

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mListview.setAdapter(adapter);
                                            }
                                        });

                                        Log.d("DATAITEM", String.valueOf(inputArray));
                                    }
                                    catch (Exception e) {
                                        Log.d("TESTDATA", e.getMessage());
                                    }
                                }
                            };
                            contactThread.start();
                            Toast.makeText(getContext(), "동기화 되었습니다", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
                @Override
                public void onMenuClosed() {

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
