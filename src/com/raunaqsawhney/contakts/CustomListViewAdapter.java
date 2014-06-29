package com.raunaqsawhney.contakts;

import java.util.List;

import com.raunaqsawhney.contakts.R.color;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomListViewAdapter extends ArrayAdapter<RowItem>{
	 
    Context context;
    LayoutInflater mInflater;
 
    public CustomListViewAdapter(Context context, int resourceId,
            List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }
 
    /* static private view holder class for speed scrolling*/
    static private class ViewHolder {
        ImageView imageView;
        TextView txtView;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.nav_item_layout, parent, false);
            
            holder = new ViewHolder();
            holder.txtView = (TextView) convertView.findViewById(R.id.nav_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.nav_photo);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }
        
        
        switch (position) {
        	case 0:
        		holder.txtView.setTextColor(Color.parseColor("#FF2D55"));
        		break;
        	case 1:
        		holder.txtView.setTextColor(Color.parseColor("#FFBB33"));
        		break;
        	case 2:
        		holder.txtView.setTextColor(Color.parseColor("#33b5e5"));
        		break;
        	case 3:
        		holder.txtView.setTextColor(Color.parseColor("#FF8800"));
        		break;
        	case 4:
        		holder.txtView.setTextColor(Color.parseColor("#5AD427"));
        		break;
        	case 5:
        		holder.txtView.setTextColor(Color.parseColor("#76004b"));
        		break;
        	case 6:
        		holder.txtView.setTextColor(Color.parseColor("#669900"));
        		break;
        	case 7:
        		holder.txtView.setTextColor(Color.parseColor("#0059b1"));
        		break;
        }
        
        	
        	
        RowItem rowItem = (RowItem)getItem(position);
        holder.txtView.setText(rowItem.getNavName());
        holder.imageView.setImageResource(rowItem.getNavImageId());
        
        return convertView;
    }
}
