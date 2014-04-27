package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
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
	String longPressAction;
	
	String number;
	Contact contact = new Contact();
	
	Integer rateIt = 0;
	
	
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
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
					Editor edit = prefs.edit();
					edit.putBoolean("isPremium", mIsPremium);
					
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
		sortParam = prefs.getString("sortParam", " COLLATE LOCALIZED ASC");
		
		longPressAction = prefs.getString("longPress", "remove");
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        Boolean isPremium = prefs.getBoolean("isPremium", false);
        
        firstRunDoneFav = prefs.getBoolean("firstRunDoneFavourite", false);
        if (!firstRunDoneFav) {
        	edit.putBoolean("firstRunDoneFavourite", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.favDialogHeader))
		    .setMessage(getString(R.string.favDialogText))
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
        		    		Intent iIntent = new Intent(FavActivity.this, LoginActivity.class);
        				   	FavActivity.this.startActivity(iIntent);
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
				getString(R.string.sMShuffle),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_allcontacts,
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
		TextView sortFreq;
		TextView sortRec;
		TextView lpaRem;
		TextView lpaCall;
		TextView lpaSMS;
		TextView lpaEmail;
		
		TextView sortHeader = (TextView) findViewById(R.id.sortOrder);
		sortHeader.setTextColor(Color.parseColor(theme));
		
		TextView longPressHeader = (TextView) findViewById(R.id.longPressHeader);
		longPressHeader.setTextColor(Color.parseColor(theme));
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
		String so = preferences.getString("sortOrder", "display_name");
		String sp = preferences.getString("sortParam", " COLLATE LOCALIZED ASC");
		String lpa = preferences.getString("longPress", "remove");
		
		if ((so + sp).toString().equalsIgnoreCase("display_name COLLATE LOCALIZED ASC")) {
			sortASC = (TextView) findViewById(R.id.azText);
			sortASC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("display_name COLLATE LOCALIZED DESC")) {
			sortDESC = (TextView) findViewById(R.id.zaText);
			sortDESC.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("times_contacted COLLATE LOCALIZED DESC")) {
			sortFreq = (TextView) findViewById(R.id.waveText);
			sortFreq.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if ((so + sp).toString().equalsIgnoreCase("last_time_contacted COLLATE LOCALIZED DESC")) {
			sortRec = (TextView) findViewById(R.id.clockText);
			sortRec.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("remove")) {
			lpaRem = (TextView) findViewById(R.id.removeText);
			lpaRem.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("call")) {
			lpaCall = (TextView) findViewById(R.id.callText);
			lpaCall.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("sms")) {
			lpaSMS = (TextView) findViewById(R.id.smsText);
			lpaSMS.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("email")) {
			lpaEmail = (TextView) findViewById(R.id.emailText);
			lpaEmail.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		LinearLayout ascending = (LinearLayout) findViewById(R.id.ascending);
		ascending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder", "display_name");
            	edit.putString("sortParam", " COLLATE LOCALIZED ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout descending = (LinearLayout) findViewById(R.id.descending);
		descending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder", "display_name");
            	edit.putString("sortParam", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout frequency = (LinearLayout) findViewById(R.id.frequency);
		frequency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder", "times_contacted");
            	edit.putString("sortParam", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout recency = (LinearLayout) findViewById(R.id.recency);
		recency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder", "last_time_contacted");
            	edit.putString("sortParam", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout call = (LinearLayout) findViewById(R.id.call);
		call.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress", "call");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout sms = (LinearLayout) findViewById(R.id.sms);
		sms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress", "sms");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout email = (LinearLayout) findViewById(R.id.email);
		email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress", "email");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	FavActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout remove = (LinearLayout) findViewById(R.id.remove);
		remove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress", "remove");
            	edit.apply();
            	
            	Intent intent = new Intent(FavActivity.this, FavActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		

	        favGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
	
				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
					
					view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
					
					if (longPressAction.equals("remove")) {
						
						AlertDialog alertDialog = new AlertDialog.Builder(FavActivity.this).create();
						
				        alertDialog.setTitle(getString(R.string.remFav));
				        
				        alertDialog.setMessage(getString(R.string.confirmRemFav));
				        
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
				        
				        alertDialog.show();
				        
					} else if (longPressAction.equals("call")) {
						
						int count = 0;

		                final ArrayList<String> allContacts = new ArrayList<String>();

		                cursor = null;
	                	cursor = (Cursor)parent.getItemAtPosition(position);
						String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID));
		                
						cursor = null;
		                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
		                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
		                        new String[]{contact_id}, null);
		                
		                try {
		                    number = PhoneNumberUtils.formatNumber(number);
		                } catch (NullPointerException e ) {
		                	e.printStackTrace();
		                }

		                while (cursor.moveToNext()) {
		                    allContacts.add(PhoneNumberUtils.formatNumber(cursor.getString(
		                    		cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))); 
		                    		count++;
		                }
			        		
		                if (count > 1) {
		            		ListView lvDialog = new ListView(FavActivity.this);
		            		
		            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(FavActivity.this,android.R.layout.simple_list_item_1, allContacts);
		            		lvDialog.setAdapter(arrayAdapter); 
		            		
		            		AlertDialog.Builder builder = new AlertDialog.Builder(FavActivity.this);
		            		
		            		builder.setView(lvDialog);
		            		builder.setTitle(getString(R.string.callDialogText));
		            		final Dialog dialog = builder.create();

		            		if (allContacts.isEmpty()) {
		            			Toast.makeText(getApplicationContext(), getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		            		} else  {
		            			dialog.show();
		            		}
		            		
		            		lvDialog.setOnItemClickListener(new OnItemClickListener() {
		            		    @Override
		            		    public void onItemClick(AdapterView<?> parent, View view,
		            		    int position, long id) {
		            		    	Intent callIntent = new Intent(Intent.ACTION_CALL);          
		            	            callIntent.setData(Uri.parse("tel:" + allContacts.get(position)));          
		            	            startActivity(callIntent);  
		            		        dialog.dismiss();

		            		    }
		            		});
		                } else {
		                	if (!allContacts.isEmpty()) {
		                    	Intent callIntent = new Intent(Intent.ACTION_CALL);          
		        	            callIntent.setData(Uri.parse("tel:" + allContacts.get(0)));          
		        	            startActivity(callIntent);  
		                	} else {
		                		try {
		                			Toast.makeText(getApplicationContext(), getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		                		} catch (NullPointerException e) {
		                			e.printStackTrace();
		                			Toast.makeText(getApplicationContext(),  getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		                		}
		                	}
		                }
		                
					} else if (longPressAction.equals("sms")) {

						int count = 0;

		                final ArrayList<String> allContacts = new ArrayList<String>();

		                cursor = null;
	                	cursor = (Cursor)parent.getItemAtPosition(position);
						String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID));
		                
						cursor = null;
		                cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
		                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
		                        new String[]{contact_id}, null);
		                
		                try {
		                    number = PhoneNumberUtils.formatNumber(number);
		                } catch (NullPointerException e ) {
		                	e.printStackTrace();
		                }

		                while (cursor.moveToNext()) {
		                    allContacts.add(PhoneNumberUtils.formatNumber(cursor.getString(
		                    		cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))); 
		                    		count++;
		                }
			        		
		                if (count > 1) {
		                	ListView lvDialog = new ListView(FavActivity.this);
		            		
		            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(FavActivity.this,android.R.layout.simple_list_item_1, allContacts);
		            		lvDialog.setAdapter(arrayAdapter); 
		            		
		            		AlertDialog.Builder builder = new AlertDialog.Builder(FavActivity.this);
		            		
		            		builder.setView(lvDialog);
		            		builder.setTitle(getString(R.string.messageDialogText));
		            		final Dialog dialog = builder.create();

		            		if (allContacts.isEmpty()) {
		            			Toast.makeText(getApplicationContext(),  getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		            		} else  {
		            			dialog.show();
		            		}
		            		
		            		lvDialog.setOnItemClickListener(new OnItemClickListener() {
		            		    @Override
		            		    public void onItemClick(AdapterView<?> parent, View view,
		            		    int position, long id) {
		            		    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", allContacts.get(position), null)));
		            		    }
		            		});
		                } else {
		                	if (!allContacts.isEmpty()) {
		        		    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", allContacts.get(0), null)));
		                	} else {
		                		try {
		                			Toast.makeText(getApplicationContext(), getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		                		} catch (NullPointerException e) {
		                			e.printStackTrace();
		                			Toast.makeText(getApplicationContext(), getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
		                		}
		                	}
		                }
						
					} else if (longPressAction.equals("email")) {
						
						int count = 0;

		        		final ArrayList<String> allContacts = new ArrayList<String>();
		                
		        		cursor = null;
	                	cursor = (Cursor)parent.getItemAtPosition(position);
						String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID));
		        		
		        		cursor = null;
		        		cursor = getContentResolver().query(
		                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
		                        null,
		                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
		                        new String[]{contact_id},
		                        null);

		                while (cursor.moveToNext()) {
		                    allContacts.add(cursor.getString(
		                    		cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))); 
		                    		count++;
		                }
			        		
		                if (count > 1) {
		                	ListView lvDialog = new ListView(FavActivity.this);
		            		
		            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(FavActivity.this,android.R.layout.simple_list_item_1, allContacts);
		            		lvDialog.setAdapter(arrayAdapter); 
		            		
		            		AlertDialog.Builder builder = new AlertDialog.Builder(FavActivity.this);
		            		
		            		builder.setView(lvDialog);
		            		builder.setTitle(getString((R.string.emailDialogText)));
		            		final Dialog dialog = builder.create();

		            		if (allContacts.isEmpty()) {
		            			Toast.makeText(getApplicationContext(), getString(R.string.noEmailFound), Toast.LENGTH_LONG).show();
		            		} else  {
		            			dialog.show();
		            		}
		            		
		            		lvDialog.setOnItemClickListener(new OnItemClickListener() {
		            		    @Override
		            		    public void onItemClick(AdapterView<?> parent, View view,
		            		    int position, long id) {
		            		    	try {
		                		    	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
		                		                "mailto",allContacts.get(position), null));
		                		    	//TODO: Change domain name signature
		                            	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\nSent from Contakts for Android.\nGet it today: www.contaktsapp.com");
		                		    	startActivity(emailIntent);
		            		    	} catch (IndexOutOfBoundsException e) {
		            		    		e.printStackTrace();
		            		    	}
		            		    }
		            		});
		                } else {
		                	if (!allContacts.isEmpty()) {
		                    	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
		        		                "mailto",allContacts.get(0), null));
		        		    	//TODO: Change domain name signature
		                    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\nSent from Contakts for Android.\nGet it today: www.contaktsapp.com");
		        		    	startActivity(emailIntent);
		                	} else {
		                		try {
		                			Toast.makeText(getApplicationContext(), getString(R.string.noEmailFound), Toast.LENGTH_LONG).show();
		                		} catch (NullPointerException e) {
		                			e.printStackTrace();
		                			Toast.makeText(getApplicationContext(), getString(R.string.noEmailFound), Toast.LENGTH_LONG).show();
		                		}
		                	}
		                }
					}
					return true;
			}
        });
        
        favGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            	cursor = null;
            	cursor = (Cursor)parent.getItemAtPosition(position);
				
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","fav");
                startActivity(intent);
		        
            }
        });
        favGrid.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

   }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		
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
		   	Intent loIntent = new Intent(FavActivity.this, ShuffleActivity.class);
		   	FavActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(FavActivity.this, FBActivity.class);
		   	FavActivity.this.startActivity(iIntent);
	   }   else if (selected == 8) {
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
