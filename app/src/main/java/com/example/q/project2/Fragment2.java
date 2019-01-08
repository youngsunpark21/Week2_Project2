package com.example.q.project2;

import android.Manifest;
import android.app.Activity;
import android.app.usage.ExternalStorageStats;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.os.Environment;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
                            Log.d("papapaapapa", path);

////-------------------------------------------------
                                Bitmap image;
                                image = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path),50,50,true);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] imageBytes = baos.toByteArray();
                                String imageString = Base64.encodeToString(imageBytes,Base64.DEFAULT);
////----------------------------------------------------
                            /*InputStream inputStream = new FileInputStream(path);
                                byte[] bytes;
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                bytes = output.toByteArray();
                                final String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);*/
                                outputObject.put("image", imageString);

                            Log.d("encodewhatt", imageString);

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
                        Log.d("dhquiwehquw", "dhqowdiqo");
                        final String url = "http://socrip4.kaist.ac.kr:2080/gallery/backup/get";
                        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("eewewfkl", "entered!");
                                    Log.d("oqoqoqoq", String.valueOf(response));
                                    String responseString = response.getString("result");
                                    Log.d("ppwpwp", responseString);
                                    String[] stringAll = responseString.split("image:");
                                    String part1 = stringAll[1];
                                    String part2 = stringAll[2];
                                    String[] strings1 = part1.split(",");
                                    String[] strings2 = part2.split(",");
                                    String part3 = strings1[0];
                                    String part4 = strings2[0];
                                    String[] lastpart1 = part3.split("'");
                                    String[] lastpart2 = part4.split("'");
                                    String last1 = lastpart1[1];
                                    String last2 = lastpart2[1];
                                    Log.d("deeqw", last1);
                                    Log.d("qoweji", last2);

                                    String test1 = "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\n";
                                    String test2 = "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\naNGRnH//2Q==\n";

                                    String[] last11 = test1.split("\\r?\\n+");
                                    StringBuilder encodedSB1 = new StringBuilder();
                                    for (String s: last11) {
                                        encodedSB1.append(s);
                                    }
                                    String encodedString1 = encodedSB1.toString();
                                    Log.d("qiwdhqjk", encodedString1);

                                    String[] last22 = test2.split("\\r?\\n+");
                                    StringBuilder encodedSB2 = new StringBuilder();
                                    for (String s : last22) {
                                        encodedSB2.append(s);
                                    }
                                    String encodedString2 = encodedSB2.toString();
                                    Log.d("djqio", encodedString2);


                                    byte[] decodedString1 = Base64.decode(encodedString1, Base64.DEFAULT);
                                    Bitmap decodedImage1 = BitmapFactory.decodeByteArray(decodedString1, 0, decodedString1.length);


                                    String imageSaveUri = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), decodedImage1, "사진 저장", "저장되었습니다");
                                    Uri uri = Uri.parse(imageSaveUri);
                                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

                                    Toast.makeText(getContext(), "동기화 되었습니다", Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("errorerror!", String.valueOf(error));
                                Log.d("errorerror!", "애러가 났어용");
                            }
                        });
                        requestQueue.add(jsonObjectRequest);

                        /*Thread requestNum = new Thread() {
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
                        }*/


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

    /**
     * Image SDCard Save (input Bitmap -> saved file JPEG)
     * Writer intruder(Kwangseob Kim)
     * @param bitmap : input bitmap file
     * @param folder : input folder name
     * @param name   : output file name
     */
    public static void saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){
        Log.d("dfqehuieywu", "here!!");
        String ex_storage =Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
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

                //imageList.add(Function.mappingInbox(null, path, timestamp, Function.convertToTime(timestamp), null));
                imageList.add(Function.mappingInbox(null, path, timestamp, null, null));
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

