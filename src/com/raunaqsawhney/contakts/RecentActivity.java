package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class RecentActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	String contact_id;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	private RecentCursorAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent);
		
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		
		getRecents();
		
	}


	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), fontTitle));
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
      
		final String[] nav = { getString(R.string.sMfavourites),
				getString(R.string.sMMostContacted),
				getString(R.string.sMPhoneContacts),
				getString(R.string.sMGoogleContacts),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings),
				getString(R.string.sMAbout)
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
	
	private void getRecents() {
		ListView recentList = (ListView) findViewById(R.id.recentList);
		
        recentList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        
            }
        });
        
        
	    String[] from = {CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE};
	    int to[] = new int[]{
	    		R.id.r_name,
	    		R.id.r_number,
	    		R.id.r_date,
	    };
	    
	    mAdapter = new RecentCursorAdapter(
	            this,
	            R.layout.recent_layout,
	            null,
	            from,
	            to);		
	    
        recentList.setAdapter(mAdapter);
	    getLoaderManager().initLoader(0, null, this);

	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		        
		Uri queryUri = android.provider.CallLog.Calls.CONTENT_URI;

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            CallLog.Calls._ID,
	            CallLog.Calls.NUMBER,
	            CallLog.Calls.CACHED_NAME,
	            CallLog.Calls.DATE,
	            CallLog.Calls.DURATION,
	            CallLog.Calls.TYPE};
        
	    String sortOrder = String.format("%s limit 100 ", CallLog.Calls.DATE + " DESC");

        cursorLoader = new CursorLoader(
        		RecentActivity.this, 
        		queryUri,
                projection, 
                null, 
                null,
                sortOrder);	
        
        return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recent, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

}
