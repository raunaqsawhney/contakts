package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
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
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseContactActivity extends Activity implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	private SlidingMenu menu;
	private ListView navListView;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	SimpleCursorAdapter mAdapter;
	String mFilter;
	String photoURI;
	ListView contactList;
	String itemid;
	Cursor c;
	String number;
	Integer dialPadNumber;
    String whichBtn = null;
    String whichName = null;
	
	Contact contact = new Contact();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_contact);
		
        dialPadNumber = getIntent().getIntExtra("dialPadNumber", 0);
		
    	if (dialPadNumber == 1) {
    		whichBtn = "oneBtn";
    		whichName = "oneName";
    	} else if (dialPadNumber == 2) {
    		whichBtn = "twoBtn";
    		whichName = "twoName";
    	} else if (dialPadNumber == 3) {
    		whichBtn = "threeBtn";
    		whichName = "threeName";
    	} else if (dialPadNumber == 4) {
    		whichBtn = "fourBtn";
    		whichName = "fourName";
    	} else if (dialPadNumber == 5) {
    		whichBtn = "fiveBtn";
    		whichName = "fiveName";
    	} else if (dialPadNumber == 6) {
    		whichBtn = "sixBtn";
    		whichName = "sixName";
    	} else if (dialPadNumber == 7) {
    		whichBtn = "sevenBtn";
    		whichName = "sevenName";
    	} else if (dialPadNumber == 8) {
    		whichBtn = "eightBtn";
    		whichName = "eightName";
    	} else if (dialPadNumber == 9) {
    		whichBtn = "nineBtn";
    		whichName = "nineName";
    	}
    	
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		
		showContacts();
	}
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
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
	}
	
	
	
	private void showContacts() {
		// Set up the ListView for contacts to be displayed
        contactList = (ListView)findViewById(R.id.list);
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        		c = null;
            	c = (Cursor)parent.getItemAtPosition(position);
				String contact_id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));		      
				
	        	int count = 0;
                final ArrayList<String> allContacts = new ArrayList<String>();

                c = null;
                c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{contact_id}, null);
                
                try {
                    number = PhoneNumberUtils.formatNumber(number);
                } catch (NullPointerException e ) {
                	e.printStackTrace();
                }

                while (c.moveToNext()) {
                    allContacts.add(PhoneNumberUtils.formatNumber(c.getString(
                    		c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))); 
                    		
                    contact.setName(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    count++;
                }
        		
                if (count > 1) {
            		ListView lvDialog = new ListView(ChooseContactActivity.this);
            		
            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ChooseContactActivity.this,android.R.layout.simple_list_item_1, allContacts);
            		lvDialog.setAdapter(arrayAdapter); 
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(ChooseContactActivity.this);
            		
            		builder.setView(lvDialog);
            		final Dialog dialog = builder.create();

            		if (allContacts.isEmpty()) {
            			Toast.makeText(getApplicationContext(), contact.getName() + " " + getString(R.string.noPhoneDialogText), Toast.LENGTH_LONG).show();
            		} else  {
            			dialog.show();
            		}
            		
            		lvDialog.setOnItemClickListener(new OnItemClickListener() {
    					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChooseContactActivity.this);
            			Editor edit = prefs.edit();
            			
            		    @Override
            		    public void onItemClick(AdapterView<?> parent, View view,
            		    int position, long id) {
  
            		    	System.out.println(contact.getName());
            		    	edit.putString(whichBtn, allContacts.get(position));
		                	edit.putBoolean(dialPadNumber.toString(), true);
		                	edit.putString(whichName, contact.getName());
		                	edit.apply();

            		        dialog.dismiss();
            		        
                			Intent intent = new Intent(ChooseContactActivity.this, DialerActivity.class);
            	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	        	ChooseContactActivity.this.startActivity(intent);
            	        	
                			Toast.makeText(getApplicationContext(), getString(R.string.added) + " \"" + allContacts.get(position) + "\" " + getString(R.string.toSpeedDial) + " \"" + dialPadNumber.toString() + "\"" , Toast.LENGTH_LONG).show();
                			

            		    }
            		});
                } else {
                	if (!allContacts.isEmpty()) {
        		    	System.out.println(contact.getName());

                		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChooseContactActivity.this);
            			Editor edit = prefs.edit();
            			
                		edit.putString(whichBtn, allContacts.get(0));
	                	edit.putBoolean(dialPadNumber.toString(), true);
	                	edit.putString(whichName, contact.getName());
	                	edit.apply();
	                	
            			Intent intent = new Intent(ChooseContactActivity.this, DialerActivity.class);
        	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	        	ChooseContactActivity.this.startActivity(intent);
        	        	
            			Toast.makeText(getApplicationContext(), getString(R.string.added) + " \"" + allContacts.get(0) + "\" " + getString(R.string.toSpeedDial) + " \"" + dialPadNumber.toString() + "\"" , Toast.LENGTH_LONG).show();
            			
                	} else {
            			Toast.makeText(getApplicationContext(), contact.getName() + " " + getString(R.string.noPhoneDialogText), Toast.LENGTH_LONG).show();
                	}
                }
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
        		ChooseContactActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");	
        
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
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_contact, menu);
		
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


	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		
		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(ChooseContactActivity.this, FavActivity.class);
		   	ChooseContactActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(ChooseContactActivity.this, RecentActivity.class);
		   ChooseContactActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(ChooseContactActivity.this, FrequentActivity.class);
	   		ChooseContactActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(ChooseContactActivity.this, MainActivity.class);
	   		ChooseContactActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(ChooseContactActivity.this, GoogleActivity.class);
	   		ChooseContactActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(ChooseContactActivity.this, GroupActivity.class);
		   	ChooseContactActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(ChooseContactActivity.this, ShuffleActivity.class);
		   	ChooseContactActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(ChooseContactActivity.this, FBActivity.class);
		   	ChooseContactActivity.this.startActivity(iIntent);
	   }   else if (selected == 8) {
		   	Intent iIntent = new Intent(ChooseContactActivity.this, LoginActivity.class);
		   	ChooseContactActivity.this.startActivity(iIntent);
	   } 
	}
	
	  @Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();
	  }
	  
	  @Override
	  public void onStart() {
	    super.onStart();
	    c = null;
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
		   if (c != null) {
		      c.close();
		   }
		}
}
