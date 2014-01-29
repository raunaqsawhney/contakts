package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FavActivity extends Activity {
	
	private SlidingMenu menu;
	private ArrayAdapter<String> listAdapter;
	private ListView navListView;
	
	String theme = "#18A7B5";
	String font = "RobotoCondensed-Regular.ttf";
	
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.fav_activity);
	        
	        setupActionBar();
	        setupFavList();
	   }

	   private void setupActionBar() {
	   
		// Setup ActionBar
        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView actionBarTitleText = (TextView) findViewById(titleId);
        Typeface actionBarFont = Typeface.createFromAsset(getAssets(), "Harabara.ttf");
        actionBarTitleText.setTypeface(actionBarFont);
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(24);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
       
        // Do Tint only if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        // Holo light action bar color is #DDDDDD
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }
        
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidth(8);
        menu.setFadeDegree(0.8f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(800);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setMenu(R.layout.menu_frame);
        navListView = (ListView) findViewById(R.id.nav_menu);
      
		String[] nav = new String[] { "Favourites", "Phone Contacts", "Google Contacts" };
		ArrayList<String> navList = new ArrayList<String>();
		navList.addAll(Arrays.asList(nav));
		
		listAdapter = new ArrayAdapter<String>(this,
	            R.layout.nav_item_layout, R.id.nav_name, navList);
		
		navListView.setAdapter(listAdapter);
		navListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String item = String.valueOf(navListView.getItemAtPosition(position));
                if (item == "Favourites") {
                	Intent stIntent = new Intent(FavActivity.this, FavActivity.class);
                	FavActivity.this.startActivity(stIntent);
                } else if (item == "Phone Contacts") {
                	Intent pIntent = new Intent(FavActivity.this, MainActivity.class);
                	FavActivity.this.startActivity(pIntent);
                } else if (item == "Google Contacts") {
                	Intent gIntent = new Intent(FavActivity.this, GoogleActivity.class);
                	FavActivity.this.startActivity(gIntent);
                }
            }
        });
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

	    @SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(queryUri, projection, selection,null,null);

	    long id= cursor.getColumnIndex(ContactsContract.Contacts._ID);
	    
	    Bitmap bitmap = loadContactPhoto(getContentResolver(), id);
	    if(bitmap!=null){
	    favIcon.setImageBitmap(bitmap);
	    }
	    else{

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
        
        // Assign adapter to the HorizontalListView
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

}
