package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.FacebookDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FBActivity extends Activity implements OnItemClickListener  {
	
	FriendAdapter adapter;
	private UiLifecycleHelper uiHelper;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
		
	
    ArrayList<fbFriend> friendList = new ArrayList<fbFriend>();

	private ListView navListView;
	private SlidingMenu menu;
	
	String uid;
	String name;
	String urlImg;
	Boolean isAppUser;
	String coverUrl;
    String username;
	String birthday;
	String current_loc_city;
	String current_loc_state;
	String current_loc_country;
	String current_home_city;
	String current_home_state;
	String current_home_country;
	private boolean firstRunDoneFB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fb);
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
		
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		startfb();
		
	}
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
        theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);		
        
        firstRunDoneFB = prefs.getBoolean("firstRunDoneFB", false);
        if (!firstRunDoneFB) {
        	edit.putBoolean("firstRunDoneFB", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle("Facebook")
		    .setMessage("This is your first time accessing Facebook. Please go to Settings and login.")
		    		.setNeutralButton("Okay", null)
		    .show();
        }
       
	}

	private void setupActionBar() {
		
		// Set up the Action Bar
        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView actionBarTitleText = (TextView) findViewById(titleId);
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(false);
       
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

	private void startfb() {
		String fqlQuery = "select uid, name, pic_big, is_app_user from user where uid in (select uid2 from friend where uid1 = me()) order by name";
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		
		System.out.println("startfb started, done fqlquery building");
		
		Session session = Session.getActiveSession();
		System.out.println("got active session");

		if (session == null) {
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("You have not logged into a Facebook account.")
		    .setNeutralButton("Settings", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	Intent settingIntent = new Intent(FBActivity.this, LoginActivity.class);
		        	FBActivity.this.startActivity(settingIntent);
		        }
		    })
		    .show();
		}
		
		Request request = new Request(session, 
    		    "/fql", 
    		    params, 
    		    HttpMethod.GET, 
    		    new Request.Callback(){ 
					public void onCompleted(Response response) {
    		        parseResponse(response);
    		    }
					private void parseResponse(Response response) {
						try
					    {
					        GraphObject go  = response.getGraphObject();
					        JSONObject  jso = go.getInnerJSONObject();
					        JSONArray   arr = jso.getJSONArray( "data" );

					        for ( int i = 0; i < ( arr.length() ); i++ )
					        {
					            fbFriend friend = new fbFriend();
					        	
					            JSONObject json_obj = arr.getJSONObject( i );
					            
					            uid     = json_obj.getString("uid");
					            name   	= json_obj.getString("name");
					            urlImg 	= json_obj.getString("pic_big");
					            isAppUser = json_obj.getBoolean("is_app_user");
					            
					            friend.setID(uid);
					            friend.setName(name);
					            friend.setURL(urlImg);
					            friend.setIsAppUser(isAppUser);
					            							            
					            friendList.add(friend);			            
					        }
					        
					    	adapter = new FriendAdapter(FBActivity.this, friendList);

					    	ListView  fbListView = (ListView) findViewById(R.id.fbList);

				    	    View header = getLayoutInflater().inflate(R.layout.fb_header, null);
				            fbListView.addHeaderView(header, null, false);
				            
				            fbListView.setOnItemClickListener(new OnItemClickListener() {
				                @Override
				                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    				
				    				fbFriend selectedFriend = new fbFriend();
				    				
				    				selectedFriend = (fbFriend) parent.getItemAtPosition(position);
				                    Intent intent = new Intent(getApplicationContext(), FriendDetailActivity.class);
				                    intent.putExtra("friend_id", selectedFriend.getID());

				                    startActivity(intent);
				                }
				            });
				            

				            fbListView.setAdapter(adapter);
					    }
					    catch ( Throwable t )
					    {
					        t.printStackTrace();
					    }								
					}
    		});
    		Request.executeBatchAsync(request);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.fb, menu);
	    
        
        
        return super.onCreateOptionsMenu(menu);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FBActivity.this, FavActivity.class);
		   	FBActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(FBActivity.this, FrequentActivity.class);
		   FBActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(FBActivity.this, MainActivity.class);
	   		FBActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(FBActivity.this, GoogleActivity.class);
	   		FBActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(FBActivity.this, FBActivity.class);
	   		FBActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(FBActivity.this, LoginActivity.class);
		   	FBActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(FBActivity.this, InfoActivity.class);
		   	FBActivity.this.startActivity(iIntent);
	   } 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.fb_logout:
	        	logoutFromFB();
	        	
	        	Intent fbLogoutIntent = new Intent(FBActivity.this, MainActivity.class);
	        	FBActivity.this.startActivity(fbLogoutIntent);
	            return true; 
	            
	        case R.id.fb_publish:
	        	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
	            .setLink("https://play.google.com/store/apps/details?id=com.raunaqsawhney.contakts")
	            .build();
			    uiHelper.trackPendingDialogCall(shareDialog.present());
			    return true;
	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void logoutFromFB() {
		Session session = Session.getActiveSession();
	    if (session != null) {

	        if (!session.isClosed()) {
	            session.closeAndClearTokenInformation();
	            //clear your preferences if saved
	        }
	    } else {

	        session = new Session(getBaseContext());
	        Session.setActiveSession(session);

	        session.closeAndClearTokenInformation();
	            //clear your preferences if saved
	    }
	    
	    Toast.makeText(getApplicationContext(), 
	               "Logged out of Facebook.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            //Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            //Log.i("Activity", "Success!");
	        }
	    });
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
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
