package com.snackpick.wee.membership_visionapi_app;

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
                .addNormal(Typekit.createFromAsset(this,"fonts/NanumBarunGothic.ttf"))
                .addBold(Typekit.createFromAsset(this,"fonts/NanumBarunGothicBold.ttf"));
    }
}
