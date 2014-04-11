package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
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
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.raunaqsawhney.contakts.inappbilling.util.IabHelper;
import com.raunaqsawhney.contakts.inappbilling.util.IabResult;
import com.raunaqsawhney.contakts.inappbilling.util.Inventory;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FavActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener{

	static final String TAG = "com.raunaqsawhney.contakts";

	private SlidingMenu menu;
	private ListView navListView;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	private boolean firstRunDoneFav;
	
	IabHelper mHelper;
	static final String ITEM_SKU = "com.raunaqsawhney.contakts.removeads";
	boolean mIsPremium = false;
	
	GridView favGrid;
	private SimpleCursorAdapter mAdapter;

	Cursor cursor;
	String sortOrder;
	String sortParam;
	
   @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fav_activity);

        //initializePayments();
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        setupFavList();

		Session.openActiveSessionFromCache(getBaseContext());
		
   }
   
   private void checkIfGridEmpty() {
       favGrid = (GridView) findViewById(R.id.favGrid);
	   TextView emptyFavs = (TextView) findViewById(R.id.emptyFavs);
	   TextView emptyFavsInfo = (TextView) findViewById(R.id.emptyFavsInfo);

	   if (favGrid.getAdapter().isEmpty()) {
		   emptyFavs.setVisibility(View.VISIBLE); 
		   emptyFavsInfo.setVisibility(View.VISIBLE); 
	   } else {
		   emptyFavs.setVisibility(View.GONE);
		   emptyFavsInfo.setVisibility(View.GONE); 
	   }
   }

   @SuppressWarnings("unused")
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
		
		sortOrder = prefs.getString("sortOrder", "display_name");
		sortParam = prefs.getString("sortParam", " ASC");
		
		theme = prefs.getString("theme", "#33B5E5");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneFav = prefs.getBoolean("firstRunDoneFav", false);
        if (!firstRunDoneFav) {
        	edit.putBoolean("firstRunDoneFav", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.favDialogHeader))
		    .setMessage(getString(R.string.favDialogText))
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
        bar.setDisplayShowTitleEnabled(true);
        
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
        menu.setMode(SlidingMenu.LEFT_RIGHT);
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
        menu.setSecondaryMenu(R.layout.extra_options);
        menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        
        
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
		
		navListView.setAdapter(listAdapter);
		navListView.setOnItemClickListener(this);	
		
		TextView sortASC;
		TextView sortDESC;
		TextView sortFreq;
		TextView sortRec;
		
		TextView sortHeader = (TextView) findViewById(R.id.sortOrder);
		sortHeader.setTextColor(Color.parseColor(theme));
		
		TextView fontHeader = (TextView) findViewById(R.id.fontHeader);
		fontHeader.setTextColor(Color.parseColor(theme));
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
		String so = preferences.getString("sortOrder", "display_name");
		String sp = preferences.getString("sortParam", " ASC");
		
		if ((so + sp).toString().equalsIgnoreCase("display_name ASC")) {
			sortASC = (TextView) findViewById(R.id.azText);
			sortASC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("display_name DESC")) {
			sortDESC = (TextView) findViewById(R.id.zaText);
			sortDESC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("times_contacted DESC")) {
			sortFreq = (TextView) findViewById(R.id.waveText);
			sortFreq.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("last_time_contacted DESC")) {
			sortRec = (TextView) findViewById(R.id.clockText);
			sortRec.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
			
			
		TextView funkyText = (TextView) findViewById(R.id.funkytext);
		funkyText.setTypeface(Typeface.createFromAsset(this.getAssets(), "RobotoCondensed-Light.ttf"));
		
		
		LinearLayout ascending = (LinearLayout) findViewById(R.id.ascending);
		ascending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder", "display_name");
            	edit.putString("sortParam", " ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout descending = (LinearLayout) findViewById(R.id.descending);
		descending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder", "display_name");
            	edit.putString("sortParam", " DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout frequency = (LinearLayout) findViewById(R.id.frequency);
		frequency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder", "times_contacted");
            	edit.putString("sortParam", " DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout recency = (LinearLayout) findViewById(R.id.recency);
		recency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder", "last_time_contacted");
            	edit.putString("sortParam", " DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout clean = (LinearLayout) findViewById(R.id.clean);
		clean.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("fontMain", "Roboto-Light.ttf");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout funky = (LinearLayout) findViewById(R.id.funky);
		funky.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("fontMain", "RobotoCondensed-Regular.ttf");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
    		   	FavActivity.this.startActivity(intent);
            }
        });
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {

		CursorLoader cursorLoader = null;
		
		Uri baseUri = ContactsContract.Contacts.CONTENT_URI;
        
	    String query = ContactsContract.Contacts.STARRED + "='1'";
        
	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.LOOKUP_KEY,
	            ContactsContract.Contacts.PHOTO_URI,
	            ContactsContract.Contacts.DISPLAY_NAME,
	            ContactsContract.Contacts.STARRED};
	    
        cursorLoader = new CursorLoader(
        		FavActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                sortOrder + sortParam);	

        return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
        checkIfGridEmpty();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		mAdapter.changeCursor(null);		
	}


	@SuppressWarnings("deprecation")
	private void setupFavList() {
		
        favGrid = (GridView) findViewById(R.id.favGrid);
                
	    String[] from = {ContactsContract.Contacts.Photo.PHOTO_URI , ContactsContract.Contacts.DISPLAY_NAME};
	    int to[] = new int[]{
	    		R.id.fav_photo,
	    		R.id.fav_name
	    };
	    
	    mAdapter = new SimpleCursorAdapter(
	            this,
	            R.layout.fav_layout,
	            null,
	            from,
	            to,
	            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
	    
	    if (favGrid.getChildCount() <= 0) {
	    	System.out.println("EMPTY");
	    } else {
	    	System.out.println("NOT EMPTY");
	    }
        
	        favGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
	
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, View view,
						final int position, long id) {
					
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

				AlertDialog alertDialog = new AlertDialog.Builder(
                       FavActivity.this).create();
				
				// Setting Dialog Title
		        alertDialog.setTitle(getString(R.string.remFav));
		        
		        // Setting Dialog Message
		        alertDialog.setMessage(getString(R.string.confirmRemFav));
		        
		        // Setting OK Button
		        alertDialog.setButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                	cursor = null;
		                	cursor = (Cursor)parent.getItemAtPosition(position);
		                	
		    				String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		                	
		                    String[] fv = new String[] { displayName };
		                    
		                	ContentValues values = new ContentValues();
		                    values.put(ContactsContract.Contacts.STARRED, 0);
		                    getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);
		                    
		                    Intent intent = new Intent(getApplicationContext(), FavActivity.class);
		                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		                    startActivity(intent);
		        	}
		        });
		        // Showing Alert Message
		        alertDialog.show();
				return true;
			}
            
        });
        
        favGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				cursor = null;
            	cursor = (Cursor)parent.getItemAtPosition(position);
				
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
				// Explicit Intent Example
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","fav");
                startActivity(intent);
		        
            }
        });
        favGrid.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

   }

	/*
	private Bitmap loadContactPhoto(ContentResolver contentResolver, long id) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
	    if (input == null) {

	        return null;
	    }
	    return BitmapFactory.decodeStream(input);		
	}
	*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fav, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_dial:
        		Intent dialIntent = new Intent(FavActivity.this, DialerActivity.class);
    		   	FavActivity.this.startActivity(dialIntent);
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
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FavActivity.this, FavActivity.class);
		   	FavActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(FavActivity.this, RecentActivity.class);
		   FavActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(FavActivity.this, FrequentActivity.class);
	   		FavActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(FavActivity.this, MainActivity.class);
	   		FavActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(FavActivity.this, GoogleActivity.class);
	   		FavActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(FavActivity.this, GroupActivity.class);
		   	FavActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(FavActivity.this, FBActivity.class);
		   	FavActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(FavActivity.this, LoginActivity.class);
		   	FavActivity.this.startActivity(iIntent);
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
