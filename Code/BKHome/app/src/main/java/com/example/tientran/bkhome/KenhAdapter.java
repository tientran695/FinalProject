package com.example.tientran.bkhome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tientran on 07/03/2018.
 */

public class KenhAdapter extends BaseAdapter {
    Context context;
    int layout;
    List<Kenh> arrayChannel;

    public KenhAdapter(Context context, int layout, List<Kenh> arrayChannel) {
        this.context = context;
        this.layout = layout;
        this.arrayChannel = arrayChannel;
    }

    @Override
    public int getCount() {
        return arrayChannel.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layout, null);
        //anh xa va gan gia tri
        TextView nameChannel = (TextView)convertView.findViewById(R.id.tvTenKenh);
        TextView numberChannel = (TextView)convertView.findViewById(R.id.tvSoKenh);
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);

        nameChannel.setText(arrayChannel.get(position).nameChannel);
        numberChannel.setText(arrayChannel.get(position).numberChannel);
        //Set icon cho kenh
        if(arrayChannel.get(position).isChecked())
            imageView.setImageResource(R.drawable.checked);
        else
            imageView.setImageResource(arrayChannel.get(position).idImage);
        return convertView;
    }
}
