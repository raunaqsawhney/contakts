package com.raunaqsawhney.contakts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class HomeActivity extends Activity implements OnItemClickListener {
	
	private SlidingMenu menu;
	private ListView navListView;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	private String ownerPhoto;
	private String missedCallContact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        
        populateHome();
	}


	private void setupGlobalPrefs() {
		   
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#33B5E5");
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
        		getString(R.string.sMRecent),
				getString(R.string.sMMostContacted),
				getString(R.string.sMPhoneContacts),
				getString(R.string.sMGoogleContacts),
				getString(R.string.sMGroups),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings),
				getString(R.string.sMAbout)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
				R.drawable.ic_nav_group,
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
	
	private void populateHome() {
		getUserInfo();
		getTimeAndDate();
		getMissedCalls();
		//getUnreadSMS();
		//getMostContacted();
		//getFavourites();
		
	}
	

	private void getUserInfo() {

		String ownerName = null;
		Cursor ownerCur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null,
                null,
                null, null);
        startManagingCursor(ownerCur);

    	while (ownerCur.moveToNext()) {
        	ownerName = ownerCur.getString(
            		ownerCur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));   
        }
        
        Cursor ownerPhotoCur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI,null,
                null,
                null, null);
        startManagingCursor(ownerCur);
        
        while (ownerPhotoCur.moveToNext()) {
        	ownerPhoto = ownerPhotoCur.getString(
        			ownerPhotoCur.getColumnIndex(ContactsContract.Profile.PHOTO_URI));   
        }
        
        TextView ownerTV = (TextView) findViewById(R.id.helloGreeting);
        
        if (ownerName == null)
            ownerTV.setText(getString(R.string.hello) + "!");
        else 
            ownerTV.setText(getString(R.string.hello) + ", " + ownerName +"!");

        ImageView ownerIV = (ImageView) findViewById(R.id.userPhoto);
        try {
            ownerIV.setImageURI(Uri.parse(ownerPhoto));
        } catch (NullPointerException e) {
            ownerIV.setImageURI(null);
        }
	}
	
	private void getTimeAndDate() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("EEEE, dd LLLL yyyy");
		String formattedDate = df.format(c.getTime());	
		
		TextView todaysDate = (TextView) findViewById(R.id.dateTime);
		todaysDate.setText(formattedDate);
	}
	
	private void getMissedCalls() {
		
		String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            CallLog.Calls._ID,
	            CallLog.Calls.NUMBER,
	            CallLog.Calls.CACHED_NAME,
	            CallLog.Calls.DATE,
	            CallLog.Calls.DURATION,
	            CallLog.Calls.TYPE };
		
		String selection = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE;          
		
	    String sortOrder = String.format("%s limit 5 ", CallLog.Calls.DATE + " DESC");
		
		Cursor missedCursor = getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				projection,
				selection,
                null,
                sortOrder);
        startManagingCursor(missedCursor);

    	while (missedCursor.moveToNext()) {
        	missedCallContact = missedCursor.getString(missedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        	
        	System.out.println(missedCallContact);
        }		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

}
