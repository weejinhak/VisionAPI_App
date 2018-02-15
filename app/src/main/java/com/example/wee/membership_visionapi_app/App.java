package com.example.wee.membership_visionapi_app;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by wee on 2018. 2. 13..
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this,"fonts/NanumBarunpenR.otf"))
                .addBold(Typekit.createFromAsset(this,"fonts/NanumBarunpenB.otf"));
    }
}
