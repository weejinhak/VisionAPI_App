package com.example.wee.membership_visionapi_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.wee.membership_visionapi_app.Handler.BackPressCloseHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wee on 2018. 2. 12..
 */

public class ResultActivity extends AppCompatActivity implements ValueEventListener {
    private static final String TAG = ResultActivity.class.getSimpleName();
    private BackPressCloseHandler backPressCloseHandler;
    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> componentList = new ArrayList<>();
    private ArrayList<String> allergyList = new ArrayList<>();

    private ImageView photoImage;
    private TextView componentTextview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        backPressCloseHandler = new BackPressCloseHandler(this);

        Intent intent = getIntent();
        Uri photoUri = intent.getParcelableExtra("PhotoURI");
        allergyList = intent.getStringArrayListExtra("allergies");
        Log.d(TAG, allergyList.toString());
        photoImage = findViewById(R.id.main_image);
        componentTextview = findViewById(R.id.componentList_textView);

        try {
            Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri), 1200);
            photoImage.setImageBitmap(bitmap);

        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        dataSnapshot.getValue();

        //get visionApi result
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");

        resultList = resultParsingString(result);

        //get Database key and value
        ArrayList<String> keyList = new ArrayList<>();
        for (DataSnapshot fileSnapshot : dataSnapshot.child("SNACK").getChildren()) {
            keyList.add(fileSnapshot.getKey());
        }

        String keyString = "";
        for (int i = 0; i < keyList.size(); i++) {
            for (int j = 0; j < resultList.size(); j++) {
                if (keyList.get(i).equals(resultList.get(j))) {
                    keyString = keyList.get(i);
                    break;
                }
            }
        }
        //새로운 방안 키값을 일일이 집어넣음
        for (DataSnapshot fileSnapshot : dataSnapshot.child("SNACK").child(keyString).getChildren()) {
            componentList.add((String) fileSnapshot.getValue());
        }


        String strComponents = "구성성분: \n\n";
        for (int i = 0; i < componentList.size(); i++) {
            strComponents += componentList.get(i);
            strComponents += "\n";
        }
        componentTextview.setText(strComponents);


    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference().removeEventListener(this);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    private ArrayList<String> resultParsingString(String response) {

        ArrayList<String> resultList = new ArrayList<>();
        String[] result = response.split("[\r\n\t]");
        Collections.addAll(resultList, result);

        return resultList;
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
