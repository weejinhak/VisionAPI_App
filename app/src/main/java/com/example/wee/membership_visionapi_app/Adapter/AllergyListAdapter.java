package com.example.wee.membership_visionapi_app.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.wee.membership_visionapi_app.Models.Allergy;
import com.example.wee.membership_visionapi_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * Created by wee on 2018. 2. 9..
 */

public class AllergyListAdapter extends ArrayAdapter<Allergy> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mReference =
            FirebaseDatabase.getInstance().getReference().child("USERS")
                    .child(mAuth.getCurrentUser().getUid()).child("components");

    public AllergyListAdapter(Context context, int resource) {
        super(context, resource);
    }

    private View setView(LayoutInflater inflater) {
        View convertView = inflater.inflate(R.layout.activity_user_allergy_item, null);
        ViewHolder holder = new ViewHolder();
        holder.bindView(convertView);
        convertView.setTag(holder);
        return convertView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = setView(inflater);
        }

        if (convertView.getTag() instanceof ViewHolder) {
            convertView = setView(inflater);
            ((ViewHolder) convertView.getTag()).setData(position);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView mAllergyName;
        ImageButton mDeleteBtn;

        private void bindView(View convertView) {
            mAllergyName = convertView.findViewById(R.id.component_name_textview);
            mDeleteBtn = convertView.findViewById(R.id.item_delete_btn);
        }

        private void setData(int position) {
            final Allergy allergy = getItem(position);
            mAllergyName.setText(allergy.getName());
            mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Delete Btn Clicked");
                    mReference.child(allergy.getFirebaseKey()).removeValue();
                }
            });
        }
    }
}

