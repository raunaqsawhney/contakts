package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends Activity implements OnQueryTextListener, LoaderCallbacks<Cursor> {
	
	/*
	 * Declare Globals
	 */
	String theme = "#18A7B5";
	String font = "RobotoCondensed-Regular.ttf";

	SimpleCursorAdapter mAdapter;
	String mFilter;
	String photoURI;
	ListView contactList;
	String itemid;
	
	private SlidingMenu menu;
	private ArrayAdapter<String> listAdapter;
	private ListView navListView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);	

        /*
         * Set up the Action Bar
         */
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
       
        /*
         * Do Title Bar Tint only if KITKAT
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        // Holo light action bar color is #DDDDDD
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }
        
        
        contactList = (ListView)findViewById(R.id.list);
        contactList.setOnItemClickListener(new OnItemClickListener() {
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
        
	        
        /*
         * Fetch the name and contact photo uri
         */
        String[] from = new String[] {
        		ContactsContract.Data.DISPLAY_NAME,
        		ContactsContract.Data.PHOTO_URI
        };
        
        int[] to = new int[] {
        		R.id.c_name,
        		R.id.c_photo
        };
        
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        
	    LoaderManager loaderManager = getLoaderManager();
	    loaderManager.initLoader(0, null, this);	
	    
        contactList.setAdapter(mAdapter);

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
                	Intent stIntent = new Intent(MainActivity.this, FavActivity.class);
            		MainActivity.this.startActivity(stIntent);
                } else if (item == "Phone Contacts") {
                	Intent pIntent = new Intent(MainActivity.this, MainActivity.class);
            		MainActivity.this.startActivity(pIntent);
                } else if (item == "Google Contacts") {
                	Intent gIntent = new Intent(MainActivity.this, GoogleActivity.class);
            		MainActivity.this.startActivity(gIntent);
                }
            }
        });
	}
   
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.options_menu, menu);
    	
		/*
		* Set up the Action Bar Menu
		*/
                  
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("Find contacts");
        searchView.setQueryHint(Html.fromHtml("<font color = #F7F7F7>" + getResources().getString(R.string.search_hint) + "</font>"));
        
        searchView.setOnQueryTextListener(this);        
        
        AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        search_text.setTextColor(Color.WHITE);
        search_text.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        
        return true;

    
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri baseUri;
        if (mFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }
        
        String query = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
        
        
        /*
        String photoURI = null;
        int imageResource = getResources().getIdentifier("@drawable/ic_contact_picture", null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
         */

        String[] projection = new String[] {
        	ContactsContract.Contacts._ID,
        	ContactsContract.Contacts.DISPLAY_NAME,
        	ContactsContract.Contacts.PHOTO_URI
        };
        
        
        //System.out.println(photoURI);
		//System.out.println(ContactsContract.Contacts.PHOTO_URI);

        CursorLoader cursorLoader = new CursorLoader(
        		MainActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                Contacts.DISPLAY_NAME);
        
        return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
		System.out.println(ContactsContract.Contacts.PHOTO_URI);
		if (ContactsContract.Contacts.PHOTO_URI == null)
		{
			ImageView c_photo = (ImageView) findViewById(R.id.c_photo);
			c_photo.setImageResource(R.drawable.ic_contact_picture);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);		
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		mFilter = !TextUtils.isEmpty(arg0) ? arg0 : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	            createNewContact();
	            return true;    
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	}

	private void createNewContact() {
		Intent intent = new Intent(Intent.ACTION_INSERT, 
                ContactsContract.Contacts.CONTENT_URI);
		startActivity(intent);	
	}
}