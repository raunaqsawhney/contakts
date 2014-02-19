package com.raunaqsawhney.contakts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
	String theme;

	private ListView navListView;
	private SlidingMenu menu;
	
	private String friend_id;
	private String name;
	private String urlImg;
	private String username;
	private String birthday;
	private String birthday_date;
	private String current_loc_city;
	private String current_loc_state;
	private String current_loc_country;
	private String current_home_city;
	private String current_home_state;
	private String current_home_country;
	private String coverUrl;
	private Boolean isAppUser;

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
	TextView friend_is_app_user;
	
	int eduCount = 0;
	int workCount = 0;
	
	boolean isThereEducation = false;
	boolean isThereWork = false;
	
	Calendar calendar = Calendar.getInstance(); 
	
	Session.OpenRequest openRequest = null;
	private boolean firstRunDoneFreDet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);
		
		setupGlobalPrefs();
		setupActionBar();
		
		
		TextView username = (TextView) findViewById(R.id.f_detail_username_header);
		username.setTextColor(Color.parseColor(theme));
		
		TextView birthday = (TextView) findViewById(R.id.f_detail_birthday_header);
		birthday.setTextColor(Color.parseColor(theme));
		
		TextView curLoc = (TextView) findViewById(R.id.f_detail_currentloc_header);
		curLoc.setTextColor(Color.parseColor(theme));
		
		TextView hometown = (TextView) findViewById(R.id.f_detail_hometown_header);
		hometown.setTextColor(Color.parseColor(theme));
		
		TextView work = (TextView) findViewById(R.id.f_detail_work_header);
		work.setTextColor(Color.parseColor(theme));
		
		TextView education = (TextView) findViewById(R.id.f_detail_education_header);
		education.setTextColor(Color.parseColor(theme));
		
		
		
		
		
		
		setupSlidingMenu();
		setupImageLoader();
		fetchFriendInfo();
		enableAds();
       
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
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneFreDet = prefs.getBoolean("firstRunFreDet", false);
        if (!firstRunDoneFreDet) {
        	edit.putBoolean("firstRunFreDet", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle("Friend Details")
		    .setMessage("Here you can see all data associated with a Facebook friend. Simply tap on any item and learn more about it.")
		    		.setNeutralButton("Okay", null)
		    .show();
        }	
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(21);
        
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
	
	private void setupImageLoader() {
		
        this.imageLoader = ImageLoader.getInstance();
        
        options = new DisplayImageOptions.Builder()
       .imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default
       .build();
       
       
       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext())
       .discCacheExtraOptions(100, 100, CompressFormat.PNG, 100, null)
       .build();
       imageLoader.init(config);
              
       friend_id = getIntent().getStringExtra("friend_id");		
	}

	private void fetchFriendInfo() {
		
		String fqlQuery = "select name, is_app_user, first_name, pic_big, pic_cover, username, birthday, birthday_date, current_location, hometown_location, work_history, education_history from user where uid = " + friend_id;
		
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		
		Session session = Session.getActiveSession();

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
					            
					            name   	= json_obj.getString("name");
					            String first_name = json_obj.getString("first_name");
					            urlImg 	= json_obj.getString("pic_big");
					            username = json_obj.getString("username");
					            isAppUser = json_obj.getBoolean("is_app_user");
					            
					            try {
						            birthday = json_obj.getString("birthday");
						            birthday_date = json_obj.getString("birthday_date");
						            if (birthday != "null") {
						            	LinearLayout birthdayLayout = (LinearLayout) findViewById(R.id.f_detail_birthday_layout);
						            	birthdayLayout.setVisibility(View.VISIBLE);

						            	SimpleDateFormat format1 = new SimpleDateFormat("MM/dd");
						            	Date dt1 = format1.parse(birthday_date);
 
						            	String month = (String) android.text.format.DateFormat.format("MM", dt1); 
						            	String day = (String) android.text.format.DateFormat.format("dd", dt1); 
						            	
						            	String friendBirthday = month+"/"+day;
						            	
						            	Calendar c = Calendar.getInstance();
						            	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
						            	String todayDate = sdf.format(c.getTime());
						            							            	
						            	if (todayDate.equalsIgnoreCase(friendBirthday))
						            	{
						            		LinearLayout birthdayIconLayout = (LinearLayout) findViewById(R.id.f_detail_header_birthday_layout);
						            		birthdayIconLayout.setVisibility(View.VISIBLE);
						            		
						            		TextView birthdayText = (TextView) findViewById(R.id.f_detail_header_birthday_text);
						            		birthdayText.setText("Wish " + first_name + " a Happy Birthday!");
						            		
						            		birthdayText.setOnClickListener(new OnClickListener() {
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
					    		friend_is_app_user = (TextView) findViewById(R.id.f_detail_header_isappuser);
					    		
					    		if (isAppUser)
						    		friend_is_app_user.setText("on Contakts");
					    		else 
						    		friend_is_app_user.setText("");
					    		
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
					    			
					    			eduTextView.setOnClickListener(new OnClickListener() {
						    	        @Override
						    	        public void onClick(View v) {
						    	        	String url = eduTextView.getText().toString();
						    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
						    	        	    url = "http://www.google.com/#q=" + url;
						    	        	}
						    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						    	        	startActivity(openUrlIntent);
						    	        }
						    	    });
					    			
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
					    			
					    			workTextView.setOnClickListener(new OnClickListener() {
						    	        @Override
						    	        public void onClick(View v) {
						    	        	String url = workTextView.getText().toString();
						    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
						    	        	    url = "http://www.google.com/#q=" + url;
						    	        	}
						    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						    	        	startActivity(openUrlIntent);
						    	        }
						    	    });
					    			
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
					    			curLocLayout.setVisibility(View.GONE);
					    		} else {
					    			curLocLayout.setVisibility(View.VISIBLE);
						    		friend_curloc_tv.setText(friend.getCurrentLocCity() + friend.getCurrentLocState() + friend.getCurrentLocCountry());
					    		}
					    		
					    		LinearLayout homeLayout = (LinearLayout) findViewById(R.id.f_detail_hometown_layout);
					    		
					    		if (friend.getCurrentHomeCity() == "" || friend.getCurrentHomeState() == "" || friend.getCurrentHomeCountry() == "") {
					    			homeLayout.setVisibility(View.GONE);
					    		} else {
					    			homeLayout.setVisibility(View.VISIBLE);
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
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FriendDetailActivity.this, FavActivity.class);
		   	FriendDetailActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(FriendDetailActivity.this, FrequentActivity.class);
		   FriendDetailActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(FriendDetailActivity.this, MainActivity.class);
	   		FriendDetailActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(FriendDetailActivity.this, GoogleActivity.class);
	   		FriendDetailActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(FriendDetailActivity.this, FBActivity.class);
	   		FriendDetailActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(FriendDetailActivity.this, LoginActivity.class);
		   	FriendDetailActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(FriendDetailActivity.this, InfoActivity.class);
		   	FriendDetailActivity.this.startActivity(iIntent);
	   } 
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO Auto-generated method stub
		
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
