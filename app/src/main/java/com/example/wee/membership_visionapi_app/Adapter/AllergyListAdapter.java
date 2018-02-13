package com.example.wee.membership_visionapi_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.wee.membership_visionapi_app.Allergy;
import com.example.wee.membership_visionapi_app.R;


/**
 * Created by wee on 2018. 2. 9..
 */

public class AllergyListAdapter extends ArrayAdapter<Allergy> {

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
        int viewType = getItemViewType(position);
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
        private TextView mAllergyName;

        private void bindView(View convertView) {
            mAllergyName = (TextView) convertView.findViewById(R.id.component_name_textview);
        }

        private void setData(int position) {
            Allergy allergy = getItem(position);
            mAllergyName.setText(allergy.getName());
        }
    }
}

