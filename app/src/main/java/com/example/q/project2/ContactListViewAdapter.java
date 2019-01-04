package com.example.q.project2;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ContactListViewAdapter extends BaseAdapter {

    private ArrayList<ContactItem> itemList = new ArrayList<ContactItem>();
    private Context mContext;

    public ContactListViewAdapter (Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public ContactItem getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contactitem_layout,viewGroup,false);
        }

        ImageView iv = (ImageView) convertView.findViewById(R.id.profileImage);
        TextView name = (TextView) convertView.findViewById(R.id.nameText);
        TextView phone = (TextView) convertView.findViewById(R.id.numText);

        ContactItem item = (ContactItem) getItem(position);

        Glide.with(mContext)
                .load(item.getImg())
                .into(iv);
        name.setText(item.getName());
        phone.setText(item.getPhone());

        return convertView;
    }

    public void addItem(Uri img, String name, String phone){
        ContactItem newItem = new ContactItem();
        newItem.setImg(img);
        newItem.setName(name);
        newItem.setPhone(phone);

        itemList.add(newItem);
    }
}
