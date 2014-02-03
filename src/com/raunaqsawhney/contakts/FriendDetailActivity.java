package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
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
	
	String theme = "#34AADC";
	String font = "RobotoCondensed-Regular.ttf";
	
	private ListView navListView;
	private SlidingMenu menu;
	
	private String friend_name;
	private String friend_imgurl;
	private String friend_coverUrl;
	private String friend_birthday;
	private String friend_username;
	private String friend_loc_city;
	private String friend_loc_state;
	private String friend_loc_country;
	private String friend_home_city;
	private String friend_home_state;
	private String friend_home_country;

	
	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	String friendName;
	String friendPhotoUri;
	String friendIsAppUser;
	String friendCoverPhotoUri;
	String friendUserName;
	
	TextView friendNameTV;
	ImageView friendPhotoUriIV;
	ImageView friendCoverPhotoUriIV;
	TextView friendUsernameTV;
	
	Session.OpenRequest openRequest = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf"));
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
       .showImageOnLoading(R.drawable.ic_contact_picture)
       .imageScaleType(ImageScaleType.NONE) // default
       .build();
       
       
       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext())
       .discCacheExtraOptions(100, 100, CompressFormat.PNG, 100, null)
       .build();
       imageLoader.init(config);
       
       friend_name = getIntent().getStringExtra("friend_name");
       friend_imgurl = getIntent().getStringExtra("friend_imgurl");
       friend_coverUrl = getIntent().getStringExtra("friend_coverUrl");
       friend_username = getIntent().getStringExtra("friend_username");
       friend_birthday = getIntent().getStringExtra("friend_birthday");
       friend_loc_city = getIntent().getStringExtra("friend_loc_city");
       friend_loc_state = getIntent().getStringExtra("friend_loc_city");
       friend_loc_country = getIntent().getStringExtra("friend_loc_country");
       friend_home_city = getIntent().getStringExtra("friend_home_city");
       friend_home_state = getIntent().getStringExtra("friend_home_state");
       friend_home_country = getIntent().getStringExtra("friend_home_country");
       
       fetchFriendInfo();
       
	}
	
	private void fetchFriendInfo() {

		TextView friend_name_tv = new TextView(this);
		TextView friend_username_tv = new TextView(this);
		TextView friend_birthday_tv = new TextView(this);
		TextView friend_curloc_tv = new TextView(this);
		TextView friend_hometown_tv = new TextView(this);
		ImageView friend_imgurl_iv = new ImageView(this);
		ImageView friend_cover_iv = new ImageView(this);
		
		friend_name_tv = (TextView) findViewById(R.id.f_detail_header_name);
		friend_username_tv = (TextView) findViewById(R.id.f_detail_username_content);
		friend_birthday_tv = (TextView) findViewById(R.id.f_detail_birthday_content);
		friend_curloc_tv = (TextView) findViewById(R.id.f_detail_currentloc_content);
		friend_hometown_tv = (TextView) findViewById(R.id.f_detail_hometown_content);
		friend_imgurl_iv = (ImageView) findViewById(R.id.f_detail_header_photo);
		friend_cover_iv = (ImageView) findViewById(R.id.cover_photo);

		friend_name_tv.setText(friend_name);
		ActionBar ab = getActionBar();
        ab.setTitle(friend_name);
		
		friend_username_tv.setText(friend_username);
		friend_birthday_tv.setText(friend_birthday);
		friend_curloc_tv.setText(friend_loc_city + ", " + friend_loc_state + ", " + friend_loc_country);
		friend_hometown_tv.setText(friend_home_city + ", " + friend_home_state + ", " + friend_home_country);
		
	    imageLoader.displayImage(friend_imgurl, friend_imgurl_iv, options);
	    imageLoader.displayImage(friend_coverUrl, friend_cover_iv, options);
	    
	    
	    friend_username_tv.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	String url = friend_username;
	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
	        	    url = "http://www.facebook.com/" + url;
	        	}
	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        	startActivity(openUrlIntent);
	        }
	    });
		
	}
	
	/*private void fetchFriendInfo(String friend_id) {
	
    
    friendNameTV = (TextView) findViewById(R.id.f_detail_header_name);
    friendPhotoUriIV = (ImageView) findViewById(R.id.f_detail_header_photo);
    friendCoverPhotoUriIV = (ImageView) findViewById(R.id.cover_photo);
    friendUsernameTV = (TextView) findViewById(R.id.f_detail_username_content);
    
    friendNameTV.setText(friendName);
    imageLoader.displayImage(friendPhotoUri, friendPhotoUriIV, options);
    imageLoader.displayImage(friendCoverPhotoUri, friendCoverPhotoUriIV, options);
    
    friendUsernameTV.setText(friendUserName);
    friendUsernameTV.setPadding(0,10,0,10);
    friendUsernameTV.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
        	String url = friendUsernameTV.getText().toString();
        	if (!url.startsWith("https://") && !url.startsWith("http://")){
        	    url = "http://www.facebook.com/" + url;
        	}
        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	
	}*/



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
