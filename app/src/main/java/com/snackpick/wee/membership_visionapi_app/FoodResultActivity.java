package com.snackpick.wee.membership_visionapi_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.snackpick.wee.membership_visionapi_app.Models.AllergyIngredient;
import com.snackpick.wee.membership_visionapi_app.Models.Food;
import com.snackpick.wee.membership_visionapi_app.Models.FoodMaterial;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoodResultActivity extends BaseActivity {
    private static final String TAG = FoodResultActivity.class.getName();
    private Context mContext = FoodResultActivity.this;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mUserReference;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    private Food food;
    private ImageView foodThumbnail,faceImoticon;
    private TextView foodName_tv,myAllergyStr_tv;
    private TextView allergyResult_tv1,allergyResult_tv2;
    private FlowLayout mAllergyFlowTagLayout,mMaterialFlowTagLayout,mTagFlowTagLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_result);

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        food = (Food)getIntent().getSerializableExtra("food-result");

        foodThumbnail=(ImageView)findViewById(R.id.foodThumbnail);
        foodName_tv = (TextView)findViewById(R.id.foodName_tv);
        allergyResult_tv1=(TextView)findViewById(R.id.allergyResult_tv1);
        allergyResult_tv2=(TextView)findViewById(R.id.allergyResult_tv2);
        faceImoticon = (ImageView)findViewById(R.id.face_imoticon_imageView);
        myAllergyStr_tv =(TextView)findViewById(R.id.my_allergy_str_tv);

        initToolbar();
        initGoogleSign();
        setupFirebaseAuth();

        foodName_tv.setText(food.getFoodName());
        Picasso.with(this).load(food.getThumbnailUrl()).into(foodThumbnail);//실패이미지 나중에

        int count = food.getCount();
        if(count==0){
            faceImoticon.setImageResource(R.drawable.happy);
            allergyResult_tv1.setText(userName+"님\n 안전해요.");
            myAllergyStr_tv.setText("");
        }else if(count>0){
            faceImoticon.setImageResource(R.drawable.sad);
            allergyResult_tv1.setText(userName+"님\n 조심하세요!");
            String str = food.getMyAllergyStr();
            Log.i("@@@@",str);
            str = food.getMyAllergyStr().substring(0,food.getMyAllergyStr().lastIndexOf(','));
            Log.i("@@@@",str);
            myAllergyStr_tv.setText("( "+str+" )");
        }
        allergyResult_tv2.setText(count+"개");


        mAllergyFlowTagLayout = findViewById(R.id.allergy_flow_layout);
        mMaterialFlowTagLayout = findViewById(R.id.material_flow_layout);
        mTagFlowTagLayout = findViewById(R.id.tag_flow_layout);


        List<AllergyIngredient> allergyIngredientList = food.getAllergyIngredients();
        if(!allergyIngredientList.isEmpty()) {
            for (int i = 0; i < allergyIngredientList.size(); i++) {
                AllergyIngredient a = allergyIngredientList.get(i);

                View view = this.getLayoutInflater().inflate(R.layout.tag_item, null);
                TextView textView = (TextView) view.findViewById(R.id.tv_tag);

                textView.setText(a.getMaterialName());
                textView.setTag(i);

                if (a.isMyAllergy()) {
                    textView.setBackgroundResource(R.drawable.select_round_bg);
                    textView.setTextColor(Color.parseColor("#FFFFFF"));
                }

                mAllergyFlowTagLayout.addView(view);

            }
        }

        List<FoodMaterial> foodMaterials = food.getFoodMaterials();
        if(!foodMaterials.isEmpty()) {
            for (int i = 0; i < foodMaterials.size(); i++) {
                FoodMaterial m = foodMaterials.get(i);
                View view = this.getLayoutInflater().inflate(R.layout.tag_item, null);
                TextView textView = (TextView) view.findViewById(R.id.tv_tag);
                textView.setText(m.getMaterialName());
                textView.setTag(i);

                if (m.isMyAllergy()) {
                    textView.setBackgroundResource(R.drawable.select_round_bg);
                    textView.setTextColor(Color.parseColor("#FFFFFF"));

                }

                mMaterialFlowTagLayout.addView(view);

            }
        }

        String[] tags = food.getTags().split(",");
        for (int i = 0; i < tags.length; i++) {
            View view = this.getLayoutInflater().inflate(R.layout.tag_item, null);
            TextView textView = (TextView) view.findViewById(R.id.tv_tag);
            textView.setBackgroundResource(R.drawable.tag_bg);
            textView.setText("#"+tags[i]);
            textView.setTag(i);
            mTagFlowTagLayout.addView(view);
        }

    }

    public void initGoogleSign() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
        onBackPressed();
        return true;
    }

    /**
     * Setup the firebase auth object
     */

    private void setupFirebaseAuth(){
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

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void signOut() {
        // Firebase sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
        mAuth.signOut();
    }
}
