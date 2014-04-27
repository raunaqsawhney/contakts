package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
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
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.google.android.gms.ads.*;

import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.raunaqsawhney.contakts.inappbilling.util.IabHelper;
import com.raunaqsawhney.contakts.inappbilling.util.IabResult;
import com.raunaqsawhney.contakts.inappbilling.util.Inventory;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class GoogleActivity extends Activity implements OnQueryTextListener, LoaderCallbacks<Cursor>, OnItemClickListener {

	static final String TAG = "com.raunaqsawhney.contakts";

	// Declare Globals
	String font;
	String fontTitle;
	String fontContent;
	String theme;

	SimpleCursorAdapter mAdapter;
	String mFilter;
	String photoURI;
	ListView contactList;
	String itemid;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	IabHelper mHelper;
	static final String ITEM_SKU = "com.raunaqsawhney.contakts.removeads";
	boolean mIsPremium = false;
	
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
        setContentView(R.layout.activity_main);
        

        initializePayments();
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        initializeLoader();
        
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
		Editor edit = prefs.edit();

		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        Boolean isPremium = prefs.getBoolean("isPremium", false);
        
        sortOrder = prefs.getString("sortOrder_google", "display_name");
		sortParam = prefs.getString("sortParam_google", " COLLATE LOCALIZED ASC");
		longPressAction = prefs.getString("longPress_google", "call_google");
		
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
        		    		Intent iIntent = new Intent(GoogleActivity.this, LoginActivity.class);
        				   	GoogleActivity.this.startActivity(iIntent);
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
        menu.setSecondaryMenu(R.layout.extra_options_main);
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
		TextView lpaCall;
		TextView lpaSMS;
		TextView lpaEmail;
		
		TextView sortHeader = (TextView) findViewById(R.id.sortOrder);
		sortHeader.setTextColor(Color.parseColor(theme));
		
		TextView longPressHeader = (TextView) findViewById(R.id.longPressHeader);
		longPressHeader.setTextColor(Color.parseColor(theme));
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
		String so = preferences.getString("sortOrder_google", "display_name");
		String sp = preferences.getString("sortParam_google", " COLLATE LOCALIZED ASC");
		String lpa = preferences.getString("longPress_google", "call_google");
		
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
		
		if (lpa.toString().equals("call_google")) {
			lpaCall = (TextView) findViewById(R.id.callText);
			lpaCall.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("sms_google")) {
			lpaSMS = (TextView) findViewById(R.id.smsText);
			lpaSMS.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("email_google")) {
			lpaEmail = (TextView) findViewById(R.id.emailText);
			lpaEmail.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		LinearLayout ascending = (LinearLayout) findViewById(R.id.ascending);
		ascending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_google", "display_name");
            	edit.putString("sortParam_google", " COLLATE LOCALIZED ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout descending = (LinearLayout) findViewById(R.id.descending);
		descending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_google", "display_name");
            	edit.putString("sortParam_google", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout frequency = (LinearLayout) findViewById(R.id.frequency);
		frequency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder_google", "times_contacted");
            	edit.putString("sortParam_google", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout recency = (LinearLayout) findViewById(R.id.recency);
		recency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder_google", "last_time_contacted");
            	edit.putString("sortParam_google", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout call = (LinearLayout) findViewById(R.id.call);
		call.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_google", "call_google");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout sms = (LinearLayout) findViewById(R.id.sms);
		sms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_google", "sms_google");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout email = (LinearLayout) findViewById(R.id.email);
		email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GoogleActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_google", "email_google");
            	edit.apply();
            	
            	Intent intent = new Intent(GoogleActivity.this, GoogleActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	GoogleActivity.this.startActivity(intent);
            }
        });
	}

	private void initializeLoader() {
		
        contactList = (ListView)findViewById(R.id.list);
        View header = getLayoutInflater().inflate(R.layout.google_header, null);
	    contactList.addHeaderView(header, null, false);
		
		// Set up the ListView for contacts to be displayed
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            	cursor = (Cursor)parent.getItemAtPosition(position);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","google");
                startActivity(intent);
            }
        });
	        
        // Fetch Display Name and Contact Photo URI
        String[] from = new String[] {
        		ContactsContract.Data.DISPLAY_NAME,
        		ContactsContract.Data.PHOTO_THUMBNAIL_URI
        };
        
        // Put above content into XML layouts
        int[] to = new int[] {
        		R.id.c_name,
        		R.id.c_photo
        };
	        
        // Set the adapter to display the list
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        
        // Initialize the loader for background activity
	    LoaderManager loaderManager = getLoaderManager();
	    loaderManager.initLoader(0, null, this);	
	    
        contactList.setAdapter(mAdapter);
        
        contactList.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
				
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				
				if (longPressAction.equals("call_google")) {
					
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
	            		ListView lvDialog = new ListView(GoogleActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(GoogleActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(GoogleActivity.this);
	            		
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
	            	            callIntent.setData(Uri.parse("tel:"+allContacts.get(position)));          
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
	                			Toast.makeText(getApplicationContext(), getString(R.string.noPhoneFound), Toast.LENGTH_LONG).show();
	                		}
	                	}
	                }
	                
				} else if (longPressAction.equals("sms_google")) {

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
	                	ListView lvDialog = new ListView(GoogleActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(GoogleActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(GoogleActivity.this);
	            		
	            		builder.setView(lvDialog);
	            		builder.setTitle(getString(R.string.messageDialogText));
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
					
				} else if (longPressAction.equals("email_google")) {
					
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
	                	ListView lvDialog = new ListView(GoogleActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(GoogleActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(GoogleActivity.this);
	            		
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
	    
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri baseUri;
        if (mFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }
        
        String query = "(" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL)";
        
        String[] projection = new String[] {
        	ContactsContract.Contacts._ID,
        	ContactsContract.Contacts.DISPLAY_NAME,
        	ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        };
        
        CursorLoader cursorLoader = new CursorLoader(
        		GoogleActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                sortOrder + sortParam);
        
        return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);		
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		mFilter = !TextUtils.isEmpty(arg0) ? arg0 : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.options_menu, menu);
    	
        // Set up the Action Bar menu Search
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("Find contacts");
        searchView.setQueryHint(Html.fromHtml("<font color = #F7F7F7>" + getResources().getString(R.string.search_hint) + "</font>"));
        
        searchView.setOnQueryTextListener(this);        
        
        AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        search_text.setTextColor(Color.WHITE);
        search_text.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
        	case R.id.menu_dial:
        		Intent dialIntent = new Intent(GoogleActivity.this, DialerActivity.class);
    		   	GoogleActivity.this.startActivity(dialIntent);
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
		   	Intent favIntent = new Intent(GoogleActivity.this, FavActivity.class);
		   	GoogleActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(GoogleActivity.this, RecentActivity.class);
		   GoogleActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(GoogleActivity.this, FrequentActivity.class);
	   		GoogleActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(GoogleActivity.this, MainActivity.class);
	   		GoogleActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(GoogleActivity.this, GoogleActivity.class);
	   		GoogleActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(GoogleActivity.this, GroupActivity.class);
		   	GoogleActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(GoogleActivity.this, ShuffleActivity.class);
		   	GoogleActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(GoogleActivity.this, FBActivity.class);
		   	GoogleActivity.this.startActivity(iIntent);
	   }   else if (selected == 8) {
		   	Intent iIntent = new Intent(GoogleActivity.this, LoginActivity.class);
		   	GoogleActivity.this.startActivity(iIntent);
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