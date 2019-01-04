package com.example.q.project2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryPreview extends AppCompatActivity {
    ImageView GalleryPreviewing;
    String path;

    //might have to change to onCreateView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Might have to delete this "hide" line
        getSupportActionBar().hide();

        setContentView(R.layout.gallery_preview);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        GalleryPreviewing = (ImageView) findViewById(R.id.GalleryPreviewImg);
        Glide.with(GalleryPreview.this).load(new File(path)).into(GalleryPreviewing);
    }
}
