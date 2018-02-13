package com.example.wee.membership_visionapi_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;
import java.util.Locale;

/**
 * Created by wee on 2018. 2. 12..
 */

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        Bundle result= intent.getBundleExtra("list");


        Log.d("tltltltl", String.valueOf(result));

    }


}
