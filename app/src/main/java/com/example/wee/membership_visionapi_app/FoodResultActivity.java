package com.example.wee.membership_visionapi_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wee.membership_visionapi_app.Adapter.TagAdapter;
import com.example.wee.membership_visionapi_app.Models.AllergyIngredient;
import com.example.wee.membership_visionapi_app.Models.Food;
import com.example.wee.membership_visionapi_app.Models.FoodMaterial;
import com.example.wee.membership_visionapi_app.widget.FlowTagLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodResultActivity extends AppCompatActivity {

    private Food food;
    private ImageView foodThumbnail;
    private TextView foodName_tv;
    private TextView allergyResult_tv1,allergyResult_tv2;
    private FlowTagLayout mAllergyFlowTagLayout,mMaterialFlowTagLayout,mTagFlowTagLayout;
    private TagAdapter<String> mAllergyAdapter,mMaterialAdapter,mTagAdapter;


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


        foodName_tv.setText(food.getFoodName());
        Picasso.with(this).load(food.getThumbnailUrl()).into(foodThumbnail);//실패이미지 나중에

        int count = food.getCount();
        if(count==0){
            allergyResult_tv1.setText(userName+"님에게 안전해요");
        }else if(count>0){
            allergyResult_tv1.setText(userName+"님에게 위험해요");
        }
        allergyResult_tv2.setText(count+"개");


        mAllergyFlowTagLayout = (FlowTagLayout) findViewById(R.id.allergy_flow_layout);
        mMaterialFlowTagLayout = (FlowTagLayout) findViewById(R.id.material_flow_layout);
        mTagFlowTagLayout = (FlowTagLayout) findViewById(R.id.tag_flow_layout);

        mAllergyAdapter = new TagAdapter<>(this);
        List<AllergyIngredient> allergyIngredients = food.getAllergyIngredients();
        for (int i = 0; i < allergyIngredients.size(); i++) {
            AllergyIngredient a = allergyIngredients.get(i);
            if (a.isMyAllergy()) {
                mAllergyAdapter.setSelected(i);
            }
        }
        mAllergyFlowTagLayout.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        mAllergyFlowTagLayout.setAdapter(mAllergyAdapter);

        mMaterialAdapter = new TagAdapter<>(this);
        List<FoodMaterial> foodMaterials = food.getFoodMaterials();
        for (int i = 0; i < foodMaterials.size(); i++) {
            FoodMaterial m = foodMaterials.get(i);
            if (m.isMyAllergy()) {
                mMaterialAdapter.setSelected(i);
            }
        }
        mMaterialFlowTagLayout.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        mMaterialFlowTagLayout.setAdapter(mMaterialAdapter);

        mTagAdapter = new TagAdapter<>(this);
        mTagFlowTagLayout.setAdapter(mTagAdapter);


        initData();

    }

    private void initData() {
        List<AllergyIngredient> allergyIngredientList = food.getAllergyIngredients();
        List<String> dataSource = new ArrayList<>();
        for (int i = 0; i < allergyIngredientList.size(); i++) {
            dataSource.add(allergyIngredientList.get(i).getMaterialName());
        }
        mAllergyAdapter.onlyAddAll(dataSource);

        List<FoodMaterial> foodMaterialList = food.getFoodMaterials();
        dataSource = new ArrayList<>();
        for (int i = 0; i < foodMaterialList.size(); i++) {
            dataSource.add(foodMaterialList.get(i).getMaterialName());
        }
        mMaterialAdapter.onlyAddAll(dataSource);

        String[] tags = food.getTags().split(",");
        dataSource = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            dataSource.add("#"+tags[i]);
        }
        mTagAdapter.onlyAddAll(dataSource);
    }
}
