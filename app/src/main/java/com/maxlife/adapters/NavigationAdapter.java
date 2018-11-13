package com.maxlife.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.data.NavItemsData;

import java.util.ArrayList;


public class NavigationAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavItemsData> navDrawerItems;

    public NavigationAdapter(Context context, ArrayList<NavItemsData> navDrawerItems) {
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        listItem = inflater.inflate(R.layout.adapter_navigation_list, parent, false);
        ImageView imageViewIcon = (ImageView) listItem.findViewById(R.id.imageViewIcon);
        TextView textViewName = (TextView) listItem.findViewById(R.id.textViewName);
        textViewName.setText(navDrawerItems.get(position).title);
        imageViewIcon.setImageResource(navDrawerItems.get(position).icon);

        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        textViewName.setTypeface(font, Typeface.NORMAL);

        return listItem;

    }
}
