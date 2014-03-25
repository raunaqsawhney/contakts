package com.raunaqsawhney.contakts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class FriendDetailActivity extends Activity implements OnItemClickListener, StatusCallback {
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;

	private ListView navListView;
	private SlidingMenu menu;
	
	private String friend_id;
	private String name;
	private String urlImg;
	private String username;
	private String birthday;
	private String birthday_date;
	private String current_loc_city;
	private String current_loc_state;
	private String current_loc_country;
	private String current_home_city;
	private String current_home_state;
	private String current_home_country;
	private String coverUrl;
	private Boolean isAppUser;
	
	private String contact_friend_id;

	private ImageLoader imageLoader;
	DisplayImageOptions options;
	
	ArrayList<fbFriend> friendList = new ArrayList<fbFriend>();
	ArrayList<String> educationHistory = new ArrayList<String>();
	ArrayList<String> workHistory = new ArrayList<String>();
	
	TextView friend_name_tv; 
	TextView friend_username_tv;
	TextView friend_birthday_tv;
	TextView friend_curloc_tv;
	TextView friend_hometown_tv;
	ImageView friend_imgurl_iv;
	ImageView friend_cover_iv;
	TextView friend_is_app_user;
	
	int eduCount = 0;
	int workCount = 0;
	
	boolean isThereEducation = false;
	boolean isThereWork = false;
	
	Calendar calendar = Calendar.getInstance(); 
	
	Session.OpenRequest openRequest = null;
	private boolean firstRunDoneFreDet;
	
    String contactNumber = null;
    String contactEmail = null;
    String contactWebsite = null;
	private TextView lblWebsite;
	
	fbFriend contactFriend = new fbFriend();
	private TextView lblEmail;
	private TextView lblPhone;
	
	ArrayList<String> globalPhoneNumberListOfContact = new ArrayList<String>();
	
	Boolean isWhatsAppEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);
		
		setupGlobalPrefs();
		setupActionBar();
		
		
		TextView username = (TextView) findViewById(R.id.f_detail_username_header);
		username.setTextColor(Color.parseColor(theme));
		
		TextView birthday = (TextView) findViewById(R.id.f_detail_birthday_header);
		birthday.setTextColor(Color.parseColor(theme));
		
		TextView curLoc = (TextView) findViewById(R.id.f_detail_currentloc_header);
		curLoc.setTextColor(Color.parseColor(theme));
		
		TextView hometown = (TextView) findViewById(R.id.f_detail_hometown_header);
		hometown.setTextColor(Color.parseColor(theme));
		
		TextView work = (TextView) findViewById(R.id.f_detail_work_header);
		work.setTextColor(Color.parseColor(theme));
		
		TextView education = (TextView) findViewById(R.id.f_detail_education_header);
		education.setTextColor(Color.parseColor(theme));
		
		
		setupSlidingMenu();
		setupImageLoader();
		fetchFriendInfo();
	}
	
	@SuppressWarnings("deprecation")
	private void connectFB() {
		
		
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Identity._ID};
        
        String selection = ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME + " like '"
                + name + "%'";
        
        String[] selectionArgs = null;
        
        String sortOrder = null;
        
        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        startManagingCursor(c);
        

        while (c.moveToNext()){
        	contact_friend_id = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID));
        }
        showContactFriendInfo();
    }
	

	private void showContactFriendInfo() {
		
		getPhoneInfo();
		getEmailInfo();
		getWebsiteInfo();
		
	}

	@SuppressWarnings("deprecation")
	private void getWebsiteInfo() {
		
		TextView websiteHeader = (TextView) findViewById(R.id.f_detail_website_header);
		websiteHeader.setTextColor(Color.parseColor(theme));
		
        int count_web = 0;
		String websiteType = null;

        lblWebsite = (TextView) findViewById(R.id.f_detail_website_header);
        lblWebsite.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout websiteLayout = (LinearLayout) findViewById(R.id.f_detail_website_layout);
        
        String websiteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] websiteWhereParams = new String[]{contact_friend_id,
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE};
        
		Cursor webCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, websiteWhere, websiteWhereParams, null);
        startManagingCursor(webCur);

        while (webCur.moveToNext()) {
            contactWebsite = webCur.getString(webCur.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
            
            String websiteTypeRaw = webCur.getString(webCur.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));

            try {
            	switch(Integer.parseInt(websiteTypeRaw))
                {
                	case 1:
                		websiteType = getString(R.string.homepage);
                		break;
                	case 2:
                		websiteType = getString(R.string.blog);
                		break;
                	case 3:
                		websiteType = getString(R.string.profile);
                		break;
                	case 4:
                		websiteType = getString(R.string.home);
                		break;
                	case 5:
                		websiteType = getString(R.string.work);
                		break;
                	case 7:
                		websiteType = "FTP";
                		break;
                	case 8:
                		websiteType = getString(R.string.other);
                		break;
            		default:
            			websiteType = getString(R.string.other);
            			break;
                }
            } catch (NumberFormatException e) {
            	websiteType = getString(R.string.other);
            }
            
            contactFriend.addWebsites(contactWebsite + "$" + websiteType);  
            count_web++;
                        
            if (contactWebsite != null)
            {
            	websiteLayout.setVisibility(View.VISIBLE);
            }
        }
        
		final int N_web = count_web; 
        final TextView[] websiteTextViews = new TextView[N_web]; 
        String currentWebsite = null;
        String currentTypeWeb = null;
        
        for (int i = 0; i < N_web; i++){
            final TextView websiteTextView = new TextView(this);
            final TextView websiteTypeTextView = new TextView(this);
            final LinearLayout websiteContentLayout = new LinearLayout(this);
                      
            try {
            	 StringTokenizer tokens = new StringTokenizer(contactFriend.getWebsiteByIndex(i), "$");            
                 
                 currentWebsite = tokens.nextToken();
                 currentTypeWeb = tokens.nextToken();
            } catch (NoSuchElementException e) {
            	e.printStackTrace();
            }
           
            websiteTypeTextView.setText(currentTypeWeb);
            websiteTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            websiteTypeTextView.setTextSize(14);
            websiteTypeTextView.setWidth(200);
            websiteTypeTextView.setPadding(0, 10, 0, 10);
            websiteTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            websiteContentLayout.addView(websiteTypeTextView);

            websiteTextView.setText(currentWebsite);
            websiteTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            websiteTextView.setTextSize(18);
            websiteTextView.setPadding(30, 10, 0, 10);
            websiteTextView.setSingleLine();
            websiteTextView.setEllipsize(TextUtils.TruncateAt.END);
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            websiteContentLayout.setLayoutParams(layoutParams);
            websiteContentLayout.addView(websiteTextView);
                        
            websiteLayout.addView(websiteContentLayout);
            
            websiteTextViews[i] = websiteTextView;
            
            websiteTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	String url = websiteTextView.getText().toString();
                	if (!url.startsWith("https://") && !url.startsWith("http://")){
                	    url = "http://" + url;
                	}
                	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                	startActivity(openUrlIntent);
                }
            });
        }		
	}

	@SuppressWarnings("deprecation")
	private void getEmailInfo() {
		
		TextView emailHeader = (TextView) findViewById(R.id.f_detail_email_header);
		emailHeader.setTextColor(Color.parseColor(theme));
		
        int count_email = 0;
		String emailType = null;

        lblEmail = (TextView) findViewById(R.id.f_detail_email_header);
        lblEmail.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout emailLayout = (LinearLayout) findViewById(R.id.f_detail_email_layout);
        
        String emailWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] emailWhereParams = new String[] {contact_friend_id,
        		ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
        
        Cursor cursorEmail = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, emailWhere, emailWhereParams, null);
        startManagingCursor(cursorEmail);
        
        while (cursorEmail.moveToNext()) {
            contactEmail = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            
            String emailTypeRaw = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            
            try {
            	switch(Integer.parseInt(emailTypeRaw))
                {
                	case 1:
                		emailType = getString(R.string.home);
                		break;
                	case 2:
                		emailType = getString(R.string.work);
                		break;
                	case 3:
                		emailType = getString(R.string.other);
                		break;
                	case 4:
                		emailType = getString(R.string.mobile);
                		break;
            		default:
            			emailType = getString(R.string.custom);
            			break;
                }
            } catch (NumberFormatException e) {
            	emailType = getString(R.string.other);
            }
            
            contactFriend.addEmailID(contactEmail + ":" + emailType);
            count_email++;
            
            if (contactEmail != null)
            {
            	emailLayout.setVisibility(View.VISIBLE);
            }
        }
      
        final int N_email = count_email; 
        final TextView[] emailTextViews = new TextView[N_email]; 
        String currentEmail = null;
        String currentTypeEmail = null;
        
        for (int i = 0; i < N_email; i++){
            final TextView emailTextView = new TextView(this);
            final TextView emailTypeTextView = new TextView(this);
            final LinearLayout emailContentLayout = new LinearLayout(this);
                  
            try {
                StringTokenizer tokens = new StringTokenizer(contactFriend.getEmaiIDByIndex(i), ":");            
                
                currentEmail = tokens.nextToken();
                currentTypeEmail = tokens.nextToken();
            } catch (NoSuchElementException e) {
            	e.printStackTrace();
            }

            emailTypeTextView.setText(currentTypeEmail);
            emailTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            emailTypeTextView.setTextSize(14);
            emailTypeTextView.setWidth(200);
            emailTypeTextView.setPadding(0, 10, 0, 10);
            emailTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            emailContentLayout.addView(emailTypeTextView);

            emailTextView.setText(currentEmail);
            emailTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            emailTextView.setTextSize(18);
            emailTextView.setPadding(30, 10, 0, 10);
            emailTextView.setSingleLine();
            emailTextView.setEllipsize(TextUtils.TruncateAt.END);


            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            emailContentLayout.setLayoutParams(layoutParams);
            emailContentLayout.addView(emailTextView);
                        
            emailLayout.addView(emailContentLayout);
            
            emailTextViews[i] = emailTextView;
            
            emailTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
    		                "mailto",emailTextView.getText().toString(), null));
                	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\nSent from Contakts for Android.\nGet it today: www.contaktsapp.com");
    		    	startActivity(emailIntent);
                }
            });
        }		
	}

	@SuppressWarnings("deprecation")
	private void getPhoneInfo() {
		
		TextView phoneHeader = (TextView) findViewById(R.id.f_detail_phone_header);
		phoneHeader.setTextColor(Color.parseColor(theme));
		
        int count_phone = 0;
		String phoneType = null;

        lblPhone = (TextView) findViewById(R.id.f_detail_phone_header);
        lblPhone.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout phoneLayout = (LinearLayout) findViewById(R.id.f_detail_phone_layout);
		
        String phoneWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] phoneWhereParams = new String[]{contact_friend_id,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        
		Cursor cursorPhone = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, phoneWhere, phoneWhereParams, null);
        startManagingCursor(cursorPhone);
 
        while (cursorPhone.moveToNext()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			contactNumber = PhoneNumberUtils.formatNumber(contactNumber);

            String phoneTypeRaw = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            
            try {
            	switch(Integer.parseInt(phoneTypeRaw))
                {
    	        	case 1:
    	        		phoneType = getString(R.string.home);
    	        		break;
    	        	case 2:
    	        		phoneType = getString(R.string.mobile);
    	        		break;
    	        	case 3:
    	        		phoneType = getString(R.string.work);
    	        		break;
    	        	case 4:
    	        		phoneType = getString(R.string.fax_work);
    	        		break;
    	        	case 5:
    	        		phoneType = getString(R.string.fax_home);
    	        		break;
    	        	case 6:
    	        		phoneType = getString(R.string.pager);
    	        		break;
    	        	case 7:
    	        		phoneType = getString(R.string.other);
    	        		break;
    	        	case 8:
    	        		phoneType = getString(R.string.callback);
    	        		break;
    	        	case 9:
    	        		phoneType = getString(R.string.car);
    	        		break;
    	        	case 10:
    	        		phoneType = getString(R.string.company_main);
    	        		break;
    	        	case 11:
    	        		phoneType = "ISDN";
    	        		break;
    	        	case 12:
    	        		phoneType = getString(R.string.main);
    	        		break;
    	        	case 13:
    	        		phoneType = getString(R.string.other);
    	        		break;
    	        	case 14:
    	        		phoneType = getString(R.string.radio);
    	        		break;
    	        	case 15:
    	        		phoneType = getString(R.string.telex);
    	        		break;
    	        	case 16:
    	        		phoneType = "TTY - TDD";
    	        		break;
    	        	case 17:
    	        		phoneType = getString(R.string.work_mobile);
    	        		break;
    	        	case 18:
    	        		phoneType = getString(R.string.work_pager);
    	        		break;
    	        	case 19:
    	        		phoneType = getString(R.string.assistant);
    	        		break;
    	        	case 20:
    	        		phoneType = getString(R.string.mms);
    	        		break;
                }
            } catch (NumberFormatException e) {
            	phoneType = getString(R.string.other);
            }
            
            contactFriend.addPhoneNumer(contactNumber + ":" + phoneType);
            count_phone++;

            if (contactNumber != null)
            {
            	phoneLayout.setVisibility(View.VISIBLE);
            }
        }
   
        final int N_phone = count_phone; 
        final TextView[] phoneTextViews = new TextView[N_phone]; 
        String currentPhone = null;
        String currentType = null;
        String unformattedNumber = null;
        
        for (int i = 0; i < N_phone; i++){
            final TextView phoneNumberTextView = new TextView(this);
            final TextView phoneTypeTextView = new TextView(this);
            final LinearLayout phoneContentLayout = new LinearLayout(this);
            
            try {
                StringTokenizer tokens = new StringTokenizer(contactFriend.getPhoneByIndex(i), ":");
                unformattedNumber = tokens.nextToken();
                
                currentPhone = PhoneNumberUtils.formatNumber(unformattedNumber);
                globalPhoneNumberListOfContact.add(currentPhone);
                
                currentType = tokens.nextToken();
            } catch (NoSuchElementException e) {
            	e.printStackTrace();
            }

            phoneTypeTextView.setText(currentType);
            phoneTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            phoneTypeTextView.setTextSize(14);
            phoneTypeTextView.setWidth(200);
            phoneTypeTextView.setPadding(0, 10, 0, 10);
            phoneTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            phoneContentLayout.addView(phoneTypeTextView);

            phoneNumberTextView.setText(currentPhone);
            phoneNumberTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            phoneNumberTextView.setTextSize(18);
            phoneNumberTextView.setPadding(30, 10, 0, 10);
            phoneNumberTextView.setSingleLine();
            phoneNumberTextView.setEllipsize(TextUtils.TruncateAt.END);
                       
            phoneContentLayout.addView(phoneNumberTextView);
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            
            layoutParams.setMargins(margin, 0, margin, 0);
            phoneContentLayout.setLayoutParams(layoutParams);
                        
            phoneLayout.addView(phoneContentLayout);
            
            phoneTextViews[i] = phoneNumberTextView;	
            
            phoneNumberTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	Intent callIntent = new Intent(Intent.ACTION_CALL);          
    	            callIntent.setData(Uri.parse("tel:"+phoneNumberTextView.getText().toString()));          
    	            startActivity(callIntent);  
                }
            });
        }		
	}
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        isWhatsAppEnabled = prefs.getBoolean("whatsAppEnabled", false);
        
        firstRunDoneFreDet = prefs.getBoolean("firstRunFreDet", false);
        if (!firstRunDoneFreDet) {
        	edit.putBoolean("firstRunFreDet", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.friendDialogHeader))
		    .setMessage(getString(R.string.friendDialogText))
		    .setNeutralButton(getString(R.string.okay), null)
		    .show();
        }	
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(21);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
       
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
				getString(R.string.sMSettings),
				getString(R.string.sMAbout)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_allcontacts,
				R.drawable.ic_nav_group,
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
	
	private void setupImageLoader() {
		
        this.imageLoader = ImageLoader.getInstance();
        
        options = new DisplayImageOptions.Builder()
       .imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default
       .build();
       
       
       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getBaseContext())
       .discCacheExtraOptions(100, 100, CompressFormat.PNG, 100, null)
       .build();
       imageLoader.init(config);
              
       friend_id = getIntent().getStringExtra("friend_id");		
	}

	private void fetchFriendInfo() {
		
		String fqlQuery = "select name, is_app_user, first_name, pic_big, pic_cover, username, birthday, birthday_date, current_location, hometown_location, work_history, education_history from user where uid = " + friend_id;
		
		final Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		
		Session session = Session.getActiveSession();

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
					            fbFriend friend = new fbFriend();
					        	
					            JSONObject json_obj = arr.getJSONObject( i );
					            
					            name   	= json_obj.getString("name");
					            String first_name = json_obj.getString("first_name");
					            urlImg 	= json_obj.getString("pic_big");
					            username = json_obj.getString("username");
					            isAppUser = json_obj.getBoolean("is_app_user");
					            
					            try {
						            birthday = json_obj.getString("birthday");
						            birthday_date = json_obj.getString("birthday_date");
						            if (birthday != "null") {
						            	LinearLayout birthdayLayout = (LinearLayout) findViewById(R.id.f_detail_birthday_layout);
						            	birthdayLayout.setVisibility(View.VISIBLE);

						            	SimpleDateFormat format1 = new SimpleDateFormat("MM/dd");
						            	Date dt1 = format1.parse(birthday_date);
 
						            	String month = (String) android.text.format.DateFormat.format("MM", dt1); 
						            	String day = (String) android.text.format.DateFormat.format("dd", dt1); 
						            	
						            	String friendBirthday = month+"/"+day;
						            	
						            	Calendar c = Calendar.getInstance();
						            	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
						            	String todayDate = sdf.format(c.getTime());
						            							            	
						            	if (todayDate.equalsIgnoreCase(friendBirthday))
						            	{
						            		LinearLayout birthdayIconLayout = (LinearLayout) findViewById(R.id.f_detail_header_birthday_layout);
						            		birthdayIconLayout.setVisibility(View.VISIBLE);
						            		
						            		TextView birthdayText = (TextView) findViewById(R.id.f_detail_header_birthday_text);
						            		birthdayText.setText(getString(R.string.wish) + " " + first_name + " " + getString(R.string.happyBday));
						            		
						            		birthdayText.setOnClickListener(new OnClickListener() {
								    	        @Override
								    	        public void onClick(View v) {
								    	        	String url = friend_username_tv.getText().toString();
								    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
								    	        	    url = "http://www.facebook.com/" + url;
								    	        	}
								    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
								    	        	startActivity(openUrlIntent);
								    	        }
								    	    });
						            	}
						            		
						            }
					            } catch (JSONException e) {
					            	e.printStackTrace();
					            }
					            
					            try {
						            current_loc_city = json_obj.getJSONObject("current_location").getString("city") + ", ";
					            } catch (JSONException e) {
					            	current_loc_city = "";
					            }
					            
					            try {
						            current_loc_state = json_obj.getJSONObject("current_location").getString("state") + ", ";
					            } catch (JSONException e) {
					            	current_loc_state = "";
					            }
					            
					            try {
						            current_loc_country = json_obj.getJSONObject("current_location").getString("country");
					            } catch (JSONException e) {
					            	current_loc_country = "";
					            } 
					            
					            try {
						            current_home_city = json_obj.getJSONObject("hometown_location").getString("city") + ", ";
					            } catch (JSONException e) {
					            	current_home_city = "";
					            }
					            
					            try {
						            current_home_state = json_obj.getJSONObject("hometown_location").getString("state") + ", ";
					            } catch (JSONException e) {
					            	current_home_state = "";
					            }
					            
					            try {
						            current_home_country = json_obj.getJSONObject("hometown_location").getString("country");
					            } catch (JSONException e) {
					            	current_home_country = "";
					            }
					            
					            try {
						            coverUrl = json_obj.getJSONObject("pic_cover").getString("source");
					            } catch (JSONException e) {
					            	coverUrl = "http://farm4.staticflickr.com/3793/9601614175_f989049ff8_z.jpg";
					            }
					            
					            LinearLayout eduLayout = (LinearLayout) findViewById(R.id.f_detail_education_layout);
					            
					            try {
					            	String currSchool;
					            	JSONObject currHistory;
					            	JSONArray eduArray = json_obj.getJSONArray("education_history");
					            	int length = eduArray.length();
					            	if (length > 0)
						            	eduLayout.setVisibility(View.VISIBLE);

					            	
					            	for (int j = 0; j < length; j++) {
					            		currHistory = eduArray.getJSONObject(j);
					            		currSchool = currHistory.getString("name");
					            		
					            		educationHistory.add(currSchool);
					            		eduCount++;
					            	}
					            } catch (JSONException e) {
					            	// Handled by not showing the layout
					            }
					            
					            LinearLayout workLayout = (LinearLayout) findViewById(R.id.f_detail_workhistory_layout);

					            try {
					            	String currWork;
					            	JSONObject currWorkHistory;
					            	JSONArray workArray = json_obj.getJSONArray("work_history");
					            	int length = workArray.length();
					            	if (length > 0)
						            	workLayout.setVisibility(View.VISIBLE);

					            	
					            	for (int j = 0; j < length; j++) {
					            		currWorkHistory = workArray.getJSONObject(j);
					            		currWork = currWorkHistory.getString("company_name");
					            		
					            		workHistory.add(currWork);
					            		workCount++;
					            	}
					            } catch (JSONException e) {
					            	// Handled by not showing the layout
					            }
 					            
					            friend.setName(name);
					            friend.setURL(urlImg);
					            friend.setCoverUrl(coverUrl);
					            friend.setUsername(username);
					            friend.setBirthday(birthday);
					            friend.setCurrentLocCity(current_loc_city);
					            friend.setCurrentLocState(current_loc_state);
					            friend.setCurrentLocCountry(current_loc_country);
					            friend.setCurrentHomeCity(current_home_city);
					            friend.setCurrentHomeState(current_home_state);
					            friend.setCurrentHomeCountry(current_home_country);
					            
					    		friend_name_tv = (TextView) findViewById(R.id.f_detail_header_name);
					    		friend_username_tv = (TextView) findViewById(R.id.f_detail_username_content);
					    		friend_birthday_tv = (TextView) findViewById(R.id.f_detail_birthday_content);
					    		friend_curloc_tv = (TextView) findViewById(R.id.f_detail_currentloc_content);
					    		friend_hometown_tv = (TextView) findViewById(R.id.f_detail_hometown_content);
					    		friend_imgurl_iv = (ImageView) findViewById(R.id.f_detail_header_photo);
					    		friend_cover_iv = (ImageView) findViewById(R.id.cover_photo);
					    		friend_is_app_user = (TextView) findViewById(R.id.f_detail_header_isappuser);
					    		
					    		if (isAppUser)
						    		friend_is_app_user.setText(getString(R.string.usesContakts));
					    		else 
						    		friend_is_app_user.setText("");
					    		
				    			final TextView[] eduTextViews = new TextView[eduCount];
					    		for (int k = 0; k < eduCount; k++) {
					    			final TextView eduTextView = new TextView(getBaseContext());
					    			
					    			eduTextView.setText(educationHistory.get(k));
					    			eduTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
					    			eduTextView.setTextSize(18);
					    			eduTextView.setSingleLine();
					    			eduTextView.setEllipsize(TextUtils.TruncateAt.END);
					    			eduTextView.setTextColor(Color.BLACK);
					    			eduTextView.setPadding(60, 10, 0, 10);
					    			
					    			eduTextView.setOnClickListener(new OnClickListener() {
						    	        @Override
						    	        public void onClick(View v) {
						    	        	String url = eduTextView.getText().toString();
						    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
						    	        	    url = "http://www.google.com/#q=" + url;
						    	        	}
						    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						    	        	startActivity(openUrlIntent);
						    	        }
						    	    });
					    			
					    			eduLayout.addView(eduTextView);
					    			eduTextViews[k] = eduTextView;
					    		}

				            	final TextView[] workTextViews = new TextView[workCount];
					    		for (int m = 0; m < workCount; m++) {
					    			final TextView workTextView = new TextView(getBaseContext());
					    			
					    			workTextView.setText(workHistory.get(m));
					    			workTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
					    			workTextView.setTextSize(18);
					    			workTextView.setSingleLine();
					    			workTextView.setEllipsize(TextUtils.TruncateAt.END);
					    			workTextView.setTextColor(Color.BLACK);
					    			workTextView.setPadding(60, 10, 0, 10);
					    			
					    			workTextView.setOnClickListener(new OnClickListener() {
						    	        @Override
						    	        public void onClick(View v) {
						    	        	String url = workTextView.getText().toString();
						    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
						    	        	    url = "http://www.google.com/#q=" + url;
						    	        	}
						    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						    	        	startActivity(openUrlIntent);
						    	        }
						    	    });
					    			
					    			workLayout.addView(workTextView);
					    			workTextViews[m] = workTextView;
					    		}

						    	friend_name_tv.setText(friend.getName());
					    		ActionBar ab = getActionBar();
					            ab.setTitle(friend.getName());
					    		
					    		friend_username_tv.setText(friend.getUsername());
					    		friend_birthday_tv.setText(friend.getBirthday());
					    		
					    		LinearLayout curLocLayout = (LinearLayout) findViewById(R.id.f_detail_currentloc_layout);
					    		
					    		if (friend.getCurrentLocCity() == "" || friend.getCurrentLocState() == "" || friend.getCurrentLocCountry() == "") {
					    			curLocLayout.setVisibility(View.GONE);
					    		} else {
					    			curLocLayout.setVisibility(View.VISIBLE);
						    		friend_curloc_tv.setText(friend.getCurrentLocCity() + friend.getCurrentLocState() + friend.getCurrentLocCountry());
					    		}
					    		
					    		LinearLayout homeLayout = (LinearLayout) findViewById(R.id.f_detail_hometown_layout);
					    		
					    		if (friend.getCurrentHomeCity() == "" || friend.getCurrentHomeState() == "" || friend.getCurrentHomeCountry() == "") {
					    			homeLayout.setVisibility(View.GONE);
					    		} else {
					    			homeLayout.setVisibility(View.VISIBLE);
						    		friend_hometown_tv.setText(friend.getCurrentHomeCity() + friend.getCurrentHomeState() + friend.getCurrentHomeCountry());
					    		}
					    	    
					    		imageLoader.displayImage(friend.getCoverUrl(), friend_cover_iv, options);
					    	    imageLoader.displayImage(friend.getURL(), friend_imgurl_iv, options);
					    	    
					    	    friend_username_tv.setOnClickListener(new OnClickListener() {
					    	        @Override
					    	        public void onClick(View v) {
					    	        	String url = friend_username_tv.getText().toString();
					    	        	if (!url.startsWith("https://") && !url.startsWith("http://")){
					    	        	    url = "http://www.facebook.com/" + url;
					    	        	}
					    	        	Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					    	        	startActivity(openUrlIntent);
					    	        }
					    	    });
					    	    connectFB();
					        }
					    }
						
					    catch ( Throwable t )
					    {
					        t.printStackTrace();
					    }								
					}
    		});
    		Request.executeBatchAsync(request);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friend_detail, menu);
		
		if(!isWhatsAppEnabled) {
			MenuItem item = menu.findItem(R.id.menu_whatsapp);
        	item.setVisible(false);
        	item.setEnabled(false);
        	//this.invalidateOptionsMenu();
        }
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(FriendDetailActivity.this, FavActivity.class);
		   	FriendDetailActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(FriendDetailActivity.this, RecentActivity.class);
		   FriendDetailActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(FriendDetailActivity.this, FrequentActivity.class);
	   		FriendDetailActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(FriendDetailActivity.this, MainActivity.class);
	   		FriendDetailActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(FriendDetailActivity.this, GoogleActivity.class);
	   		FriendDetailActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(FriendDetailActivity.this, GroupActivity.class);
		   	FriendDetailActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(FriendDetailActivity.this, FBActivity.class);
		   	FriendDetailActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(FriendDetailActivity.this, LoginActivity.class);
		   	FriendDetailActivity.this.startActivity(iIntent);
	   }   else if (selected == 8) {
		   	Intent iIntent = new Intent(FriendDetailActivity.this, InfoActivity.class);
		   	FriendDetailActivity.this.startActivity(iIntent);
	   }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
	        	
	        case R.id.menu_whatsapp:
	        	
	        	if (globalPhoneNumberListOfContact.size() > 1 ) {
	        		ListView whatsAppDialog = new ListView(FriendDetailActivity.this);
					
					ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(FriendDetailActivity.this,android.R.layout.simple_list_item_1, globalPhoneNumberListOfContact);
					whatsAppDialog.setAdapter(arrayAdapter); 
					
					AlertDialog.Builder builder = new AlertDialog.Builder(FriendDetailActivity.this);
					
					builder.setView(whatsAppDialog);
					builder.setTitle("WhatsApp");
					final Dialog dialog = builder.create();

					if (globalPhoneNumberListOfContact.isEmpty()) {
						Toast.makeText(getApplicationContext(), getString(R.string.friend) + " " + getString(R.string.noWhatsAppDialogText), Toast.LENGTH_LONG).show();
					} else  {
						dialog.show();
					}
					
					whatsAppDialog.setOnItemClickListener(new OnItemClickListener() {
					    @Override
					    public void onItemClick(AdapterView<?> parent, View view,
					    int position, long id) {
					    	Uri mUri = Uri.parse("smsto:+"+globalPhoneNumberListOfContact.get(position));
					    	Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
							mIntent.setPackage("com.whatsapp");
							mIntent.putExtra("chat",true);
							try {
								startActivity(mIntent);
							} catch (ActivityNotFoundException e) {
								Toast.makeText(getApplicationContext(), getString(R.string.whatsAppNotFound), Toast.LENGTH_LONG).show();
							}
					        dialog.dismiss();
					    }
					});	
	        	} else {
	        		try {
	        			Uri mUri = Uri.parse("smsto:+"+globalPhoneNumberListOfContact.get(0));
				    	Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
						mIntent.setPackage("com.whatsapp");
						mIntent.putExtra("chat",true);
						startActivity(mIntent);
	        		} catch (IndexOutOfBoundsException f) {
						Toast.makeText(getApplicationContext(), getString(R.string.whatsAppNotFound), Toast.LENGTH_LONG).show();
	        		} catch (ActivityNotFoundException e) {
						Toast.makeText(getApplicationContext(), getString(R.string.whatsAppNotFound), Toast.LENGTH_LONG).show();
	        		}
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
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

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO Auto-generated method stub
		
	}
	
	// Causing tons of crashes, remove it
	@SuppressWarnings("unused")
	private boolean isAppInstalled(String packageName) {
	    PackageManager pm = getPackageManager();
	    boolean installed = false;
	    try {
	       pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
	       installed = true;
	    } catch (PackageManager.NameNotFoundException e) {
	       installed = false;
	    }
	    return installed;
	}
	
	  @Override
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();

	  }
}
