package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.raunaqsawhney.contakts.inappbilling.util.IabHelper;
import com.raunaqsawhney.contakts.inappbilling.util.IabResult;
import com.raunaqsawhney.contakts.inappbilling.util.Inventory;
import com.raunaqsawhney.contakts.inappbilling.util.Purchase;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class LoginActivity extends FragmentActivity implements OnItemClickListener {

	static final String TAG = "com.raunaqsawhney.contakts";
	
	// Declare Globals
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	Boolean isWhatsAppEnabled = false;
		
	private SlidingMenu menu;
	private ListView navListView;
	
	SharedPreferences preferences;
	Editor edit;
	
	IabHelper mHelper;
	static final String ITEM_SKU = "com.raunaqsawhney.contakts.removeads";
	boolean mIsPremium = false;
	
	CheckBox whatsApp;
	
	SharedPreferences prefs;
    Integer checkedItem = -1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Session.openActiveSessionFromCache(getBaseContext());
		Session.openActiveSession(this, false, null);

		whatsApp = (CheckBox) findViewById(R.id.enableWhatsApp);

		setupGlobalPrefs();
		initializePayments();
		setupActionBar();
		setupSlidingMenu();
		setupColorPref();
		setupFBLogin();
		
		whatsApp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (whatsApp.isChecked()) {
					edit.putBoolean("whatsAppEnabled", true);
					edit.apply();
				} else {
					edit.putBoolean("whatsAppEnabled", false);
					edit.apply();
				}
			}
		});
		
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
				Button buyAppBtn = (Button) findViewById(R.id.buyApp);

				mIsPremium = inventory.hasPurchase(ITEM_SKU);

				
				if (!mIsPremium)
				    buyAppBtn.setVisibility(View.VISIBLE);
				else 
				    buyAppBtn.setVisibility(View.GONE);
			    
				Log.e(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
			}

		Log.d(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};
	
	
	public void buyApp(View view) {
	     mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,   
			   mPurchaseFinishedListener, "");
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) 
		{
			preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			edit = preferences.edit();
			
		   if (result.isFailure()) {
				Log.e(TAG, "Purchase failure");
				return;
		   } else if (purchase.getSku().equals(ITEM_SKU)) {
				Toast toast = Toast.makeText(getApplicationContext(), "Thanks for purchasing the Contakts!", Toast.LENGTH_LONG);
				toast.show();
				
				Button buyAppBtn = (Button) findViewById(R.id.buyApp);
				buyAppBtn.setVisibility(View.GONE);

				mIsPremium = true;
				
				edit.putBoolean("appPurchased", mIsPremium);
				edit.apply();
		   }    
		}
	};

	private void setupFBLogin() {
		
		LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
		authButton.setOnErrorListener(new OnErrorListener() {
	   
			@Override
			public void onError(FacebookException error) {
			}
		});
			authButton.setReadPermissions(Arrays.asList("basic_info",
					"friends_birthday",
					"friends_hometown",
					"friends_location",
					"friends_work_history",
					"friends_education_history"));
		
			authButton.setSessionStatusCallback(new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session.isOpened()) {
						// DO NOTHING, Return to Settings/Login Activity
					}
				}
			});		
	}

	private void setupGlobalPrefs() {
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        isWhatsAppEnabled = prefs.getBoolean("whatsAppEnabled", false);
        
        if (isWhatsAppEnabled) 
        	whatsApp.setChecked(true);
        else 
        	whatsApp.setChecked(false);
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
        
		Integer startView = prefs.getInt("startView", 0);
		
		if (startView == 0) {
			checkedItem = 0;
    	} else if (startView == 1) {
			checkedItem = 1;
    	} else if (startView == 2) {
			checkedItem = 2;
    	} else if (startView == 3) { 
			checkedItem = 3;
    	} else if (startView == 4) {
			checkedItem = 4;
    	} else if (startView == 5) {
			checkedItem = 5;
    	} else if (startView == 6 ) {
			checkedItem = 6;
    	} else if (startView == 7) {
			checkedItem = 7;
    	} else if (startView == 8) {
			checkedItem = 8;
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
	
	private void setupColorPref() {
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		edit = preferences.edit();
		
		final String views[] = new String[9];
		views[0] = getString(R.string.sMfavourites);
		views[1] = getString(R.string.sMRecent);
		views[2] = getString(R.string.sMMostContacted);
		views[3] = getString(R.string.sMPhoneContacts);
		views[4] = getString(R.string.sMGoogleContacts);
		views[5] = getString(R.string.sMGroups);
		views[6] = getString(R.string.sMShuffle);
		views[7] = getString(R.string.sMFacebook);
		views[8] = getString(R.string.dialer);
		
		
		Button startupViewButton = new Button(this);
		startupViewButton = (Button) findViewById(R.id.startupView);
		startupViewButton.setBackgroundColor(Color.parseColor(theme));
		
		Button colorPicker = new Button(this);
		colorPicker = (Button) findViewById(R.id.colorPicker);
		colorPicker.setBackgroundColor(Color.parseColor(theme));
		
		Button removeAdsBtn = new Button(this);
		removeAdsBtn = (Button) findViewById(R.id.buyApp);
		removeAdsBtn.setBackgroundColor(Color.parseColor(theme));
		
		TextView generalHeader = (TextView) findViewById(R.id.general);
		generalHeader.setTextColor(Color.parseColor(theme));
		
		TextView facebookHeader = (TextView) findViewById(R.id.facebook);
		facebookHeader.setTextColor(Color.parseColor(theme));
		
    	final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
		colorPickerDialog.initialize(R.string.color_dialog_title, new int[] { 
				Color.parseColor("#33B5E5"),
				Color.parseColor("#0099CC"),
				Color.parseColor("#AA66CC"),
				Color.parseColor("#9b59b6"),
				Color.parseColor("#4F2F4F"),
				Color.parseColor("#4C004C"),
				Color.parseColor("#99CC00"),
				Color.parseColor("#669900"),
				Color.parseColor("#FFBB33"),
				Color.parseColor("#FF8800"),
				Color.parseColor("#FF2D55"),
				Color.parseColor("#FF4444"),
				Color.parseColor("#CC0000"),
				Color.parseColor("#590000"),
				Color.parseColor("#1F1F21")}, Color.parseColor(theme), 3,2);
		
		colorPickerDialog.setOnColorSelectedListener(new OnColorSelectedListener() {
			
			@Override
			public void onColorSelected(int color) {
						
				System.out.println(color);
				String themeColor = null;
				
				if (color == -13388315) {
					themeColor = "#33B5E5";
				} else if (color == -16737844 ) {
					themeColor = "#0099CC";
				} else if (color == -5609780) {
					themeColor = "#AA66CC";
				} else if (color == -6596170 ) {
					themeColor = "#9b59b6";
				} else if (color == -6697984 ) {
					themeColor = "#99CC00";
				} else if (color == -10053376) {
					themeColor = "#669900";
				} else if (color == -17613 ) {
					themeColor = "#FFBB33";
				} else if (color == -30720) {
					themeColor = "#FF8800";
				} else if (color == -48060) {
					themeColor = "#FF4444";
				} else if (color == -3407872) {
					themeColor = "#CC0000";
				}  else if (color == -11587761 ) {
					themeColor = "#4F2F4F";
				} else if (color == -11796404) {
					themeColor = "#4C004C";
				} else if (color == -53931) {
					themeColor = "#FF2D55";
				} else if (color == -10944512) {
					themeColor = "#590000";
				}  else if (color == -14737631) {
					themeColor = "#1F1F21";
				}
				
				edit.putString("theme", themeColor);
				edit.apply();
								
				Intent firstRunDoneIntent = null;
	        	Integer which = preferences.getInt("startView", 0);

				
	        	if (which == 0) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, FavActivity.class);
	        	} else if (which == 1) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, RecentActivity.class);
	        	} else if (which == 2) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, GraphActivity.class);
	        	} else if (which == 3) { 
	        		firstRunDoneIntent = new Intent(LoginActivity.this, MainActivity.class);
	        	} else if (which == 4) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, GoogleActivity.class);
	        	} else if (which == 5) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, GroupActivity.class);
	        	} else if (which == 6 ) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, ShuffleActivity.class);
	        	} else if (which == 7) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, FBActivity.class);
	        	} else if (which == 8) {
	        		firstRunDoneIntent = new Intent(LoginActivity.this, DialerActivity.class);
	        	}
	        	
	        	firstRunDoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        	LoginActivity.this.startActivity(firstRunDoneIntent);
				finish();
			}
		});
		
		colorPicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	colorPickerDialog.show(getSupportFragmentManager(), "colorpicker");
            }
        });		
		
		startupViewButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder viewPicker  = new AlertDialog.Builder(LoginActivity.this)
				.setTitle(getString(R.string.pickView))
				.setSingleChoiceItems(views, checkedItem, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					if (which == 0) {
						edit.putInt("startView", 0);
					} else if (which == 1) {
						edit.putInt("startView", 1);
					} else if (which == 2) {
						edit.putInt("startView", 2);
					} else if (which == 3) {
						edit.putInt("startView", 3);
					} else if (which == 4) {
						edit.putInt("startView", 4);
					} else if (which == 5) {
						edit.putInt("startView", 5);
					} else if (which == 6) {
						edit.putInt("startView", 6);
					} else if (which == 7) {
						edit.putInt("startView", 7);
					} else if (which == 8) {
						edit.putInt("startView", 8);
					}
					
					edit.apply();
	
					//dismissing the dialog when the user makes a selection.
					dialog.dismiss();
	
					Intent firstRunDoneIntent = null;
		        	
		        	if (which == 0) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, FavActivity.class);
		        	} else if (which == 1) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, RecentActivity.class);
		        	} else if (which == 2) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, GraphActivity.class);
		        	} else if (which == 3) { 
		        		firstRunDoneIntent = new Intent(LoginActivity.this, MainActivity.class);
		        	} else if (which == 4) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, GoogleActivity.class);
		        	} else if (which == 5) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, GroupActivity.class);
		        	} else if (which == 6 ) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, ShuffleActivity.class);
		        	} else if (which == 7) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, FBActivity.class);
		        	} else if (which == 8) {
		        		firstRunDoneIntent = new Intent(LoginActivity.this, DialerActivity.class);
		        	}
		        	
		        	firstRunDoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		        	LoginActivity.this.startActivity(firstRunDoneIntent);
		        	finish();
					}
				});
				AlertDialog viewPickerDialog = viewPicker.create();		
				viewPickerDialog.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_info:
	        	Intent iIntent = new Intent(LoginActivity.this, InfoActivity.class);
			   	LoginActivity.this.startActivity(iIntent);
	            return true;  
	            
	        case android.R.id.home:
        		menu.toggle(true);
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	@Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
	     
	        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {  
	   	     Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

	        	super.onActivityResult(requestCode, resultCode, data);
	        }
		    
	 }
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(LoginActivity.this, FavActivity.class);
		   	LoginActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(LoginActivity.this, RecentActivity.class);
		   LoginActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(LoginActivity.this, GraphActivity.class);
	   		LoginActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(LoginActivity.this, MainActivity.class);
	   		LoginActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
		   	Intent fbIntent = new Intent(LoginActivity.this, GroupActivity.class);
		   	LoginActivity.this.startActivity(fbIntent);
	   }  else if (selected == 5) {
		   	Intent loIntent = new Intent(LoginActivity.this, ShuffleActivity.class);
		   	LoginActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(LoginActivity.this, FBActivity.class);
		   	LoginActivity.this.startActivity(iIntent);
	   }   else if (selected == 7) {
		   	Intent iIntent = new Intent(LoginActivity.this, LoginActivity.class);
		   	LoginActivity.this.startActivity(iIntent);
	   }
	}

	
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
		if (mHelper != null) mHelper.dispose();
		mHelper = null;  
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
	  
	  @Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();

	  }
}
