package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FavActivity extends Activity implements OnItemClickListener{

	private SlidingMenu menu;
	private ListView navListView;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	private boolean firstRunDoneFav;
	
   @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fav_activity);
        
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        setupFavList();
        
		Session.openActiveSessionFromCache(getBaseContext());
		
   }

	private void setupGlobalPrefs() {
		   
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneFav = prefs.getBoolean("firstRunDoneFav", false);
        if (!firstRunDoneFav) {
        	edit.putBoolean("firstRunDoneFav", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle("Favourites")
		    .setMessage("Here you can see a grid of all your starred contacts. To remove a favourite, simply tap and hold on a contact.")
		    		.setNeutralButton("Okay", null)
		    .show();
        }
	}

	private void setupActionBar() {
		   
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setHomeButtonEnabled(true);
       
        // Do Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        
	        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
	        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, -150, 0,0);
	        config.getPixelInsetBottom();
	        
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }
	}

	private void setupSlidingMenu() {
		
		// Set up Sliding Menu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidth(8);
        menu.setFadeDegree(0.8f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(800);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.setMenu(R.layout.menu_frame);
        navListView = (ListView) findViewById(R.id.nav_menu);
      
		final String[] nav = { "Favourites",
				"Most Contacted",
				"Phone Contacts",
				"Google Contacts",
				"Facebook",
				"Settings",
				"About"
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_settings,
				R.drawable.ic_nav_about
		};

		List<RowItem> rowItems;
		
		rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < nav.length; i++) {
            RowItem item = new RowItem(navPhoto[i], nav[i]);
            rowItems.add(item);
        }
		
		CustomListViewAdapter listAdapter = new CustomListViewAdapter(this,
                R.layout.nav_item_layout, rowItems);
		
		navListView.setAdapter(listAdapter);
		navListView.setOnItemClickListener(this);	
		
	}


	private void setupFavList() {
		
		ImageView favIcon = (ImageView) findViewById(R.id.fav_photo);
		
		Uri queryUri = ContactsContract.Contacts.CONTENT_URI;

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.LOOKUP_KEY,
	            ContactsContract.Contacts.PHOTO_URI,
	            ContactsContract.Contacts.DISPLAY_NAME,
	            ContactsContract.Contacts.STARRED};

	    String selection = ContactsContract.Contacts.STARRED + "='1'";

	    Cursor cursor = getContentResolver().query(queryUri, projection, selection,null,null);

	    long id= cursor.getColumnIndex(ContactsContract.Contacts._ID);
	    
	    Bitmap bitmap = loadContactPhoto(getContentResolver(), id);
	    if(bitmap!=null) {
	    	favIcon.setImageBitmap(bitmap);
	    } else {
	    	// NOTHING
	    }
	    
	    String[] from = {ContactsContract.Contacts.Photo.PHOTO_URI , ContactsContract.Contacts.DISPLAY_NAME};
	    int to[] = new int[]{
	    		R.id.fav_photo,
	    		R.id.fav_name
	    };

	    ListAdapter adapter = new SimpleCursorAdapter(
	            this,
	            R.layout.fav_layout,
	            cursor,
	            from,
	            to,
	            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
        GridView favGrid = (GridView) findViewById(R.id.favGrid);
        
        favGrid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view,
					final int position, long id) {
				AlertDialog alertDialog = new AlertDialog.Builder(
                       FavActivity.this).create();
				
				// Setting Dialog Title
		        alertDialog.setTitle("Remove Favourite");
		        
		        // Setting Dialog Message
		        alertDialog.setMessage("Are you sure you want to remove this contact from your favourites?");
		        
		        // Setting OK Button
		        alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                	
		                	Cursor cursor = (Cursor)parent.getItemAtPosition(position);
		    				String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		                	
		                    String[] fv = new String[] { displayName };
		                    
		                	ContentValues values = new ContentValues();
		                    values.put(ContactsContract.Contacts.STARRED, 0);
		                    getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);
		                    
		                    Intent intent = new Intent(getApplicationContext(), FavActivity.class);
		                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		                    startActivity(intent);
		        	}
		        });
		        // Showing Alert Message
		        alertDialog.show();
				return true;
			}
            
        });
        
        favGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor)parent.getItemAtPosition(position);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
				// Explicit Intent Example
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                startActivity(intent);
		        
            }
        });
        
        favGrid.setAdapter(adapter);
   }

	private Bitmap loadContactPhoto(ContentResolver contentResolver, long id) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
	    if (input == null) {

	        return null;
	    }
	    return BitmapFactory.decodeStream(input);		
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FavActivity.this, FavActivity.class);
		   	FavActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(FavActivity.this, FrequentActivity.class);
		   FavActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(FavActivity.this, MainActivity.class);
	   		FavActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(FavActivity.this, GoogleActivity.class);
	   		FavActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(FavActivity.this, FBActivity.class);
	   		FavActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(FavActivity.this, LoginActivity.class);
		   	FavActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(FavActivity.this, InfoActivity.class);
		   	FavActivity.this.startActivity(iIntent);
	   } 
	}
	
	@Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }
	
	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
}
