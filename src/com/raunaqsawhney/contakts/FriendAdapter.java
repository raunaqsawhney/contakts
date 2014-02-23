package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class FriendAdapter extends ArrayAdapter<fbFriend> implements Filterable{

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        ImageView photo;
        TextView isAppUser;
    }
    
    private ArrayList<fbFriend> items;
    private ArrayList<fbFriend> originalItems = new ArrayList<fbFriend>();
    private final Object mLock = new Object();
    
    private Filter filter;

	private ImageLoader imageLoader;
	DisplayImageOptions options;

	private Context mContext;
	
	public FriendAdapter(Context context, ArrayList<fbFriend> friendList) {
	       super(context, R.layout.fb_friend_layout, friendList);
	       this.items = friendList;
           cloneItems(friendList);
           
	        mContext = context;

	        filter = new MyFilter();
	       
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
	
	protected void cloneItems(ArrayList<fbFriend> items) {
        for (Iterator iterator = items.iterator(); iterator
        .hasNext();) {
            fbFriend friend = (fbFriend) iterator.next();
            originalItems.add(friend);
        }
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
          viewHolder.isAppUser = (TextView) convertView.findViewById(R.id.friend_isappuser);
          
          convertView.setTag(viewHolder);
          
       } else {
           viewHolder = (ViewHolder) convertView.getTag();
       }
       // Populate the data into the template view using the data object
       viewHolder.name.setText(friend.getName());
       
       if (friend.getIsAppUser()) {
           viewHolder.isAppUser.setText(R.string.usesContakts);
       } else {
           viewHolder.isAppUser.setText("");
       }
       
       imageLoader.displayImage(friend.getURL(), viewHolder.photo, options);
       
       // Return the completed view to render on screen
       return convertView;
   }
	
	@Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new MyFilter();
        }
        return filter;
    }
	
	public int getCount() {
        return items.size();
    }

    public fbFriend getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return Long.valueOf(items.get(position).getID());
    }
    
    private class MyFilter extends Filter {
    	protected FilterResults performFiltering(CharSequence prefix) {
            // Initiate our results object
            FilterResults results = new FilterResults();

            // No prefix is sent to filter by so we're going to send back the original array
            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    results.values = originalItems;
                    results.count = originalItems.size();
                }
            } else {
                synchronized(mLock) {
                        // Compare lower case strings
                    String prefixString = prefix.toString().toLowerCase();
                    final ArrayList<fbFriend> filteredItems = new ArrayList<fbFriend>();
                    // Local to here so we're not changing actual array
                    final ArrayList<fbFriend> localItems = new ArrayList<fbFriend>();
                    localItems.addAll(originalItems);
                    final int count = localItems.size();

                    for (int i = 0; i < count; i++) {
                        final fbFriend item = localItems.get(i);
                        final String itemName = item.getName().toString().toLowerCase();

                        // First match against the whole, non-splitted value
                        if (itemName.startsWith(prefixString)) {
                            filteredItems.add(item);
                        } else {} /* This is option and taken from the source of ArrayAdapter
                            final String[] words = itemName.split(" ");
                            final int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                if (words[k].startsWith(prefixString)) {
                                    newItems.add(item);
                                    break;
                                }
                            }
                        } */
                    }

                    // Set and return
                    results.values = filteredItems;
                    results.count = filteredItems.size();
                }//end synchronized
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked
            synchronized(mLock) {
                final ArrayList<fbFriend> localItems = (ArrayList<fbFriend>) results.values;
                notifyDataSetChanged();
                clear();
                //Add the items back in
                for (Iterator iterator = localItems.iterator(); iterator
                        .hasNext();) {
                    fbFriend friend = (fbFriend) iterator.next();
                    add(friend);
                }
            }//end synchronized
        }
    }
}
