package com.dev.thrwat_zidan.la_bla_bla;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewer extends AppCompatActivity {
    private ImageView image_viewer;
    private String imgURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        image_viewer=findViewById(R.id.image_viewer);

        imgURL = getIntent().getStringExtra("url");

        Picasso.get().load(imgURL).into(image_viewer);
    }
}
