package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class LoginActivity extends FragmentActivity implements OnItemClickListener {

	// Declare Globals
	String font;
	String fontContent;
	String fontTitle;
	String theme;
		
	private SlidingMenu menu;
	private ListView navListView;
	
	SharedPreferences preferences;
	Editor edit;
	
	/*
	IInAppBillingService mService;

	ServiceConnection mServiceConn = new ServiceConnection() {
	   @Override
	   public void onServiceDisconnected(ComponentName name) {
	       mService = null;
	   }

	   @Override
	   public void onServiceConnected(ComponentName name, 
	      IBinder service) {
	       mService = IInAppBillingService.Stub.asInterface(service);
	   }
	};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Session.openActiveSessionFromCache(getBaseContext());
		Session.openActiveSession(this, false, null);
		
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		setupColorPref();
		setupFBLogin();
		enableAds();
		
        
        //TODO: In- App Purchasing
        //bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
 	}
	
	private void enableAds() {
    	AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest request = new AdRequest.Builder()
	    .addTestDevice("0354E8ED4FC960988640B5FD3E894FAF")
	    .addKeyword("games")
	    .addKeyword("apps")
	    .addKeyword("social")
	    .build();
	    adView.loadAd(request);			
	}

	private void setupFBLogin() {
		
		LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
		authButton.setOnErrorListener(new OnErrorListener() {
	   
			@Override
			public void onError(FacebookException error) {
			}
		});
			authButton.setReadPermissions(Arrays.asList("basic_info",
					"friends_birthday",
					"friends_hometown",
					"friends_location",
					"friends_work_history",
					"friends_education_history"));
		
			authButton.setSessionStatusCallback(new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.isOpened()) {
						// DO NOTHING, Return to Settings/Login Activity
					}
				}
			});		
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
        bar.setDisplayHomeAsUpEnabled(true);

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
	
	private void setupColorPref() {
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		edit = preferences.edit();
		
		Button colorPicker = new Button(this);
		colorPicker = (Button) findViewById(R.id.colorPicker);
		colorPicker.setBackgroundColor(Color.parseColor(theme));
		
    	final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
		colorPickerDialog.initialize(R.string.color_dialog_title, new int[] { 
				Color.parseColor("#34AADC"),
				Color.parseColor("#8E8E93"),
				Color.parseColor("#FF2D55"),
				Color.parseColor("#FF3B30"),
				Color.parseColor("#FF9500"),
				Color.parseColor("#FFCC00"),
				Color.parseColor("#4CD964"),
				Color.parseColor("#007AFF"),
				Color.parseColor("#5856D6")}, Color.parseColor(theme), 3, 2);
		
		colorPickerDialog.setOnColorSelectedListener(new OnColorSelectedListener() {

			@Override
			public void onColorSelected(int color) {
				
				String themeColor = null;
				
				if (color == -13325604) {
					themeColor = "#34AADC";
				} else if (color == -7434605 ) {
					themeColor = "#8E8E93";
				} else if (color == -53931) {
					themeColor = "#FF2D55";
				} else if (color == -13312 ) {
					themeColor = "#FFCC00";
				} else if (color == -27392 ) {
					themeColor = "#FF9500";
				} else if (color == -50384) {
					themeColor = "#FF3B30";
				} else if (color == -11740828 ) {
					themeColor = "#4CD964";
				} else if (color == -16745729) {
					themeColor = "#007AFF";
				} else if (color == -10987818) {
					themeColor = "#5856D6";
				}
				
				edit.putString("theme", themeColor);
				edit.apply(); 
								
			   	Intent goBackToMain = new Intent(LoginActivity.this, MainActivity.class);
			   	goBackToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				LoginActivity.this.startActivity(goBackToMain);		
			}
		});
		
		colorPicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	colorPickerDialog.show(getSupportFragmentManager(), "colorpicker");
            }
        });		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	     Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	 }
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(LoginActivity.this, FavActivity.class);
		   	LoginActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(LoginActivity.this, FrequentActivity.class);
		   LoginActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(LoginActivity.this, MainActivity.class);
	   		LoginActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(LoginActivity.this, GoogleActivity.class);
	   		LoginActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(LoginActivity.this, FBActivity.class);
	   		LoginActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(LoginActivity.this, LoginActivity.class);
		   	LoginActivity.this.startActivity(loIntent);
	   } else if (selected == 6) {
		   	Intent iIntent = new Intent(LoginActivity.this, InfoActivity.class);
		   	LoginActivity.this.startActivity(iIntent);
	   } 
	}
	
	/*
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mService != null) {
	        unbindService(mServiceConn);
	    }   
	}
	*/
	
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
