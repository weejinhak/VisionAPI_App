package com.example.wee.membership_visionapi_app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wee.membership_visionapi_app.Models.AllergyIngredient;
import com.example.wee.membership_visionapi_app.Models.Food;
import com.example.wee.membership_visionapi_app.Models.FoodMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoodResultActivity extends AppCompatActivity {

    private Food food;
    private ImageView foodThumbnail;
    private TextView foodName_tv;
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


        foodName_tv.setText(food.getFoodName());
        Picasso.with(this).load(food.getThumbnailUrl()).into(foodThumbnail);//실패이미지 나중에

        int count = food.getCount();
        if(count==0){
            allergyResult_tv1.setText(userName+"님에게 안전해요");
        }else if(count>0){
            allergyResult_tv1.setText(userName+"님에게 위험해요");
        }
        allergyResult_tv2.setText(count+"개");


        mAllergyFlowTagLayout = (FlowLayout) findViewById(R.id.allergy_flow_layout);
        mMaterialFlowTagLayout = (FlowLayout) findViewById(R.id.material_flow_layout);
        mTagFlowTagLayout = (FlowLayout) findViewById(R.id.tag_flow_layout);


        List<AllergyIngredient> allergyIngredientList = food.getAllergyIngredients();
        for (int i = 0; i < allergyIngredientList.size(); i++) {
            AllergyIngredient a = allergyIngredientList.get(i);
            Log.i("##",a.getMaterialName()+"  "+a.isMyAllergy());


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

        List<FoodMaterial> foodMaterials = food.getFoodMaterials();
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

        String[] tags = food.getTags().split(",");
        for (int i = 0; i < tags.length; i++) {
            View view = this.getLayoutInflater().inflate(R.layout.tag_item, null);
            TextView textView = (TextView) view.findViewById(R.id.tv_tag);
            textView.setText("#"+tags[i]);
            textView.setTag(i);
            mTagFlowTagLayout.addView(view);
        }

    }


}
