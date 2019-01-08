package com.example.q.project2;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MoyeoAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<MoyeoItem> data;
    private int layout;

    public MoyeoAdapter(Context context, int layout, ArrayList<MoyeoItem> data) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView=inflater.inflate(layout,parent,false);
        }

        MoyeoItem moyeoItem = data.get(position);

        TextView name = (TextView) convertView.findViewById(R.id.nameItemText);
        name.setText(moyeoItem.getName());

        return convertView;
    }
}
