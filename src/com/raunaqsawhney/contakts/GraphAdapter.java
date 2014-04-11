package com.raunaqsawhney.contakts;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class GraphAdapter extends ArrayAdapter<FreqContact> {

	// View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView photo;
        TextView timeContacted;
    }

	public GraphAdapter(Context context, ArrayList<FreqContact> freqContactList) {
	       super(context, R.layout.graph_item_layout, freqContactList);

	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
	    
	    String [] colorArray = new String[10];

	    colorArray[0] = "#33B5E5";
	    colorArray[1] = "#AA66CC";
	    colorArray[2] = "#4F2F4F";
	    colorArray[3] = "#99CC00";
	    colorArray[4] = "#669900";
	    colorArray[5] = "#FFBB33";
	    colorArray[6] = "#FF8800";
	    colorArray[7] = "#FF2D55";
	    colorArray[8] = "#FF4444";
	    colorArray[9] = "#CC0000";
		
		ViewHolder viewHolder = null;
		FreqContact freqContact = getItem(position);    

		try {
	       if (null == convertView) {
	    	   
	    	   LayoutInflater inflater = LayoutInflater.from(getContext());
	    	   convertView = inflater.inflate(R.layout.graph_item_layout, null);
	    	   
	    	   // Creates a ViewHolder and store references to
	           // the two children views we want to bind data to.
	    	   viewHolder = new ViewHolder();
	    	   viewHolder.name = (TextView) convertView.findViewById(R.id.freq_name);
	    	   viewHolder.photo = (ImageView) convertView.findViewById(R.id.freq_photo);
	    	   viewHolder.timeContacted = (TextView) convertView.findViewById(R.id.freq_count);
	    	   convertView.setTag(viewHolder); 
	    	   
	       } else {
	    	   // Get the ViewHolder back to get fast access to the TextView
	           // and the ImageView.
	    	   viewHolder = (ViewHolder) convertView.getTag();
	       }
	       
	       Integer count = freqContact.getCount();

	       // Bind the data efficiently with the holder.
	       viewHolder.name.setText(freqContact.getName().toString());
	       viewHolder.name.setTextColor(Color.parseColor(colorArray[count])); 
	       
	       viewHolder.photo.setImageURI(Uri.parse(freqContact.getURL()));
	       
	       if (!freqContact.getTimesContacted().isEmpty() || freqContact.getTimesContacted().equalsIgnoreCase("NULL") || freqContact.getTimesContacted().equals("".toString())) {
		       viewHolder.timeContacted.setText(freqContact.getTimesContacted());
	       } else {
		       viewHolder.timeContacted.setText("--");
	       }
	       viewHolder.timeContacted.setTextColor(Color.parseColor(colorArray[count]));

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

       // Return the completed view to render on screen
       return convertView;
   }
}
