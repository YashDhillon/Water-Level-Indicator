package com.adara.yashsd.waterlevelindicator;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class customListviewAdapter extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] arr;
    private final String[] arr1;

    public customListviewAdapter(Activity context, String[] arr, String[] arr1)
    {
        super(context,R.layout.custom_layout,arr);
        this.context = context;
        this.arr = arr;
        this.arr1 = arr1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View viewrow = inflater.inflate(R.layout.custom_layout,null,true);

        TextView nameview = (TextView)viewrow.findViewById(R.id.patternname);
        TextView phonenoview = (TextView)viewrow.findViewById(R.id.pattern);

        nameview.setText(arr[position]);
        phonenoview.setText(arr1[position]);
        return viewrow;
    }
}