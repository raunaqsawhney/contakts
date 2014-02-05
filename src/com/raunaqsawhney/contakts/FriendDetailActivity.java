package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FriendDetailActivity extends Activity implements OnItemClickListener, StatusCallback {
	
	String font;
	String fontContent;
	String fontTitle;

	private ListView navListView;
	private SlidingMenu menu;
	
	private String friend_id;
	private String name;
	private String urlImg;
	private String username;
	private String birthday;
	private String current_loc_city;
	private String current_loc_state;
	private String current_loc_country;
	private String current_home_city;
	private String current_home_state;
	private String current_home_country;
	private String coverUrl;

	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	ArrayList<fbFriend> friendList = new ArrayList<fbFriend>();
	ArrayList<String> educationHistory = new ArrayList<String>();
	ArrayList<String> workHistory = new ArrayList<String>();

	
	String friendName;
	String friendPhotoUri;
	String friendIsAppUser;
	String friendCoverPhotoUri;
	String friendUserName;
	
	
	TextView friend_name_tv; 
	TextView friend_username_tv;
	TextView friend_birthday_tv;
	TextView friend_curloc_tv;
	TextView friend_hometown_tv;
	ImageView friend_imgurl_iv;
	ImageView friend_cover_iv;
	
	int eduCount = 0;
	int workCount = 0;
	
	boolean isThereEducation = false;
	boolean isThereWork = false;

	
	Session.OpenRequest openRequest = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);
		
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);
        
        
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(21);
        
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
                
        this.imageLoader = ImageLoader.getInstance();
        
        options = new DisplayImageOptions.Builder()
       .imageScaleType(ImageScaleType.NONE) // default
       .build();
       
       
       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext())
       .discCacheExtraOptions(100, 100, CompressFormat.PNG, 100, null)
       .build();
       imageLoader.init(config);
       
       friend_id = getIntent().getStringExtra("friend_id");
       fetchFriendInfo();
       
	}
	
	private void fetchFriendInfo() {
		
		String fqlQuery = "select name, pic_big, pic_cover, username, birthday, current_location, hometown_location, work_history, education_history from user where uid = " + friend_id;
		System.out.println(fqlQuery);
		
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
							System.out.println(response.toString());
					        GraphObject go  = response.getGraphObject();
					        JSONObject  jso = go.getInnerJSONObject();
					        JSONArray   arr = jso.getJSONArray( "data" );

					        for ( int i = 0; i < ( arr.length() ); i++ )
					        {
					            fbFriend friend = new fbFriend();
					        	
					            JSONObject json_obj = arr.getJSONObject( i );
					            
					            name   	= json_obj.getString("name");
					            urlImg 	= json_obj.getString("pic_big");
					            username = json_obj.getString("username");
					            
					            try {
						            birthday = json_obj.getString("birthday");
						            if (birthday != "null") {
						            	LinearLayout birthdayLayout = (LinearLayout) findViewById(R.id.f_detail_birthday_layout);
						            	birthdayLayout.setVisibility(View.VISIBLE);
						            }
					            } catch (JSONException e) {
					            	// Handled by not showing the view
					            }
					            
					            try {
						            current_loc_city = json_obj.getJSONObject("current_location").getString("city") + ", ";
					            } catch (JSONException e) {
					            	current_loc_city = "";
					            }
					            
					            try {
						            current_loc_state = json_obj.getJSONObject("current_location").getString("state") + ", ";
					            } catch (JSONException e) {
					            	current_loc_state = "";
					            }
					            
					            try {
						            current_loc_country = json_obj.getJSONObject("current_location").getString("country");
					            } catch (JSONException e) {
					            	current_loc_country = "";
					            } 
					            
					            try {
						            current_home_city = json_obj.getJSONObject("hometown_location").getString("city") + ", ";
					            } catch (JSONException e) {
					            	current_home_city = "";
					            }
					            
					            try {
						            current_home_state = json_obj.getJSONObject("hometown_location").getString("state") + ", ";
					            } catch (JSONException e) {
					            	current_home_state = "";
					            }
					            
					            try {
						            current_home_country = json_obj.getJSONObject("hometown_location").getString("country");
					            } catch (JSONException e) {
					            	current_home_country = "";
					            }
					            
					            try {
						            coverUrl = json_obj.getJSONObject("pic_cover").getString("source");
					            } catch (JSONException e) {
					            	coverUrl = "http://farm4.staticflickr.com/3793/9601614175_f989049ff8_z.jpg";
					            }
					            
					            LinearLayout eduLayout = (LinearLayout) findViewById(R.id.f_detail_education_layout);
					            
					            try {
					            	String currSchool;
					            	JSONObject currHistory;
					            	JSONArray eduArray = json_obj.getJSONArray("education_history");
					            	int length = eduArray.length();
					            	if (length > 0)
						            	eduLayout.setVisibility(View.VISIBLE);
					            	
					            	for (int j = 0; j < length; j++) {
					            		currHistory = eduArray.getJSONObject(j);
					            		currSchool = currHistory.getString("name");
					            		
					            		System.out.println("Added " + currSchool);
					            		educationHistory.add(currSchool);
					            		eduCount++;
					            	}
					            } catch (JSONException e) {
					            	// Handled by not showing the layout
					            }
					            
					            LinearLayout workLayout = (LinearLayout) findViewById(R.id.f_detail_workhistory_layout);

					            try {
					            	String currWork;
					            	JSONObject currWorkHistory;
					            	JSONArray workArray = json_obj.getJSONArray("work_history");
					            	int length = workArray.length();
					            	if (length > 0)
						            	workLayout.setVisibility(View.VISIBLE);
					            	
					            	for (int j = 0; j < length; j++) {
					            		currWorkHistory = workArray.getJSONObject(j);
					            		currWork = currWorkHistory.getString("company_name");
					            		
					            		System.out.println("Added " + currWork);
					            		workHistory.add(currWork);
					            		workCount++;
					            	}
					            } catch (JSONException e) {
					            	// Handled by not showing the layout
					            }
 					            
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
					            
					    		friend_name_tv = (TextView) findViewById(R.id.f_detail_header_name);
					    		friend_username_tv = (TextView) findViewById(R.id.f_detail_username_content);
					    		friend_birthday_tv = (TextView) findViewById(R.id.f_detail_birthday_content);
					    		friend_curloc_tv = (TextView) findViewById(R.id.f_detail_currentloc_content);
					    		friend_hometown_tv = (TextView) findViewById(R.id.f_detail_hometown_content);
					    		friend_imgurl_iv = (ImageView) findViewById(R.id.f_detail_header_photo);
					    		friend_cover_iv = (ImageView) findViewById(R.id.cover_photo);

				    			final TextView[] eduTextViews = new TextView[eduCount];
					    		for (int k = 0; k < eduCount; k++) {
					    			final TextView eduTextView = new TextView(getBaseContext());
					    			
					    			eduTextView.setText(educationHistory.get(k));
					    			eduTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
					    			eduTextView.setTextSize(18);
					    			eduTextView.setSingleLine();
					    			eduTextView.setEllipsize(TextUtils.TruncateAt.END);
					    			eduTextView.setTextColor(Color.BLACK);
					    			eduTextView.setPadding(60, 10, 0, 10);
					    			
					    			eduLayout.addView(eduTextView);
					    			eduTextViews[k] = eduTextView;
					    		}

				            	final TextView[] workTextViews = new TextView[workCount];
					    		for (int m = 0; m < workCount; m++) {
					    			final TextView workTextView = new TextView(getBaseContext());
					    			
					    			workTextView.setText(workHistory.get(m));
					    			workTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
					    			workTextView.setTextSize(18);
					    			workTextView.setSingleLine();
					    			workTextView.setEllipsize(TextUtils.TruncateAt.END);
					    			workTextView.setTextColor(Color.BLACK);
					    			workTextView.setPadding(60, 10, 0, 10);
					    			
					    			workLayout.addView(workTextView);
					    			workTextViews[m] = workTextView;
					    		}

						    	friend_name_tv.setText(friend.getName());
					    		ActionBar ab = getActionBar();
					            ab.setTitle(friend.getName());
					    		
					    		friend_username_tv.setText(friend.getUsername());
					    		friend_birthday_tv.setText(friend.getBirthday());
					    		
					    		LinearLayout curLocLayout = (LinearLayout) findViewById(R.id.f_detail_currentloc_layout);
					    		
					    		if (friend.getCurrentLocCity() == "" || friend.getCurrentLocState() == "" || friend.getCurrentLocCountry() == "") {
					    			System.out.println(friend.getCurrentLocCity() + friend.getCurrentLocState() + friend.getCurrentLocCountry() + "GONE");
					    			curLocLayout.setVisibility(View.GONE);
					    		} else {
					    			curLocLayout.setVisibility(View.VISIBLE);
					    			System.out.println(friend.getCurrentLocCity() + friend.getCurrentLocState() + friend.getCurrentLocCountry() + "VISIBLE");
						    		friend_curloc_tv.setText(friend.getCurrentLocCity() + friend.getCurrentLocState() + friend.getCurrentLocCountry());
					    		}
					    		
					    		LinearLayout homeLayout = (LinearLayout) findViewById(R.id.f_detail_hometown_layout);
					    		
					    		if (friend.getCurrentHomeCity() == "" || friend.getCurrentHomeState() == "" || friend.getCurrentHomeCountry() == "") {
					    			System.out.println(friend.getCurrentHomeCity() + friend.getCurrentHomeState() + friend.getCurrentHomeCountry() + "GONE");
					    			homeLayout.setVisibility(View.GONE);
					    		} else {
					    			homeLayout.setVisibility(View.VISIBLE);
					    			System.out.println(friend.getCurrentHomeCity() + friend.getCurrentHomeState() + friend.getCurrentHomeCountry() + "VISIBLE");
						    		friend_hometown_tv.setText(friend.getCurrentHomeCity() + friend.getCurrentHomeState() + friend.getCurrentHomeCountry());
					    		}
					    	    
					    		imageLoader.displayImage(friend.getCoverUrl(), friend_cover_iv, options);
					    	    imageLoader.displayImage(friend.getURL(), friend_imgurl_iv, options);
					    	    
					    	    friend_username_tv.setOnClickListener(new OnClickListener() {
					    	        @Override
					    	        public void onClick(View v) {
					    	        	String url = friend_username_tv.getText().toString();
					    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
					    	        	    url = "http://www.facebook.com/" + url;
					    	        	}
					    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					    	        	startActivity(openUrlIntent);
					    	        }
					    	    });							            
					        }
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friend_detail, menu);
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent stIntent = new Intent(FriendDetailActivity.this, FavActivity.class);
		   	FriendDetailActivity.this.startActivity(stIntent);
	   } else if (selected == 1) {
		   Intent pIntent = new Intent(FriendDetailActivity.this, MainActivity.class);
		   FriendDetailActivity.this.startActivity(pIntent);
	   } else if (selected == 2) {
	   		Intent gIntent = new Intent(FriendDetailActivity.this, GoogleActivity.class);
	   		FriendDetailActivity.this.startActivity(gIntent);
	   } else if (selected == 3) {
	   		Intent fbIntent = new Intent(FriendDetailActivity.this, FBActivity.class);
	   		FriendDetailActivity.this.startActivity(fbIntent);
	   } else if (selected == 4) {
	   		Intent liIntent = new Intent(FriendDetailActivity.this, LoginActivity.class);
	   		FriendDetailActivity.this.startActivity(liIntent);
	   }	
		//TODO: ADD TWITTER
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO Auto-generated method stub
		
	}
}
