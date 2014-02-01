package com.raunaqsawhney.contakts;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
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

public class FriendAdapter extends ArrayAdapter<fbFriend> {
	

	
    // View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView photo;
        TextView presence;
    }

	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	public FriendAdapter(Context context, ArrayList<fbFriend> friendList) {
	       super(context, R.layout.fb_friend_layout, friendList);
	       
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
		 
		
       // Get the data item for this position
       fbFriend friend = getItem(position);    
       
       // Check if an existing view is being reused, otherwise inflate the view
       ViewHolder viewHolder; // view lookup cache stored in tag
       if (convertView == null) {
          viewHolder = new ViewHolder();
          LayoutInflater inflater = LayoutInflater.from(getContext());
          convertView = inflater.inflate(R.layout.fb_friend_layout, null);
          viewHolder.name = (TextView) convertView.findViewById(R.id.friend_name);
          viewHolder.photo = (ImageView) convertView.findViewById(R.id.friend_photo);
          viewHolder.presence = (TextView) convertView.findViewById(R.id.friend_presence);
          convertView.setTag(viewHolder);
       } else {
           viewHolder = (ViewHolder) convertView.getTag();
       }
       // Populate the data into the template view using the data object
       viewHolder.name.setText(friend.getName());
       
       imageLoader.displayImage(friend.getURL(), viewHolder.photo, options);
       
       viewHolder.presence.setText(friend.getPresence());
       
       

       
       // Return the completed view to render on screen
       return convertView;
   }
}
