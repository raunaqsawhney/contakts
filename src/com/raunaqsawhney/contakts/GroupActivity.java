package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.raunaqsawhney.contakts.inappbilling.util.IabHelper;
import com.raunaqsawhney.contakts.inappbilling.util.IabResult;
import com.raunaqsawhney.contakts.inappbilling.util.Inventory;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class GroupActivity extends Activity implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	static final String TAG = "com.raunaqsawhney.contakts";
	
	// Declare Globals
	String theme;
	String font;
	String fontContent;
	String fontTitle;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	ArrayList<Group> groupList;
	
	SimpleCursorAdapter mAdapter;
	private ListView groupListView;
	
    private static final int TEXT_ID = 0;
    
    String newGroupName;
    Cursor cursor;
    
    IabHelper mHelper;
	static final String ITEM_SKU = "com.raunaqsawhney.contakts.removeads";
	boolean mIsPremium = false;
	
	AdView adView;
	
	private boolean firstRunDoneGroupMain = false;
	
	String sortOrder;
	String sortParam;
	
	Integer rateIt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		
        TelephonyManager tManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();
        
		if (!uid.equalsIgnoreCase("358239051659912")) // This is developer device uid
			initializePayments();
		
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        
        showGroups();
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
			adView = (AdView) findViewById(R.id.adView);
			adView.setEnabled(false);
			adView.setVisibility(View.GONE);
		}
	     
		private void enableAds() {
			
			Boolean isNetworkAvailable = checkOnlineStatus();

			if (isNetworkAvailable) {
				adView = (AdView) findViewById(R.id.adView);
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

        theme = prefs.getString("theme", "#0099CC");
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        font = prefs.getString("font", null);
        
        Boolean isPremium = prefs.getBoolean("isPremium", false);
        
        sortOrder = prefs.getString("sortOrder_group", "title");
		sortParam = prefs.getString("sortParam_group", " COLLATE LOCALIZED ASC");
        
        firstRunDoneGroupMain = prefs.getBoolean("firstRunDoneGroupMain", false);
        if (!firstRunDoneGroupMain) {
        	edit.putBoolean("firstRunDoneGroupMain", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.groupMainDialogHeader))
		    .setMessage(getString(R.string.groupMainDialogText))
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
        
        Integer buyApp = prefs.getInt("buyApp", 0);
        Integer doneBuy = prefs.getInt("doneBuy", 0);

        if (buyApp != 15 ) {
        	if (doneBuy == 0) {
        		buyApp += 1;
            	edit.putInt("buyApp", buyApp);
            	edit.apply();
        	}
        } else {
        	if (doneBuy != 1) {
        		
        		if (!isPremium) {
            		new AlertDialog.Builder(this)
                	.setCancelable(true)
        		    .setTitle(getString(R.string.buyItHeader))
        		    .setMessage(getString(R.string.buyItText))
        		    .setPositiveButton(getString(R.string.removeAds), new DialogInterface.OnClickListener() {
        		    	public void onClick(DialogInterface dialog, int id) {
        		    		Intent iIntent = new Intent(GroupActivity.this, LoginActivity.class);
        				   	GroupActivity.this.startActivity(iIntent);
                            dialog.cancel();
        		    	}
        		    })
        		    .setNegativeButton(getString(R.string.cancel), null)
        		    .show();
                	
                	edit.putInt("doneBuy", 1);
                	edit.apply();
        		}
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
        menu.setSecondaryMenu(R.layout.extra_options_group);
        menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        
        navListView = (ListView) findViewById(R.id.nav_menu);
        
        final String[] nav = { getString(R.string.sMfavourites),
        		getString(R.string.sMRecent),
				getString(R.string.sMMostContacted),
				getString(R.string.sMPhoneContacts),
				getString(R.string.sMGroups),
				getString(R.string.sMShuffle),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings)
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
		
		TextView sortASC;
		TextView sortDESC;
		TextView sortNUMASC;
		TextView sortNUMDESC;
		
		TextView sortHeader = (TextView) findViewById(R.id.sortOrder);
		sortHeader.setTextColor(Color.parseColor(theme));
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);
		String so = preferences.getString("sortOrder_group", "title");
		String sp = preferences.getString("sortParam_group", " COLLATE LOCALIZED ASC");
		
		if ((so + sp).toString().equalsIgnoreCase("title COLLATE LOCALIZED ASC")) {
			sortASC = (TextView) findViewById(R.id.azText);
			sortASC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("title COLLATE LOCALIZED DESC")) {
			sortDESC = (TextView) findViewById(R.id.zaText);
			sortDESC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("summ_count COLLATE LOCALIZED ASC")) {
			sortNUMASC = (TextView) findViewById(R.id.numascText);
			sortNUMASC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("summ_count COLLATE LOCALIZED DESC")) {
			sortNUMDESC = (TextView) findViewById(R.id.numdescText);
			sortNUMDESC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		LinearLayout ascending = (LinearLayout) findViewById(R.id.ascending);
		ascending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_group", "title");
            	edit.putString("sortParam_group", " COLLATE LOCALIZED ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GroupActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout descending = (LinearLayout) findViewById(R.id.descending);
		descending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_group", "title");
            	edit.putString("sortParam_group", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GroupActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout numasc = (LinearLayout) findViewById(R.id.num_ascending);
		numasc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_group", "summ_count");
            	edit.putString("sortParam_group", " COLLATE LOCALIZED ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GroupActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout numdesc = (LinearLayout) findViewById(R.id.num_descending);
		numdesc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_group", "summ_count");
            	edit.putString("sortParam_group", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GroupActivity.this.startActivity(intent);
            }
        });
	}
	
	private void showGroups() {
		
		// Set up the ListView for contacts to be displayed
        groupListView = (ListView)findViewById(R.id.groupList);
        groupListView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            	cursor = (Cursor)parent.getItemAtPosition(position);
				String group_name = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));	
				String group_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));		      

                Intent intent = new Intent(getApplicationContext(), GroupDetailActivity.class);
                intent.putExtra("group_name", group_name);
                intent.putExtra("group_id", group_id);

                startActivity(intent);
            }
        });
        
          
        String[] from = new String[] {
        		ContactsContract.Groups.TITLE,
        		ContactsContract.Groups.SUMMARY_COUNT
        };
        
        // Put above content into XML layouts
        int[] to = new int[] {
        		R.id.g_name,
        		R.id.g_count
        };
	        
        // Set the adapter to display the list
        mAdapter = new SimpleCursorAdapter(
        		this,
                R.layout.g_layout, 
                null,
                from,
                to, 
                0);
        	
        View header = getLayoutInflater().inflate(R.layout.group_header, null);
        groupListView.addHeaderView(header, null, false);
	    getLoaderManager().initLoader(0, null, this);
        groupListView.setAdapter(mAdapter);
 
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		
		Uri baseUri = ContactsContract.Groups.CONTENT_SUMMARY_URI;
        
        String[] projection = new String[] {
        	ContactsContract.Groups._ID,
        	ContactsContract.Groups.TITLE,
        	ContactsContract.Groups.SUMMARY_COUNT
        };
        
        
        cursorLoader = new CursorLoader(
        		GroupActivity.this, 
        		baseUri,
                projection, 
                ContactsContract.Groups.SUMMARY_COUNT + " > 0 AND " + 
                ContactsContract.Groups.DELETED + "!='1'",
                null,
                sortOrder + sortParam);	
        
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
	
	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_add_group:
	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            builder.setTitle(getString(R.string.newGroup));
	     
	             // Use an EditText view to get user input.
	             final EditText input = new EditText(this);
	             input.setId(TEXT_ID);
	             builder.setView(input);
	     
	            builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
	     
	                @Override
	                public void onClick(DialogInterface dialog, int whichButton) {
	                    newGroupName = input.getText().toString();
	                    ContentValues groupValues;
	    	        	ContentResolver cr = getApplicationContext().getContentResolver();
	    	        	groupValues = new ContentValues();
	    	        	groupValues.put(ContactsContract.Groups.TITLE, newGroupName);
	    	        	cr.insert(ContactsContract.Groups.CONTENT_URI, groupValues);
	                    return;
	                }
	            });
	     
	            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	     
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    return;
	                }
	            });
	            
	            builder.create();
	            builder.show();
	        	
	            return true;  
	        default:
	        	return true;
	    }
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
	if (selected == 0) {
		   	Intent favIntent = new Intent(GroupActivity.this, FavActivity.class);
		   	GroupActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(GroupActivity.this, RecentActivity.class);
		   GroupActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(GroupActivity.this, GraphActivity.class);
	   		GroupActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(GroupActivity.this, MainActivity.class);
	   		GroupActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
		   	Intent fbIntent = new Intent(GroupActivity.this, GroupActivity.class);
		   	GroupActivity.this.startActivity(fbIntent);
	   }  else if (selected == 5) {
		   	Intent loIntent = new Intent(GroupActivity.this, ShuffleActivity.class);
		   	GroupActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(GroupActivity.this, FBActivity.class);
		   	GroupActivity.this.startActivity(iIntent);
	   }   else if (selected == 7) {
		   	Intent iIntent = new Intent(GroupActivity.this, LoginActivity.class);
		   	GroupActivity.this.startActivity(iIntent);
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
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_dial:
	        	try {
	        		Intent dialIntent = new Intent(GroupActivity.this, DialerActivity.class);
	    		   	GroupActivity.this.startActivity(dialIntent);
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(getApplicationContext(), getString(R.string.dialerNotFound), Toast.LENGTH_LONG).show();
	        	}
	            return true;  
	        
	        case android.R.id.home:
        		menu.toggle(true);
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      cursor = null;
	      setupActionBar();
	  }
	
	public void onDestroy() {
	   super.onDestroy();
	   if (cursor != null) {
	      cursor.close();
	   }
	}
}
