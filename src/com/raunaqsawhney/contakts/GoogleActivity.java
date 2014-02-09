package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class GoogleActivity extends Activity implements OnQueryTextListener, LoaderCallbacks<Cursor>, OnItemClickListener {

	// Declare Globals
	String font;
	String fontTitle;
	String fontContent;
	String theme;

	SimpleCursorAdapter mAdapter;
	String mFilter;
	String photoURI;
	ListView contactList;
	String itemid;
	
	private SlidingMenu menu;
	private ListView navListView;
	
   @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        initializeLoader();
        
		Session.openActiveSessionFromCache(getBaseContext());

	}

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
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
	        tintManager.setNavigationBarTintEnabled(true);
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
	        tintManager.setNavigationBarTintColor(Color.parseColor("#000000"));
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

	private void initializeLoader() {
		
		// Set up the ListView for contacts to be displayed
        contactList = (ListView)findViewById(R.id.list);
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor)parent.getItemAtPosition(position);
				startManagingCursor(cursor);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                startActivity(intent);
            }
        });
	        
        // Fetch Display Name and Contact Photo URI
        String[] from = new String[] {
        		ContactsContract.Data.DISPLAY_NAME,
        		ContactsContract.Data.PHOTO_URI
        };
        
        // Put above content into XML layouts
        int[] to = new int[] {
        		R.id.c_name,
        		R.id.c_photo
        };
	        
        // Set the adapter to display the list
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        
        // Initialize the loader for background activity
	    LoaderManager loaderManager = getLoaderManager();
	    loaderManager.initLoader(0, null, this);	
	    
        contactList.setAdapter(mAdapter);
        View header = getLayoutInflater().inflate(R.layout.google_header, null);
        contactList.addHeaderView(header);	
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
        
        String query = "(" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL)";
        
        String[] projection = new String[] {
        	ContactsContract.Contacts._ID,
        	ContactsContract.Contacts.DISPLAY_NAME,
        	ContactsContract.Contacts.PHOTO_URI
        };
        
        CursorLoader cursorLoader = new CursorLoader(
        		GoogleActivity.this, 
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
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.options_menu, menu);
    	
        // Set up the Action Bar menu Search
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
        	case R.id.menu_dial:
        		Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        		startActivity(dialIntent);
            return true;
	        case R.id.menu_add:
	    		Intent addIntent = new Intent(Intent.ACTION_INSERT, 
	            ContactsContract.Contacts.CONTENT_URI);
	    		startActivity(addIntent);	
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(GoogleActivity.this, FavActivity.class);
		   	GoogleActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(GoogleActivity.this, FrequentActivity.class);
		   GoogleActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(GoogleActivity.this, MainActivity.class);
	   		GoogleActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(GoogleActivity.this, GoogleActivity.class);
	   		GoogleActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(GoogleActivity.this, FBActivity.class);
	   		GoogleActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(GoogleActivity.this, LoginActivity.class);
		   	GoogleActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(GoogleActivity.this, InfoActivity.class);
		   	GoogleActivity.this.startActivity(iIntent);
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