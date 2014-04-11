package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.*;


import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.raunaqsawhney.contakts.inappbilling.util.IabHelper;
import com.raunaqsawhney.contakts.inappbilling.util.IabResult;
import com.raunaqsawhney.contakts.inappbilling.util.Inventory;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FrequentActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	
	static final String TAG = "com.raunaqsawhney.contakts";
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	private SlidingMenu menu;
	private ListView navListView;
	private boolean firstRunDoneFreq;
	
	IabHelper mHelper;
	static final String ITEM_SKU = "com.raunaqsawhney.contakts.removeads";
	boolean mIsPremium = false;
	
	SimpleCursorAdapter mAdapter;
	
	private ListView freqList;
	
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frequent);
		
		//initializePayments();
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		fetchFrequents();
		
		Session.openActiveSessionFromCache(getBaseContext());
	}
	
   private void initializePayments() {
		
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnFvDAXf6H/D0bXbloyf6LgwaFpqafFlABIds+hvN+LGO+uw+tB+1z+EsY5mGwU/Py22yAqKM2w8rUj6QZZJ7xcf0Jy33z3BBLsqAg8wyNv8yZ7Cq2pSYku7EzjaOHpgD43meJp5ByYlyKlL40GijlzPOIAlkUjh6oM2iQRQwrFazZcduIixecPMTk9exDqbgBgfUjxPB4nlVKd2jVCgDTasRMFv9No1q9ntffNd1zgZ/YM3GvzDn3dQwJ+f1LJuHWurrkiz2QZS8mmye52NspyFv+f/DO0PLCm+3a4wh3t3KLFftNYM5nT+j7FFiJvRU2J6M2lsQubWaUmbkVRHxRwIDAQAB";
       
   	mHelper = new IabHelper(this, base64EncodedPublicKey);
   
   	mHelper.startSetup(new 
		IabHelper.OnIabSetupFinishedListener() {
   	   	  public void onIabSetupFinished(IabResult result) 
   	   	  {
   	        if (!result.isSuccess()) {
   	           Log.e("IAB", "In-app Billing setup failed: " + result);
   	      } else {             
   	      	    Log.e("IAB", "In-app Billing is set up OK");
   	      	    mHelper.queryInventoryAsync(mGotInventoryListener); 
   	      }
   	   }
   	});		
	}

	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.e(TAG, "Query inventory finished.");
			if (result.isFailure()) {
				Log.e(TAG, "Failed to query inventory: " + result);
				return;
			} else {
				Log.e(TAG, "Query inventory was successful.");
				mIsPremium = inventory.hasPurchase(ITEM_SKU);
				
				if (!mIsPremium)
					enableAds();
				else 
					disableAds();
			    
				Log.e(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
			}

		Log.e(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};
	
	private void disableAds() {
		AdView adView = (AdView) findViewById(R.id.adView);
		adView.setEnabled(false);
		adView.setVisibility(View.GONE);
	}

	private void enableAds() {
		
		Boolean isNetworkAvailable = checkOnlineStatus();

		if (isNetworkAvailable) {
			AdView adView = (AdView)this.findViewById(R.id.adView);
			adView.setVisibility(View.VISIBLE);
		    AdRequest request = new AdRequest.Builder()
		    .addKeyword("games")
		    .addKeyword("apps")
		    .addKeyword("social")
		    .build();
		    adView.loadAd(request);
		}
	}

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
        theme = prefs.getString("theme", "#33B5E5");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneFreq = prefs.getBoolean("firstRunDoneFreq", false);
        if (!firstRunDoneFreq) {
        	edit.putBoolean("firstRunDoneFreq", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.freqDialogHeader))
		    .setMessage(getString(R.string.freqDialogText))
		    		.setNeutralButton(getString(R.string.okay), null)
		    .show();
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
				getString(R.string.sMGoogleContacts),
				getString(R.string.sMGroups),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_allcontacts,
				R.drawable.ic_nav_group,
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
		
		navListView.setOnItemClickListener(this);
		navListView.setAdapter(listAdapter);
	}
	
	
	@SuppressWarnings("deprecation")
	private void fetchFrequents() {

		ListView freqList = (ListView) findViewById(R.id.freqList);
		
        freqList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				cursor = (Cursor)parent.getItemAtPosition(position);
				
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
				// Explicit Intent Example
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","most");
                startActivity(intent);
		        
            }
        });
        
	    String[] from = {ContactsContract.Contacts.PHOTO_URI , ContactsContract.Contacts.DISPLAY_NAME};
	    int to[] = new int[]{
	    		R.id.freq_photo,
	    		R.id.freq_name
	    };
	    
	    mAdapter = new SimpleCursorAdapter(
	            this,
	            R.layout.freq_layout,
	            null,
	            from,
	            to,
	            0);
		
	    View header = getLayoutInflater().inflate(R.layout.freq_header, null);
	    freqList.addHeaderView(header, null, false);
	    getLoaderManager().initLoader(0, null, this);
        freqList.setAdapter(mAdapter);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		
		Uri baseUri = ContactsContract.Contacts.CONTENT_URI;
        
	    String query = "("+ ContactsContract.Contacts.TIMES_CONTACTED + " > 0) AND ("
	    + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
        + ContactsContract.Contacts.DISPLAY_NAME + " != '' )";
	    

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.LOOKUP_KEY,
	            ContactsContract.Contacts.PHOTO_URI,
	            ContactsContract.Contacts.DISPLAY_NAME,
	            ContactsContract.Contacts.TIMES_CONTACTED};
        
        cursorLoader = new CursorLoader(
        		FrequentActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                ContactsContract.Contacts.TIMES_CONTACTED + " DESC");	
        
        return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}
	
	@Override
	public void onContentChanged() {
	    super.onContentChanged();

	    View empty = findViewById(R.id.emptyFreqText);
	    ListView list = (ListView) findViewById(R.id.freqList);
	    list.setEmptyView(empty);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.frequent, menu);
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FrequentActivity.this, FavActivity.class);
		   	FrequentActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(FrequentActivity.this, RecentActivity.class);
		   FrequentActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(FrequentActivity.this, FrequentActivity.class);
	   		FrequentActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(FrequentActivity.this, MainActivity.class);
	   		FrequentActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(FrequentActivity.this, GoogleActivity.class);
	   		FrequentActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(FrequentActivity.this, GroupActivity.class);
		   	FrequentActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(FrequentActivity.this, FBActivity.class);
		   	FrequentActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(FrequentActivity.this, LoginActivity.class);
		   	FrequentActivity.this.startActivity(iIntent);
	   }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_graph:
	        	Intent graphIntent = new Intent(FrequentActivity.this, GraphActivity.class);
	    	   	FrequentActivity.this.startActivity(graphIntent);
	    	   	return true;    
	        case R.id.menu_dial:
        		Intent dialIntent = new Intent(FrequentActivity.this, DialerActivity.class);
    		   	FrequentActivity.this.startActivity(dialIntent);
	            return true;    
	        case R.id.menu_add:
	        	try {
		    		Intent addIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
		    		startActivity(addIntent);
		    		return true;
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(this, getString(R.string.addNotFound), Toast.LENGTH_LONG).show();
	        	} 
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	private Boolean checkOnlineStatus() {
		ConnectivityManager CManager =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo NInfo = CManager.getActiveNetworkInfo();
		    if (NInfo != null && NInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
	}
	
	@Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();
	  }
	  
	  @Override
	  public void onStart() {
	    super.onStart();
	    cursor = null;
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
	  
	  @Override
	  public void onDestroy() {
		   super.onDestroy();
		   if (cursor != null) {
		      cursor.close();
		   }
		}
}
