package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class GraphActivity extends Activity implements OnItemClickListener {
	
	Hashtable<String, String> hashtable = new Hashtable<String, String>();		
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	String contact_id;
	
	private SlidingMenu menu;
	private ListView navListView;
	
    ArrayList<FreqContact> freqContactList = new ArrayList<FreqContact>();

	private boolean firstRunDoneGraph;
	String [] colorArray;
	
	Cursor c = null;
	
	Integer rateIt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
        getActionBar().setDisplayHomeAsUpEnabled(true);

		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		
		colorArray = new String[10];
		colorArray[0] = "#33B5E5";
		colorArray[1] = "#AA66CC";
		colorArray[2] = "#4F2F4F";
		colorArray[3] = "#99CC00";
		colorArray[4] = "#669900";
		colorArray[5] = "#FFBB33";
		colorArray[6] = "#FF8800";
		colorArray[7] = "#FF2D55";
		colorArray[8] = "#FF4444";
		colorArray[9] = "#CC0000";
		
		createData();
	}

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneGraph = prefs.getBoolean("firstRunDoneGraph1", false);
        if (!firstRunDoneGraph) {
        	edit.putBoolean("firstRunDoneGraph1", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.graphDialogHeader))
		    .setMessage(getString(R.string.graphDialogText))
		    .setNeutralButton(getString(R.string.okay), null)
		    .show();
        }
        
        rateIt = prefs.getInt("rateIt", 0);
    	Integer doneRate = prefs.getInt("doneRate", 0);

        if (rateIt != 10 ) {
        	if (doneRate == 0) {
        		rateIt += 1;
            	edit.putInt("rateIt", rateIt);
            	edit.apply();
        	}
        } else {
        	if (doneRate != 1) {
        		new AlertDialog.Builder(this)
            	.setCancelable(true)
    		    .setTitle(getString(R.string.rateItHeader))
    		    .setMessage(getString(R.string.rateItText))
    		    .setPositiveButton(getString(R.string.playstore), new DialogInterface.OnClickListener() {
    		    	public void onClick(DialogInterface dialog, int id) {
    		    		final String appPackageName = getPackageName();
    	        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        dialog.cancel();
    		    	}
    		    })
    		    .setNegativeButton(getString(R.string.cancel), null)
    		    .show();
            	
            	edit.putInt("doneRate", 1);
            	edit.apply();	
        	}
        }
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
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.ic_navigation_drawer);
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
      
        final String[] nav = { getString(R.string.sMfavourites).toUpperCase(),
        		getString(R.string.sMRecent).toUpperCase(),
				getString(R.string.sMMostContacted).toUpperCase(),
				getString(R.string.sMPhoneContacts).toUpperCase(),
				getString(R.string.sMGroups).toUpperCase(),
				getString(R.string.sMShuffle).toUpperCase(),
				getString(R.string.sMFacebook).toUpperCase(),
				getString(R.string.sMSettings).toUpperCase()
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

	@SuppressWarnings("deprecation")
	private void createData() {
			
		Integer count = 0;
	    GraphAdapter adapter = new GraphAdapter(GraphActivity.this, freqContactList);
	    
		ListView freqGraphList = (ListView) findViewById(R.id.freq_graph_list);
	    freqGraphList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            	FreqContact selectedFreqContact = new FreqContact();
				
            	selectedFreqContact = (FreqContact) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", selectedFreqContact.getID());

                startActivity(intent);
            }
        });
	    freqGraphList.setAdapter(adapter);

		
		PieGraph pie = (PieGraph)findViewById(R.id.graph);
		PieSlice slice;
				
		Uri queryUri = ContactsContract.Contacts.CONTENT_URI;

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.LOOKUP_KEY,
	            ContactsContract.Contacts.DISPLAY_NAME,
	            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
	            ContactsContract.Contacts.TIMES_CONTACTED};
	    

	    String selection = "("+ ContactsContract.Contacts.TIMES_CONTACTED + " > 0) AND ("
	    	    + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
	            + ContactsContract.Contacts.DISPLAY_NAME + " != '' )";

	    try {
	    
			c = getContentResolver().query(queryUri, projection, selection, null, ContactsContract.Contacts.TIMES_CONTACTED + " DESC");
	    	FreqContact curFreqContact;

		    while (c.moveToNext() && count <= 9) {
				curFreqContact = new FreqContact();
		    	
		        curFreqContact.setName(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		        curFreqContact.setTimesContacted(c.getString(c.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)));
		        curFreqContact.setURL(c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
		        curFreqContact.setID(c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)));
		        curFreqContact.setCount(count);
    	       				 
				freqContactList.add(curFreqContact);

				slice = new PieSlice();
				slice.setColor(Color.parseColor(colorArray[count]));
				slice.setValue(Float.parseFloat(curFreqContact.getTimesContacted()));
				pie.addSlice(slice); 
				
	            adapter.notifyDataSetChanged();
	            count++;
		    }    
		    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	@Override
	public void onContentChanged() {
	    super.onContentChanged();

	    View empty = findViewById(R.id.empty);
	    ListView list = (ListView) findViewById(R.id.freq_graph_list);
	    list.setEmptyView(empty);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.graph, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_dial:
        		Intent dialIntent = new Intent(GraphActivity.this, DialerActivity.class);
    		   	GraphActivity.this.startActivity(dialIntent);
	            return true;   
	        case R.id.menu_list:
        		Intent freqIntent = new Intent(GraphActivity.this, FrequentActivity.class);
    		   	GraphActivity.this.startActivity(freqIntent);
	            return true; 
	            
	        case R.id.menu_add:
				try {
		    		Intent addIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
		    		startActivity(addIntent);
		    		return true;
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(this, getString(R.string.addNotFound), Toast.LENGTH_LONG).show();
	        	}
	            return true; 
	            
	        case android.R.id.home:
        		menu.toggle(true);
        		
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
	if (selected == 0) {
		   	Intent favIntent = new Intent(GraphActivity.this, FavActivity.class);
		   	GraphActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(GraphActivity.this, RecentActivity.class);
		   GraphActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(GraphActivity.this, GraphActivity.class);
	   		GraphActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(GraphActivity.this, MainActivity.class);
	   		GraphActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
		   	Intent fbIntent = new Intent(GraphActivity.this, GroupActivity.class);
		   	GraphActivity.this.startActivity(fbIntent);
	   }  else if (selected == 5) {
		   	Intent loIntent = new Intent(GraphActivity.this, ShuffleActivity.class);
		   	GraphActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(GraphActivity.this, FBActivity.class);
		   	GraphActivity.this.startActivity(iIntent);
	   }   else if (selected == 7) {
		   	Intent iIntent = new Intent(GraphActivity.this, LoginActivity.class);
		   	GraphActivity.this.startActivity(iIntent);
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
	  
	  public void onDestroy() {
		   super.onDestroy();
		   if (c != null) {
		      c.close();
		   }
		}
	  
	  public void onPause() {
		   super.onPause();
		   if (c != null) {
		      c.close();
		   }
		}
	  
	  @Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      c = null;
	      setupActionBar();

	  }
}
