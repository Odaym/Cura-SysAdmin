package com.cura.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cura.R;

import java.util.List;

public class Drawer_Adapter extends BaseAdapter {

    private final int MANAGE_KEYS = 0;
    private final int SETTINGS = 1;
    private final int REPORT_BUGS = 2;
    private final int RATE_US = 3;
    private final int ABOUT = 4;

    private LayoutInflater inflater;
    private ViewHolder holder;
    private Context context;
    private List<String> drawerItems;

    public Drawer_Adapter(Context context, List<String> drawerItems) {
        super();
        this.context = context;
        this.drawerItems = drawerItems;
    }

    @Override
    public int getCount() {
        return drawerItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent,
                    false);

            holder = new ViewHolder();

            holder.itemDescription = (TextView) convertView.findViewById(R.id.drawer_list_item_TV);
            holder.itemImage = (ImageView) convertView.findViewById(R.id.drawer_list_item_IV);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemDescription.setText(drawerItems.get(position));

        switch (position) {
            case MANAGE_KEYS:
                holder.itemImage.setImageResource(R.drawable.keys);
                break;
            case REPORT_BUGS:
                holder.itemImage.setImageResource(R.drawable.report_bugs);
                break;
            case SETTINGS:
                holder.itemImage.setImageResource(R.drawable.settings);
                break;
            case RATE_US:
                holder.itemImage.setImageResource(R.drawable.rate_us);
                break;
            case ABOUT:
                holder.itemImage.setImageResource(R.drawable.about);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView itemImage;
        TextView itemDescription;
    }
}
