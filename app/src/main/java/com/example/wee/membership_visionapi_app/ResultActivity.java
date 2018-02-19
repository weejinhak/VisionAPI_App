package com.example.wee.membership_visionapi_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wee.membership_visionapi_app.Adapter.ComponentListAdapter;
import com.example.wee.membership_visionapi_app.Handler.BackPressCloseHandler;
import com.example.wee.membership_visionapi_app.Models.Component;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class ResultActivity extends AppCompatActivity implements ValueEventListener {
    private static final String TAG = ResultActivity.class.getSimpleName();

    private BackPressCloseHandler backPressCloseHandler;
    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> componentList = new ArrayList<>();
    private ArrayList<String> allergyList = new ArrayList<>();

    private ImageView photoImage;
    private ListView mListView;
    private ComponentListAdapter mAdapter;
    private Context mContext = ResultActivity.this;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        backPressCloseHandler = new BackPressCloseHandler(this);


        Intent intent = getIntent();
        Uri photoUri = intent.getParcelableExtra("PhotoURI");
        allergyList = intent.getStringArrayListExtra("allergies");
        Log.d(TAG, allergyList.toString());
        photoImage = findViewById(R.id.item_image);
        mListView = findViewById(R.id.component_listView);

        //광고삽입
        AdView mAdView = findViewById(R.id.adView);
        /*divice Id */
        Log.d("Test_Device_Id", AdRequest.DEVICE_ID_EMULATOR);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        mAdapter = new ComponentListAdapter(this, 0);

        mListView.setAdapter(mAdapter);

        setupFirebaseAuth();
        initToolbar();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        try {
            Bitmap bitmap = MainActivity.scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri), 1200);
            photoImage.setImageBitmap(bitmap);

        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }

    }

    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("SNACKpick");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return true;
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
            Log.d(TAG, "fileSnapshot: " + fileSnapshot);
            componentList.add((String) fileSnapshot.getValue());
        }

        // 사용자가 정한 유해성분만 리스트뷰에 추가
        for (int j = 0; j < allergyList.size(); j++) {
            for (int i = 0; i < componentList.size(); i++) {
                Log.d(TAG, allergyList.get(j) + " ? " + componentList.get(i));

                if (componentList.get(i).contains(allergyList.get(j))) {
                    Log.d(TAG, "allergy found!! - " + allergyList.get(j));
                    Component component = new Component(componentList.get(i), TRUE);
                    mAdapter.add(component);
                    mListView.smoothScrollToPosition(mAdapter.getCount());
                    componentList.remove(i);
                }
            }
        }

        // 나머지 성분 리스트뷰에 추가
        for (int i = 0; i < componentList.size(); i++) {
            Component component = new Component(componentList.get(i), FALSE);
            mAdapter.add(component);
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
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    /**
     * Setup the firebase auth object
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void signOut() {
        // Firebase sign out
        LoginManager.getInstance().logOut();
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });

    }
}
