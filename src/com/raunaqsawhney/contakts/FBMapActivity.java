package com.raunaqsawhney.contakts;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FBMapActivity extends Activity implements OnItemClickListener {

	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	
	private String uid;
	private String name;
	private String urlImg;
	private String lat;
	private String lon;
	
	private ListView navListView;
	private SlidingMenu menu;
	
    GoogleMap googleMap;
    	
    ArrayList<fbFriend> mapFriendList = new ArrayList<fbFriend>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fbmap);
		
		
		setupGlobalPrefs();
		//setupActionBar();
		
		getActionBar().hide();

		//setupSlidingMenu();
		startfb();
	}
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);		
	}

	@SuppressWarnings("unused")
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
	        
	        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
	        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, -150, 0,0);
	        config.getPixelInsetBottom();
	        
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }		
	}

	@SuppressWarnings("unused")
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
        
        final String[] nav = { getString(R.string.sMfavourites).toUpperCase(),
				getString(R.string.sMMostContacted).toUpperCase(),
				getString(R.string.sMPhoneContacts).toUpperCase(),
				getString(R.string.sMGoogleContacts).toUpperCase(),
				getString(R.string.sMFacebook).toUpperCase(),
				getString(R.string.sMSettings).toUpperCase(),
				getString(R.string.sMAbout).toUpperCase()
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
		
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fb_map)).getMap();
        
		
		String fqlQuery = "select uid, name, pic, current_location from user where uid in (select uid2 from friend where uid1 = me()) order by name";
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
				
		Session session = Session.getActiveSession();

		if (session == null) {
			//TODO: Change to MAP Alert
			new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.error))
		    .setMessage(getString(R.string.notLoggedInFB))
		    .setNeutralButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	Intent settingIntent = new Intent(FBMapActivity.this, LoginActivity.class);
		        	FBMapActivity.this.startActivity(settingIntent);
		        }
		    }).show();
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
					            fbFriend mapFriend = new fbFriend();
					            
				            	JSONObject json_obj = arr.getJSONObject( i );

					            uid     = json_obj.getString("uid");
					            name   	= json_obj.getString("name");
					            urlImg 	= json_obj.getString("pic");
						            
					            try {
						            lat = json_obj.getJSONObject("current_location").getString("latitude") ; 
					            } catch (JSONException e) {
					            	lat = null;
					            }
					            
					            try {
						            lon = json_obj.getJSONObject("current_location").getString("longitude") ; 
					            } catch (JSONException e) {
					            	lon = null;
					            }
					            
					            mapFriend.setID(uid);
					            mapFriend.setName(name);
					            mapFriend.setURL(urlImg);
					            mapFriend.setLat(lat);
					            mapFriend.setLon(lon);
					            
					            mapFriendList.add(mapFriend);
					            
				            	
					            
					            String[] myTaskParams = { name, lat, lon, urlImg, uid };
				            	new MapTask().execute(myTaskParams);
													            
					        }
			                //googleMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );
					    }
						
					    catch ( Throwable t )
					    {
					        t.printStackTrace();
					    }								
					}
    		});
    		Request.executeBatchAsync(request);
	}
	
	class MapTask extends AsyncTask <String,Void,Void> {
		
		Bitmap bmp;
		String nameOfContact;
		String latitude;
		String longitude;
		String urlOfImage;
		String friend_id;
		
		@Override
		protected void onPreExecute() {
		    // TODO Auto-generated method stub
		    super.onPreExecute();

		}
		
		@Override
		protected Void doInBackground(String... params) {
			
			nameOfContact = params[0];
			latitude = params[1];
			longitude = params[2];
			urlOfImage = params[3];
			friend_id = params[4];
			
			URL url;
			
			try {
				url = new URL(urlOfImage);
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

			} catch (Exception e) {
				e.printStackTrace();
	        }
	    return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

            LatLng latlng = null;
            try {
            	System.out.println(nameOfContact + " = " + latitude + " = " + longitude);
            	latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            	googleMap.addMarker(new MarkerOptions()
    			.position(latlng)
    			.title(nameOfContact)
    			.snippet(friend_id)
    			.icon(BitmapDescriptorFactory.fromBitmap(bmp)));   
            	
            	googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

        			@Override
        			public void onInfoWindowClick(Marker marker) {
        				Intent intent = new Intent(FBMapActivity.this, FriendDetailActivity.class);
        				intent.putExtra("friend_id", marker.getSnippet());
        	            startActivity(intent);				
        			}      
                	
                });
            	
            } catch (NullPointerException e) {
            	latlng = null;
            }                                 
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fbmap, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
  @Override
  public void onResume() {
      super.onResume();  // Always call the superclass method first

  }
  
}
