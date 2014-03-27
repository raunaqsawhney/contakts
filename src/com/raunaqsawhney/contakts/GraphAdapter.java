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

public class GraphAdapter extends ArrayAdapter<FreqContact>{
	
	// View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView photo;
        TextView timeContacted;
    }

	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	public GraphAdapter(Context context, ArrayList<FreqContact> freqContactList) {
	       super(context, R.layout.graph_item_layout, freqContactList);
	       
	        this.imageLoader = ImageLoader.getInstance();
	        
	         options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_contact_picture)
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
            .build();
	        
	        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
	        .discCacheExtraOptions(100, 100, CompressFormat.PNG, 100, null)
            .build();
	        imageLoader.init(config);
	       
	    }


	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		 
		try {
	       // Get the data item for this position
	       FreqContact freqContact = getItem(position);    
	       
	       // Check if an existing view is being reused, otherwise inflate the view
	       ViewHolder viewHolder; // view lookup cache stored in tag
	       if (convertView == null) {
	          viewHolder = new ViewHolder();
	          LayoutInflater inflater = LayoutInflater.from(getContext());
	          convertView = inflater.inflate(R.layout.graph_item_layout, null);
	          viewHolder.name = (TextView) convertView.findViewById(R.id.freq_name);
	          viewHolder.photo = (ImageView) convertView.findViewById(R.id.freq_photo);
	          viewHolder.timeContacted = (TextView) convertView.findViewById(R.id.freq_count);
	          convertView.setTag(viewHolder);
	       } else {
	           viewHolder = (ViewHolder) convertView.getTag();
	       }
	       
			String [] colorArray;
			colorArray = new String[10];

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
				       
	       Integer count = freqContact.getCount();
	       
	       // Populate the data into the template view using the data object
	       viewHolder.name.setText(freqContact.getName());
	       viewHolder.name.setTextColor(Color.parseColor(colorArray[count]));
	       
	       viewHolder.photo.setImageURI(Uri.parse(freqContact.getURL()));
	       
	       viewHolder.timeContacted.setText(freqContact.getTimesContacted());
	       viewHolder.timeContacted.setTextColor(Color.parseColor(colorArray[count]));
	       
		   System.out.println("DONE getView");
			
		}catch (NullPointerException e) {
			//
		}

       // Return the completed view to render on screen
       return convertView;
       
   }
}
