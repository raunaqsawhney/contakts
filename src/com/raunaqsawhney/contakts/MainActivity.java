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
import android.widget.SearchView.OnQueryTextListener;
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

public class MainActivity extends Activity implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
	
	static final String TAG = "com.raunaqsawhney.contakts";

	// Declare Globals
	String theme;
	String font;
	String fontContent;
	String fontTitle;

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
	
	AdView adView;
	
	Cursor cursor;
	String sortOrder;
	String sortParam;
	String longPressAction;
	
	String number;
	Contact contact = new Contact();
	
	Integer rateIt = 0;
	
   @Override
   public void onCreate(Bundle savedInstanceState) {
       

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);	
        
        // Initialize the loader for background activity
	    
        initializePayments();
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        initializeLoader();
        
        // Enable open Facebook Session
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
        Editor edit = prefs.edit();
        
    	theme = prefs.getString("theme", "#0099CC");
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        font = prefs.getString("font", null);
        
        sortOrder = prefs.getString("sortOrder_main", "display_name");
		sortParam = prefs.getString("sortParam_main", " COLLATE LOCALIZED ASC");
		
		longPressAction = prefs.getString("longPress_main", "call_main");
		
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
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		String so = preferences.getString("sortOrder_main", "display_name");
		String sp = preferences.getString("sortParam_main", " COLLATE LOCALIZED ASC");
		String lpa = preferences.getString("longPress_main", "call_main");
		
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
		
		if (lpa.toString().equals("call_main")) {
			lpaCall = (TextView) findViewById(R.id.callText);
			lpaCall.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("sms_main")) {
			lpaSMS = (TextView) findViewById(R.id.smsText);
			lpaSMS.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		if (lpa.toString().equals("email_main")) {
			lpaEmail = (TextView) findViewById(R.id.emailText);
			lpaEmail.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
		}
		
		LinearLayout ascending = (LinearLayout) findViewById(R.id.ascending);
		ascending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_main", "display_name");
            	edit.putString("sortParam_main", " COLLATE LOCALIZED ASC");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout descending = (LinearLayout) findViewById(R.id.descending);
		descending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

        		edit.putString("sortOrder_main", "display_name");
            	edit.putString("sortParam_main", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout frequency = (LinearLayout) findViewById(R.id.frequency);
		frequency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder_main", "times_contacted");
            	edit.putString("sortParam_main", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout recency = (LinearLayout) findViewById(R.id.recency);
		recency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("sortOrder_main", "last_time_contacted");
            	edit.putString("sortParam_main", " COLLATE LOCALIZED DESC");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout call = (LinearLayout) findViewById(R.id.call);
		call.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_main", "call_main");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout sms = (LinearLayout) findViewById(R.id.sms);
		sms.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_main", "sms_main");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
		LinearLayout email = (LinearLayout) findViewById(R.id.email);
		email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        		Editor edit = preferences.edit();

            	edit.putString("longPress_main", "email_main");
            	edit.apply();
            	
            	Intent intent = new Intent(MainActivity.this, MainActivity.class);
            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	MainActivity.this.startActivity(intent);
            }
        });
		
	}

	private void initializeLoader() {
		// Set up the ListView for contacts to be displayed
        contactList = (ListView)findViewById(R.id.list);
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            	cursor = (Cursor)parent.getItemAtPosition(position);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","main");
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
        mAdapter = new SimpleCursorAdapter(
        		this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        	
	    
        View header = getLayoutInflater().inflate(R.layout.phone_header, null);
	    contactList.addHeaderView(header, null, false);
	    getLoaderManager().initLoader(0, null, this);
        contactList.setAdapter(mAdapter);
        
        contactList.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
				
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				
				if (longPressAction.equals("call_main")) {
					
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
	            		ListView lvDialog = new ListView(MainActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	            		
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
	                
				} else if (longPressAction.equals("sms_main")) {

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
	                	ListView lvDialog = new ListView(MainActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	            		
	            		builder.setView(lvDialog);
	            		builder.setTitle(getString(R.string.messageDialogText));
	            		final Dialog dialog = builder.create();

	            		if (allContacts.isEmpty()) {
	            			Toast.makeText(getApplicationContext(), contact.getName() + " " + getString(R.string.noPhoneDialogText), Toast.LENGTH_LONG).show();
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
					
				} else if (longPressAction.equals("email_main")) {
					
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
	                	ListView lvDialog = new ListView(MainActivity.this);
	            		
	            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, allContacts);
	            		lvDialog.setAdapter(arrayAdapter); 
	            		
	            		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	            		
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
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		
		Uri baseUri;
        if (mFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }
        
        String query = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
        
        String[] projection = new String[] {
        	ContactsContract.Contacts._ID,
        	ContactsContract.Contacts.DISPLAY_NAME,
        	ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        };
        
        cursorLoader = new CursorLoader(
        		MainActivity.this, 
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
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);		
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
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(MainActivity.this, FavActivity.class);
		   	MainActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(MainActivity.this, RecentActivity.class);
		   MainActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(MainActivity.this, FrequentActivity.class);
	   		MainActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(MainActivity.this, MainActivity.class);
	   		MainActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(MainActivity.this, GoogleActivity.class);
	   		MainActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(MainActivity.this, GroupActivity.class);
		   	MainActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(MainActivity.this, ShuffleActivity.class);
		   	MainActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(MainActivity.this, FBActivity.class);
		   	MainActivity.this.startActivity(iIntent);
	   }   else if (selected == 8) {
		   	Intent iIntent = new Intent(MainActivity.this, LoginActivity.class);
		   	MainActivity.this.startActivity(iIntent);
	   }
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
        getMenuInflater().inflate(R.menu.options_menu, menu);
    	
		// Set up Action Bar       
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
	    switch (item.getItemId()) {
	        case R.id.menu_dial:
	        	try {
	        		Intent dialIntent = new Intent(MainActivity.this, DialerActivity.class);
	    		   	MainActivity.this.startActivity(dialIntent);
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(getApplicationContext(), getString(R.string.dialerNotFound), Toast.LENGTH_LONG).show();
	        	}
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