package com.example.wee.membership_visionapi_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


import com.example.wee.membership_visionapi_app.Handler.BackPressCloseHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wee on 2018. 2. 12..
 */

public class ResultActivity extends AppCompatActivity implements ValueEventListener {

    private BackPressCloseHandler backPressCloseHandler;
    private ArrayList<String> resultList;
    private ArrayList<String> componentList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        backPressCloseHandler = new BackPressCloseHandler(this);


    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        dataSnapshot.getValue();

        //get visionApi result
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");

        resultList = resultParsingString(result);

        for (int i = 0; i < resultList.size(); i++) {
            System.out.println(resultList.get(i));
        }

        //get Database key and value
        ArrayList<String> keyList =new ArrayList<>();
        for (DataSnapshot fileSnapshot : dataSnapshot.child("SNACK").getChildren()) {
            keyList.add(fileSnapshot.getKey());
        }

        String keyString="";
        for(int i=0; i< keyList.size();i++){
            for(int j=0; j<resultList.size();j++){
                if(keyList.get(i).equals(resultList.get(j))){
                    keyString=keyList.get(i);
                    break;
                }
            }
        }

        for(DataSnapshot fileSnapshot : dataSnapshot.child("SNACK").child(keyString).getChildren()){
            componentList.add((String) fileSnapshot.getValue());
        }

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
}
