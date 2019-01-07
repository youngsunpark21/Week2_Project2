package com.example.q.project2;

import android.Manifest;
import android.app.Activity;
import android.app.usage.ExternalStorageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class Fragment2 extends Fragment {
    static final int REQUEST_PERMISSION_KEY = 5245;
    LoadAlbumImages loadAlbumTask;
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> imageList = new ArrayList<>();
    int numInt = 0;

    public Fragment2() {
        //Required empty public constructor
    }

    public interface OneTimeData {
        void oneTimeData(String a);
    }

    OneTimeData oneTimeDataItem;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        oneTimeDataItem = (OneTimeData) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragmentlayout2, container, false);

        galleryGridView = (GridView) view.findViewById(R.id.galleryGridView);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;

        Resources resources = getActivity().getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getActivity().getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

        FabSpeedDial fab = (FabSpeedDial) view.findViewById(R.id.frag2_fab);
        fab.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_backup2:
                        //1번 백업하기
                        //1-3 데이터 베이스 다 지우기
                        Thread deleteImages = new Thread() {
                            @Override
                            public void run(){
                                try {
                                    URL url = null;
                                    url = new URL("http://socrip4.kaist.ac.kr:2080/gallery/backup/delete");
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("DELETE");
                                    connection.setDoInput(true);

                                    InputStream inputStream = connection.getInputStream();
                                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                    bufferedReader.close();
                                    Log.d("deleteimagee",bufferedReader.readLine());

                                    Log.d("deleteimagee", String.valueOf(connection.getResponseCode()));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        deleteImages.start();
                        //1-1 Bas64로 encoding 하기
                        //1-2 json 되기
                        //final JSONArray outputList = new JSONArray();
                        String path = null;
                        String timestamp = null;

                        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

                        final String[] projection = {MediaStore.MediaColumns.DATA,
                                MediaStore.MediaColumns.DATE_MODIFIED};

                        Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection, null, null, null);
                        Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection, null, null, null);
                        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

                        int times = 0;

                        while (cursor.moveToNext()) {
                            try {
                            final JSONObject outputObject = new JSONObject();
                            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));

                            InputStream inputStream = new FileInputStream(path);
                                byte[] bytes;
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                bytes = output.toByteArray();
                                final String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
                                outputObject.put("image", encodedString);

                            Log.d("encodewhatt", encodedString);

                            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                            outputObject.put("timestamp", Function.convertToTime(timestamp));

                            //0면 못나감
                            final int exitLoop = 0;

                            //보내기
                            Thread backupImages = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        URL url = null;
                                        url = new URL("http://socrip4.kaist.ac.kr:2080/gallery/backup/post");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("POST");
                                        connection.setDoOutput(true);
                                        connection.setDoInput(true);
                                        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                        connection.setRequestProperty("Accept", "application/json");

                                        OutputStream os = connection.getOutputStream();
                                        os.write(outputObject.toString().getBytes("UTF-8"));
                                        os.close();

                                        Log.d("whatoutput", String.valueOf(outputObject.toString().getBytes("UTF-8")));

                                        InputStream inputStream = connection.getInputStream();
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        Log.d("gallreturn", bufferedReader.readLine());

                                        Log.d("gallreturn", String.valueOf(connection.getResponseCode()));
                                    } catch (Exception e) {
                                            Log.d("gallreturn2",e.getMessage());
                                    }

                                    }
                                };
                                backupImages.start();
                                backupImages.join(2000 + times*2000);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            times +=1;
                        }
                        cursor.close();

                        Toast.makeText(getContext(), "백업 되었습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_delete2:
                        getContext().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null);
                        getContext().getContentResolver().delete(MediaStore.Images.Media.INTERNAL_CONTENT_URI, null, null);
                        Toast.makeText(getContext(), "전체삭제 되었습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_sync2:
                        Thread requestNum = new Thread() {
                            @Override
                            public void run(){
                                try{
                                    URL url = new URL("http://socrip4.kaist.ac.kr:2080/gallery/backup/getnum");
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.setDoInput(true);

                                    InputStream inputStream = connection.getInputStream();
                                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                                    StringBuilder sb = new StringBuilder();
                                    sb.append(bufferedReader.readLine());

                                    Log.d("djqwlqj", String.valueOf(sb));

                                    JSONObject obj = new JSONObject(String.valueOf(sb));
                                    String getINput = (String) obj.get("number");
                                    numInt = Integer.parseInt(getINput);

                                    Log.d("dqwqwdq", getINput);

                                    Log.d("getgallerynum", String.valueOf(connection.getResponseCode()));
                                }catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        requestNum.start();

                        Thread getImages = new Thread(){
                            @Override
                            public void run(){
                                try {
                                    Log.d("dqwdqwdwqdwq", "run!!");
                                    URL url = new URL("http://socrip4.kaist.ac.kr:2080/gallery/backup/getone");
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.setDoInput(true);

                                    InputStream inputStream = connection.getInputStream();
                                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                                    StringBuilder sb = new StringBuilder();
                                    sb.append(bufferedReader.readLine());

                                    Log.d("hahaha", String.valueOf(sb));

                                    JSONObject obj = new JSONObject(String.valueOf(sb));
                                    String dataPath = (String) obj.get("image");
                                    //TODO: timestamp 저장하는 방법 생각해보기
                                    String dataTime = (String) obj.get("timestamp");

                                    byte[] decodedString = Base64.decode(dataPath, Base64.DEFAULT);
                                    Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    String imageSaveUri = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), decodedImage, "사진 저장", "저장되었습니다");
                                    Uri uri = Uri.parse(imageSaveUri);
                                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

                                    Log.d("qqqkqkqk", String.valueOf(connection.getResponseCode()));
                                } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            };

                        while (numInt != 0) {
                            Log.d("eqeqwqe", "Loop!!");
                            getImages.start();
                            try {
                                getImages.join(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            numInt -=1;
                        }

                        Toast.makeText(getContext(), "동기화 되었습니다", Toast.LENGTH_SHORT).show();
                        break;
                }
                //Toast.makeText(getContext(), ""+menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override
            public void onMenuClosed() {

            }
        });

        return view;
    }

    class LoadAlbumImages extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
            Log.d("onPreExecute","OK");
        }

        @Override
        protected String doInBackground(String... strings) {
            String xml = "";

            String path = null;
            String timestamp = null;
            Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;


            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED};

            Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection, null, null, null);
            Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection, null, null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Function.mappingInbox(null, path, timestamp, Function.convertToTime(timestamp), null));
            }

            cursor.close();
            Collections.sort(imageList, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));
            Log.d("doInBackground","OK");
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            SingleAlbumAdapter adapter = new SingleAlbumAdapter(getActivity(), imageList);
            galleryGridView.setAdapter(adapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    Toast.makeText(getContext(),"호락호락하게 사진이 커지진 않지!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getActivity(), GalleryPreview.class);
//                    intent.putExtra("path", imageList.get(+position).get(Function.KEY_PATH));
//                    startActivity(intent);
                    //oneTimeDataItem.oneTimeData(imageList.get(+position).get(Function.KEY_PATH));
                }
            });
        }

    }

}

class SingleAlbumAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public SingleAlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
        activity = a;
        data = d;
    }
    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SingleAlbumViewHolder holder = null;
        if (convertView == null) {
            holder = new SingleAlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.single_album_row, parent, false);

            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);

            convertView.setTag(holder);
        } else {
            holder = (SingleAlbumViewHolder) convertView.getTag();
        }
        holder.galleryImage.setId(position);

        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {

            Glide.with(activity)
                    .load(new File(song.get(Function.KEY_PATH))) // Uri of the picture
                    .into(holder.galleryImage);

        } catch (Exception e) {}

        Log.d("getView","OK");
        return convertView;
    }
}

class SingleAlbumViewHolder {
    ImageView galleryImage;
}