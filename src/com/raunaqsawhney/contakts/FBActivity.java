package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FBActivity extends Activity implements OnItemClickListener {
	
	String theme = "#34AADC";
	String font = "RobotoCondensed-Regular.ttf";
	
    ArrayList<fbFriend> friendList = new ArrayList<fbFriend>();

	private ListView navListView;
	private SlidingMenu menu;


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
				"Facebook Friends",
				"Twitter",
				"LinkedIn Connections"
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_twitter,
				R.drawable.ic_action_linkedin_512
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
        
        
        startFB();
	}
	

	private void startFB() {
		
		String fqlQuery = "select uid, name, pic_big, is_app_user, online_presence from user where uid in (select uid2 from friend where uid1 = me())";
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		
		// start Facebook Login
	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	      // callback when session changes state
	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	        if (session.isOpened()) {

	        	Request request = new Request(session, 
	        		    "/fql", 
	        		    params, 
	        		    HttpMethod.GET, 
	        		    new Request.Callback(){ 
	        		        public void onCompleted(Response response) {
	        		        Log.i("FB", "Got results: " + response.toString());
	        		        
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
							            
							            String id     = json_obj.getString( "uid"           );
							            String name   = json_obj.getString( "name"          );
							            String urlImg = json_obj.getString( "pic_big"    );
							            String presence = json_obj.getString( "online_presence"    );

							            friend.setID(id);
							            friend.setName(name);
							            friend.setURL(urlImg);
							            friend.setPresence(presence);
							            							            
							            friendList.add(friend);
							         
							            System.out.println("URI:" + Uri.parse(urlImg));
							            
							        }
							        // Create the adapter to convert the array to views
						            FriendAdapter adapter = new FriendAdapter(FBActivity.this, friendList);
						            // Attach the adapter to a ListView
						            ListView fbListView = (ListView) findViewById(R.id.fbList);

						    	    View header = getLayoutInflater().inflate(R.layout.fb_header, null);
						            fbListView.addHeaderView(header);
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
	      }
	    });
	}


	@Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
	   } else if (selected == 5) {
	   		Intent liIntent = new Intent(FBActivity.this, LinkedInActivity.class);
	   		FBActivity.this.startActivity(liIntent);
	   }	
		//TODO: ADD TWITTER
	}
}
