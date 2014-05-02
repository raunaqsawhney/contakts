package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class InfoActivity extends Activity implements OnItemClickListener {

	private String theme;
	
	private SlidingMenu menu;
	private ListView navListView;

	private String fontTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        showInfo();
        
	}


	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		theme = prefs.getString("theme", "#0099CC");
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
				getString(R.string.sMGroups),
				getString(R.string.sMShuffle),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_group,
				R.drawable.ic_shuffle,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_settings
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
	
	private void showInfo() {
		
		String versionName = null;
		try {
			versionName = getString(R.string.currentVersion)+ " "+ getApplicationContext().getPackageManager()
				    .getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = getString(R.string.currentVersionNA);
			e.printStackTrace();
		}
		
		ListView listview = (ListView) findViewById(R.id.infoList);
	    String[] values = new String[] { getString(R.string.shareWithFriends), getString(R.string.sendFeedback), getString(R.string.rateOnPlay), getString(R.string.website), getString(R.string.tos), versionName };

	    ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < values.length; ++i) {
	      list.add(values[i]);
	    }
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	        R.layout.info_item_layout, R.id.info_item, list);
	    
	    View header = getLayoutInflater().inflate(R.layout.info_header, null);
	    listview.addHeaderView(header, null, false);
	    listview.setAdapter(adapter);
	   
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view,
	            int position, long id) {
	        	
	        	switch(position) {
	        	case 1:
	        		Intent shareWithFriendsIntent = new Intent();
	        		shareWithFriendsIntent.setAction(Intent.ACTION_SEND);
	        		shareWithFriendsIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMarketingText)); 
	        		shareWithFriendsIntent.setType("text/plain");
	        		startActivity(shareWithFriendsIntent);
	        		break;
	        	case 2:
	        		Intent feedBackIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
    		                "mailto","contaktsapp@gmail.com", null));
	        		feedBackIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\nFeedback sent from Contakts for Android.");
    		    	startActivity(feedBackIntent);
	        		break;
	        	case 3:
	        		final String appPackageName = getPackageName();
	        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
	        		break;
	        	case 4:
	        		String url = "http://www.contaktsapp.com";
	        		Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
	        		websiteIntent.setData(Uri.parse(url));
	        		startActivity(websiteIntent);
	        		break;
	        	case 5:
	        		String urlTOS = "http://www.contaktsapp.com/tos";
	        		Intent websiteTOSIntent = new Intent(Intent.ACTION_VIEW);
	        		websiteTOSIntent.setData(Uri.parse(urlTOS));
	        		startActivity(websiteTOSIntent);
                	break;
        		default:
        			break;
	        	}
	        }
	    });
	}
	       
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.info, menu);
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(InfoActivity.this, FavActivity.class);
		   	InfoActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(InfoActivity.this, RecentActivity.class);
		   InfoActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(InfoActivity.this, GraphActivity.class);
	   		InfoActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(InfoActivity.this, MainActivity.class);
	   		InfoActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
		   	Intent fbIntent = new Intent(InfoActivity.this, GroupActivity.class);
		   	InfoActivity.this.startActivity(fbIntent);
	   }  else if (selected == 5) {
		   	Intent loIntent = new Intent(InfoActivity.this, ShuffleActivity.class);
		   	InfoActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(InfoActivity.this, FBActivity.class);
		   	InfoActivity.this.startActivity(iIntent);
	   }   else if (selected == 7) {
		   	Intent iIntent = new Intent(InfoActivity.this, LoginActivity.class);
		   	InfoActivity.this.startActivity(iIntent);
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
	  
	  @Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();

	  }
}
