package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
import com.facebook.model.GraphObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FBActivity extends Activity implements OnItemClickListener {
	
	String theme = "#34AADC";
	String font = "RobotoCondensed-Regular.ttf";
	
    ArrayList<fbFriend> friendList = new ArrayList<fbFriend>();

	private ListView navListView;
	private SlidingMenu menu;
	
	String uid;
	String name;
	String urlImg;
	String coverUrl;
    String username;
	String birthday;
	String current_loc_city;
	String current_loc_state;
	String current_loc_country;
	String current_home_city;
	String current_home_state;
	String current_home_country;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fb);
		
       		
		// Set up the Action Bar
        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView actionBarTitleText = (TextView) findViewById(titleId);
        actionBarTitleText.setTypeface(Typeface.createFromAsset(getAssets(), "Harabara.ttf"));
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
				"Phone Contacts",
				"Google Contacts",
				"Facebook",
				"Settings"
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
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
        
		startfb();
	}
	

	
	private void startfb() {
		String fqlQuery = "select uid, name, pic_big, pic_cover, username, birthday, current_location, hometown_location, work_history, education_history from user where uid in (select uid2 from friend where uid1 = me()) order by name";
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		
		System.out.println("startfb started, done fqlquery building");
		
		Session session = Session.getActiveSession();
		System.out.println("got active session");

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
					            username = json_obj.getString("username");
					            
					            try {
						            birthday = json_obj.getString("birthday");
						            current_loc_city = json_obj.getJSONObject("current_location").getString("city");
						            current_loc_state = json_obj.getJSONObject("current_location").getString("state");
						            current_loc_country = json_obj.getJSONObject("current_location").getString("country");
						            current_home_city = json_obj.getJSONObject("hometown_location").getString("city");
						            current_home_state = json_obj.getJSONObject("hometown_location").getString("state");
						            current_home_country = json_obj.getJSONObject("hometown_location").getString("country");
						            coverUrl = json_obj.getJSONObject("pic_cover").getString("source");

					            } catch (JSONException e) {
					            	//Log.d("JSON", "NULL ITEM");
					            }
					            
					            
					            friend.setID(uid);
					            friend.setName(name);
					            friend.setURL(urlImg);
					            friend.setCoverUrl(coverUrl);
					            friend.setUsername(username);
					            friend.setBirthday(birthday);
					            friend.setCurrentLocCity(current_loc_city);
					            friend.setCurrentLocState(current_loc_state);
					            friend.setCurrentLocCountry(current_loc_country);
					            friend.setCurrentHomeCity(current_home_city);
					            friend.setCurrentHomeState(current_home_state);
					            friend.setCurrentHomeCountry(current_home_country);
					            							            
					            friendList.add(friend);			            
					        }
					        
				            FriendAdapter adapter = new FriendAdapter(FBActivity.this, friendList);
				            ListView fbListView = (ListView) findViewById(R.id.fbList);

				    	    View header = getLayoutInflater().inflate(R.layout.fb_header, null);
				            fbListView.addHeaderView(header);
				            
				            fbListView.setOnItemClickListener(new OnItemClickListener() {
				                @Override
				                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    				
				    				fbFriend selectedFriend = new fbFriend();
				    				
				    				selectedFriend = (fbFriend) parent.getItemAtPosition(position);
				                    Intent intent = new Intent(getApplicationContext(), FriendDetailActivity.class);
				                    intent.putExtra("friend_id", selectedFriend.getID());
				                    intent.putExtra("friend_name", selectedFriend.getName());
				                    intent.putExtra("friend_imgurl", selectedFriend.getURL());
				                    intent.putExtra("friend_coverUrl", selectedFriend.getCoverUrl());
				                    intent.putExtra("friend_username", selectedFriend.getUsername());
				                    intent.putExtra("friend_birthday", selectedFriend.getBirthday());
				                    intent.putExtra("friend_loc_city", selectedFriend.getCurrentLocCity());
				                    intent.putExtra("friend_loc_state", selectedFriend.getCurrentLocState());
				                    intent.putExtra("friend_loc_country", selectedFriend.getCurrentLocCountry());
				                    intent.putExtra("friend_home_city", selectedFriend.getCurrentHomeCity());
				                    intent.putExtra("friend_home_state", selectedFriend.getCurrentHomeState());
				                    intent.putExtra("friend_home_country", selectedFriend.getCurrentHomeCountry());

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
	    return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent stIntent = new Intent(FBActivity.this, FavActivity.class);
		   	FBActivity.this.startActivity(stIntent);
	   } else if (selected == 1) {
		   Intent pIntent = new Intent(FBActivity.this, MainActivity.class);
		   FBActivity.this.startActivity(pIntent);
	   } else if (selected == 2) {
	   		Intent gIntent = new Intent(FBActivity.this, GoogleActivity.class);
	   		FBActivity.this.startActivity(gIntent);
	   } else if (selected == 3) {
	   		Intent fbIntent = new Intent(FBActivity.this, FBActivity.class);
	   		FBActivity.this.startActivity(fbIntent);
	   } else if (selected == 4) {
	   		Intent liIntent = new Intent(FBActivity.this, LoginActivity.class);
	   		FBActivity.this.startActivity(liIntent);
	   }	
		//TODO: ADD TWITTER
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
}
