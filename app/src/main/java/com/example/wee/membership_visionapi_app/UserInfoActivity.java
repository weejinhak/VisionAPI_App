package com.example.wee.membership_visionapi_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

/**
 * Created by wee on 2018. 2. 9..
 */

public class UserInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        ImageButton allergy_addButton = findViewById(R.id.allergy_add_button);

    }
}
