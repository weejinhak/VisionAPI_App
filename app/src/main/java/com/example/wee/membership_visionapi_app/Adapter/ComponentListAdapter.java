package com.example.wee.membership_visionapi_app.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.wee.membership_visionapi_app.Models.Component;
import com.example.wee.membership_visionapi_app.R;

public class ComponentListAdapter extends ArrayAdapter<Component> {

    public ComponentListAdapter(Context context, int resource) {
        super(context, resource);
    }

    private View setView(LayoutInflater inflater) {
        View convertView = inflater.inflate(R.layout.component_item, null);
        ComponentListAdapter.ViewHolder holder = new ComponentListAdapter.ViewHolder();
        holder.bindView(convertView);
        convertView.setTag(holder);
        return convertView;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = setView(inflater);
        }

        if (convertView.getTag() instanceof ComponentListAdapter.ViewHolder) {
            convertView = setView(inflater);
            ((ComponentListAdapter.ViewHolder) convertView.getTag()).setData(position);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView mComponentName;

        private void bindView(View convertView) {
            mComponentName = convertView.findViewById(R.id.component_name_textview);
        }

        private void setData(int position) {
            final Component component = getItem(position);
            mComponentName.setText(component != null ? component.getName() : null);
            if( component.isAllergy() )
                mComponentName.setTextColor(0xFFFF0000);
        }
    }
}
