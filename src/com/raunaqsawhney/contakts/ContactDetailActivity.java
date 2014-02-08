package com.raunaqsawhney.contakts;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
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


import com.facebook.Session;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ContactDetailActivity extends Activity implements OnClickListener, OnItemClickListener {
	
	private SlidingMenu menu;
	private ListView navListView;
	
	String lookupKey = null;
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	TextView lblNumber;
	TextView lblName;
	TextView lblCompany;
	TextView lblIMInfo;
	TextView lblEmail;
	TextView lblDate;
	TextView lblAddress;
	TextView lblNote;
	TextView lblRelationship;
	TextView lblWebsite;
	TextView lblNoteContent;
	TextView lblRelationshipContent;
	TextView lblRelationshipType;
	
	ImageView contactPhoto;
	ImageView headerBG;
	
	String number;
	String name;
	String photo;
	String company;
	String im;
	String email;
	String date;
	String address;
	String note;
	String relationship;
	String relationshipType;
	String website;
	String friendUserName;
	
	String contact_id;
	String lookupkey;
	
    Contact contact = new Contact();
    
    GoogleMap googleMap;
	private boolean firstRunDoneConDet;
    	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_detail);
        
        contact_id = getIntent().getStringExtra("contact_id");
        
        setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        setupQuickLinks();
        
        getContactInfo(contact_id);
        
        Session.openActiveSessionFromCache(getBaseContext());

    }

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneConDet = prefs.getBoolean("firstRunConDet", false);
        if (!firstRunDoneConDet) {
        	edit.putBoolean("firstRunDoeConDet", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle("Contact Details")
		    .setMessage("Here you can see all data associated with a contact. Simply tap on any item and learn more about it.")
		    		.setNeutralButton("Okay", null)
		    .show();
        }
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(getAssets(), fontContent)); // Only here the font is not same as fontTitle
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(21);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setHomeButtonEnabled(true);
       
        // Do Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        tintManager.setNavigationBarTintEnabled(true);
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
	        tintManager.setNavigationBarTintColor(Color.parseColor("#000000"));
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
      
		final String[] nav = { "Favourites",
				"Most Contacted",
				"Phone Contacts",
				"Google Contacts",
				"Facebook",
				"Settings",
				"About"
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
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
	
	private void setupQuickLinks() {
		
		// Check for Favourites
        Boolean isStarred = checkStarredStatus(contact_id);
        if (isStarred == true)
        {
        	System.out.println("TRUE");
            ImageView star_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_star);
            star_quicklink.setImageResource(R.drawable.ic_star_gold);
        } else if (isStarred == false){
        	System.out.println("FALSE");
            ImageView star_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_star);
            star_quicklink.setImageResource(R.drawable.ic_star);
        }
        
        // Set up the QuickLinks
        final ImageView star_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_star);
        star_quicklink.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
                String[] fv = new String[] { contact.getName() };

                if (checkStarredStatus(contact_id)) {
                    star_quicklink.setImageResource(R.drawable.ic_star);
                    ContentValues values = new ContentValues();
                    values.put(ContactsContract.Contacts.STARRED, 0);
                    getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);
                } else {
                    star_quicklink.setImageResource(R.drawable.ic_star_gold);
                    ContentValues values = new ContentValues();
                    values.put(ContactsContract.Contacts.STARRED, 1);
                    getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, ContactsContract.Contacts.DISPLAY_NAME + "= ?", fv);
                }
        	}
        });

        ImageView call_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_phone);
        call_quicklink.setOnClickListener(new View.OnClickListener() {
        	
        	@SuppressWarnings("deprecation")
			@Override
            public void onClick(View v) {
                final ArrayList<String> allContacts = new ArrayList<String>();

                Cursor phoneCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{contact_id}, null);
                startManagingCursor(phoneCur);
                
                while (phoneCur.moveToNext()) {
                    allContacts.add(phoneCur.getString(
                    		phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))); 
                }
        		
        		ListView lvDialog = new ListView(ContactDetailActivity.this);
        		
        		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ContactDetailActivity.this,android.R.layout.simple_list_item_1, allContacts);
        		lvDialog.setAdapter(arrayAdapter); 
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailActivity.this);
        		
        		builder.setView(lvDialog);
        		builder.setTitle("Call");
        		final Dialog dialog = builder.create();

        		if (allContacts.isEmpty()) {
        			Toast.makeText(getApplicationContext(), contact.getName() + " does not has any phone numbers.", Toast.LENGTH_LONG).show();
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
        	}
        });
       
        ImageView text_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_text);
        text_quicklink.setOnClickListener(new View.OnClickListener() {
        	
        	@SuppressWarnings("deprecation")
			@Override
            public void onClick(View v) {
        		final ArrayList<String> allContacts = new ArrayList<String>();
                
                Cursor phoneCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{contact_id}, null);
                startManagingCursor(phoneCur);

                
                while (phoneCur.moveToNext()) {
                    allContacts.add(phoneCur.getString(
                    		phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))); 
                }
        		
        		ListView lvDialog = new ListView(ContactDetailActivity.this);
        		
        		
        		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ContactDetailActivity.this,android.R.layout.simple_list_item_1, allContacts);
        		lvDialog.setAdapter(arrayAdapter); 
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailActivity.this);
        		
        		builder.setView(lvDialog);
        		builder.setTitle("Message");
        		final Dialog dialog = builder.create();

        		if (allContacts.isEmpty()) {
        			Toast.makeText(getApplicationContext(), contact.getName() + " does not has any phone numbers.", Toast.LENGTH_LONG).show();
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
        	}
        });
        
        ImageView email_quicklink = (ImageView) findViewById(R.id.c_detail_header_quickLinks_email);
        email_quicklink.setOnClickListener(new View.OnClickListener() {
        	
        	@SuppressWarnings("deprecation")
			@Override
            public void onClick(View v) {
        		final ArrayList<String> allContacts = new ArrayList<String>();
                
        		Cursor emailCur = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{contact_id},
                        null);
                startManagingCursor(emailCur);

                while (emailCur.moveToNext()) {
                    allContacts.add(emailCur.getString(
                    		emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))); 
                }
        		
        		ListView lvDialog = new ListView(ContactDetailActivity.this);
        		
        		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ContactDetailActivity.this,android.R.layout.simple_list_item_1, allContacts);
        		lvDialog.setAdapter(arrayAdapter); 
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailActivity.this);
        		
        		builder.setView(lvDialog);
        		builder.setTitle("Email");
        		final Dialog dialog = builder.create();

        		if (allContacts.isEmpty()) {
        			Toast.makeText(getApplicationContext(), contact.getName() + " does not has any email addresses.", Toast.LENGTH_LONG).show();
        		} else  {
        			dialog.show();
        		}
        		
        		lvDialog.setOnItemClickListener(new OnItemClickListener() {
        		    @Override
        		    public void onItemClick(AdapterView<?> parent, View view,
        		    int position, long id) {
        		    	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
        		                "mailto",allContacts.get(position), null));
        		    	//TODO: Change domain name signature
                    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Contakts for Android.\nGet it today: www.contaktsapp.com");
        		    	startActivity(emailIntent);
        		    }
        		});
        	}
        });		
	}

	@SuppressWarnings("deprecation")
	private Boolean checkStarredStatus(String contact_id2) {

		int starred = 0;
		Boolean boolStarred = false;
		
	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.STARRED};

	    final Cursor cursor = managedQuery(
	            ContactsContract.Contacts.CONTENT_URI,  
	            projection,
	            ContactsContract.Contacts._ID + "=?",
	            new String[]{contact_id},
	            null);
	    startManagingCursor(cursor);
	
	    while (cursor.moveToNext()) {
	        starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED));

	        System.out.println("isStarredStatus:" + starred);
	    }
	    
	    if (starred == 1)
	    {
	    	boolStarred = true;
	    }
	    else if (starred ==  0)
	    {
	    	boolStarred = false;
	    }
	    return boolStarred;
	}

	private void getContactInfo(String contact_id) {
		
        getNameInfo(contact_id);
		getPhoneInfo(contact_id);
		getEmailInfo(contact_id);
		getAddressInfo(contact_id);
		getWebsiteInfo(contact_id);
		getOrganizationInfo(contact_id);
		getNotesInfo(contact_id);
		getDatesInfo(contact_id);
		getRelationshipInfo(contact_id);
		getIMInfo(contact_id);
		getPhoto(contact_id);
		getLookupKey(contact_id);
		
	}

	private void getLookupKey(String contact_id) {
		// Look Up Key
		String [] proj = new String [] {  ContactsContract.Contacts.LOOKUP_KEY };
		
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(
	            ContactsContract.Contacts.CONTENT_URI,  
	            proj,
	            ContactsContract.Contacts._ID + "=?",
	            new String[]{contact_id},
	            null);
 
		while (cursor.moveToNext()) {
	        lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
		}		
	}

	@SuppressWarnings("deprecation")
	private void getPhoto(String contact_id) {
		
        contactPhoto = (ImageView) findViewById(R.id.c_detail_header_photo);
		
		ContentResolver cr = getContentResolver();
		
		Cursor photoCur = cr.query(ContactsContract.Contacts.CONTENT_URI,null,
                ContactsContract.Contacts._ID +" = ?",
                new String[]{contact_id}, null);
        startManagingCursor(photoCur);

		
		while (photoCur.moveToNext()) {
	        photo = photoCur.getString(photoCur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
            
	        if (photo == null)
	        {
	            contactPhoto.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_contact_picture));

	        } else 
	        {
	        	contactPhoto.setImageURI((Uri.parse(photo)));   
	        }
        }
        
        headerBG = (ImageView) findViewById(R.id.header_bg);
        
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contact_id)));
        
        if (inputStream != null) {
        	headerBG.setImageBitmap(BlurImage(BitmapFactory.decodeStream(inputStream)));
        } else {
        	// TODO: Change default image to something nicer
        	headerBG.setImageBitmap((BitmapFactory.decodeResource(this.getResources(), R.drawable.default_bg)));
        }        
	}

	@SuppressWarnings("deprecation")
	private void getIMInfo(String contact_id) {
		
		int count = 0;
		
        lblIMInfo = (TextView) findViewById(R.id.c_detail_im_header);
        lblIMInfo.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout imLayout = (LinearLayout) findViewById(R.id.c_detail_im_layout);
		
        ContentResolver cr = getContentResolver();
        String imType = null;

        String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] imWhereParams = new String[]{contact_id,
            ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
        
        Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
                null, imWhere, imWhereParams, null);
        startManagingCursor(imCur);

        
        while (imCur.moveToNext()) {
            im = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
            
            String imTypeRaw = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
            
            switch(Integer.parseInt(imTypeRaw))
            {
            	case -1:
            		imType = "Custom";
            		break;
            	case 0:
            		imType = "AIM";
            		break;
            	case 1:
            		imType = "MSN";
            		break;
            	case 2:
            		imType = "Yahoo";
            		break;
            	case 3:
            		imType = "Skype";
            		break;
            	case 4:
            		imType = "QQ";
            		break;
            	case 5:
            		imType = "Google Talk";
            		break;
            	case 6:
            		imType = "ICQ";
            		break;
            	case 7:
            		imType = "Jabber";
            		break;
            	case 8:
            		imType = "Net Meeting";
            		break;
            	default:
            		imType = "Custom";
            		break;
            }
            
            contact.addIM(im  + ":" + imType); 
            count++;
                        
            if (im != null && !im.isEmpty())
            {
            	imLayout.setVisibility(View.VISIBLE);
            }
        }
        
		final int N = count; 
        final TextView[] imTextViews = new TextView[N]; 
        String currentIM = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++) {
	        final TextView imTextView = new TextView(this);
	        final TextView imTypeTextView = new TextView(this);
	        final LinearLayout imContentLayout = new LinearLayout(this);
	                    
	        StringTokenizer tokens = new StringTokenizer(contact.getIMByIndex(i), ":");            
	        
	        currentIM = tokens.nextToken();;
	        currentType = tokens.nextToken();
	           
	        imTypeTextView.setText(currentType);
	        imTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
	        imTypeTextView.setTextSize(14);
	        imTypeTextView.setWidth(200);
	        imTypeTextView.setPadding(0, 10, 0, 10);
	        imTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

	        imContentLayout.addView(imTypeTextView);

            imTextView.setText(currentIM);
            imTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            imTextView.setTextSize(18);
            imTextView.setPadding(30, 10, 0, 10);
            imTextView.setSingleLine();
            imTextView.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	        	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        
	        final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
	
	        layoutParams.setMargins(margin, 0, margin, 0);
	        
	        imContentLayout.setLayoutParams(layoutParams);
	        imContentLayout.addView(imTextView);
	                    
	        imLayout.addView(imContentLayout);
	        
	        imTextViews[i] = imTextView;
        }
	}

	@SuppressWarnings("deprecation")
	private void getRelationshipInfo(String contact_id) {
		
        lblRelationship = (TextView) findViewById(R.id.c_detail_relationship_header);
        lblRelationship.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout relationshipLayout = (LinearLayout) findViewById(R.id.c_detail_relationship_layout);
        
        ContentResolver cr = getContentResolver();
        
        String relationshipWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] relationshipWhereParams = new String[]{contact_id,
        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE};
        
        Cursor relationshipCur = cr.query(ContactsContract.Data.CONTENT_URI, null, relationshipWhere, relationshipWhereParams, null);
        startManagingCursor(relationshipCur);

        while (relationshipCur.moveToNext()) {
            relationship = relationshipCur.getString(relationshipCur.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME));
            
            String relationshipTypeRaw = relationshipCur.getString(relationshipCur.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE));
                        
            switch(Integer.parseInt(relationshipTypeRaw))
            {
            	case 1:
            		relationshipType = "Assistant";
            		break;
            	case 2:
            		relationshipType = "Brother";
            		break;
            	case 3:
            		relationshipType = "Child";
            		break;
            	case 4:
            		relationshipType = "Domestic Partner";
            		break;
            	case 5:
            		relationshipType = "Father";
            		break;
            	case 6:
            		relationshipType = "Friend";
            		break;
            	case 7:
            		relationshipType = "Manager";
            		break;
            	case 8:
            		relationshipType = "Mother";
            		break;
            	case 9:
            		relationshipType = "Parent";
            		break;
            	case 10:
            		relationshipType = "Partner";
            		break;
            	case 11:
            		relationshipType = "Referred by";
            		break;
            	case 12:
            		relationshipType = "Relative";
            		break;
            	case 13:
            		relationshipType = "Sister";
            		break;
            	case 14:
            		relationshipType = "Spouse";
            		break;
        		default:
        			relationshipType = "Custom";
        			break;
            }
            
            contact.setRelationship(relationship);
            contact.setRelationshipType(relationshipType);
                        
            if (relationship != null && !relationship.isEmpty())
            {
            	relationshipLayout.setVisibility(View.VISIBLE);
            }
        }
        
        final TextView lblRelationshipType = new TextView(this);
        final TextView lblRelationshipContent = new TextView(this);
        final LinearLayout relationshipContentLayout = new LinearLayout(this);

        lblRelationshipType.setText(contact.getRelationshipType());
        lblRelationshipType.setTypeface(Typeface.createFromAsset(getAssets(), font));
        lblRelationshipType.setTextSize(14);
        lblRelationshipType.setWidth(200);
        lblRelationshipType.setPadding(0, 10, 0, 10);
        lblRelationshipType.setEllipsize(TextUtils.TruncateAt.END);

        lblRelationshipContent.setText(contact.getRelationship());
        lblRelationshipContent.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
        lblRelationshipContent.setTextSize(18);
        lblRelationshipContent.setPadding(30, 10, 0, 10);
        lblRelationshipContent.setSingleLine();
        lblRelationshipContent.setEllipsize(TextUtils.TruncateAt.END);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
       	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
       
       final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

       layoutParams.setMargins(margin, 0, margin, 0);
       
       relationshipContentLayout.setLayoutParams(layoutParams);
       relationshipContentLayout.addView(lblRelationshipType);
       relationshipContentLayout.addView(lblRelationshipContent);
                   
       relationshipLayout.addView(relationshipContentLayout);   
	}

	@SuppressWarnings("deprecation")
	private void getDatesInfo(String contact_id) {
		
		int count = 0;
		
        lblDate = (TextView) findViewById(R.id.c_detail_date_header);
        lblDate.setTypeface(Typeface.createFromAsset(getAssets(), font));
		
        LinearLayout dateLayout = (LinearLayout) findViewById(R.id.c_detail_date_layout);
        
        ContentResolver cr = getContentResolver();
		String dateType = null;
        
        String dateWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] dateWhereParams = new String[]{contact_id,
        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
        
        Cursor dateCur = cr.query(ContactsContract.Data.CONTENT_URI, null, dateWhere, dateWhereParams, null);
        startManagingCursor(dateCur);

        while (dateCur.moveToNext()) {
            date = dateCur.getString(dateCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            
            String dateTypeRaw = dateCur.getString(dateCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
            		
            switch(Integer.parseInt(dateTypeRaw))
            {
            	case 1:
            		dateType = "Anniversary";
            		break;
            	case 2:
            		dateType = "Other";
            		break;
            	case 3:
            		dateType = "Birthday";
            		break;
        		default:
        			break;
            }
            
            contact.addDates(date + ":" + dateType);
            count++;
            
            if (date != null && !date.isEmpty())
            {
            	dateLayout.setVisibility(View.VISIBLE);
            }
            
        }
        
		final int N = count; 
        final TextView[] dateTextViews = new TextView[N]; 
        String currentDate = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++) {
	        final TextView dateTextView = new TextView(this);
	        final TextView dateTypeTextView = new TextView(this);
	        final LinearLayout dateContentLayout = new LinearLayout(this);
	                    
	        StringTokenizer tokens = new StringTokenizer(contact.getDatesByIndex(i), ":");            
	        
	        currentDate = tokens.nextToken();;
	        currentType = tokens.nextToken();
	           
	        dateTypeTextView.setText(currentType);
	        dateTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
	        dateTypeTextView.setTextSize(14);
	        dateTypeTextView.setWidth(200);
	        dateTypeTextView.setPadding(0, 10, 0, 10);
	        dateTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            dateContentLayout.addView(dateTypeTextView);

            dateTextView.setText(currentDate);
            dateTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            dateTextView.setTextSize(18);
            dateTextView.setPadding(30, 10, 0, 10);
            dateTextView.setSingleLine();
            dateTextView.setEllipsize(TextUtils.TruncateAt.END);
            
	        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	        	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        
	        final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
	
	        layoutParams.setMargins(margin, 0, margin, 0);
	        
	        dateContentLayout.setLayoutParams(layoutParams);
	        dateContentLayout.addView(dateTextView);
	                    
	        dateLayout.addView(dateContentLayout);
	        
	        dateTextViews[i] = dateTextView;
        }
	}

	@SuppressWarnings("deprecation")
	private void getNotesInfo(String contact_id) {

        lblNote = (TextView) findViewById(R.id.c_detail_note_header);
        lblNote.setTypeface(Typeface.createFromAsset(getAssets(), font));
        		
        lblNoteContent = (TextView) findViewById(R.id.c_detail_note_content);
        lblNoteContent.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
        
        LinearLayout noteLayout = (LinearLayout) findViewById(R.id.c_detail_note_layout);
        
		ContentResolver cr = getContentResolver();
		
        String noteWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{contact_id,
        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        
        Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
        startManagingCursor(noteCur);

        while (noteCur.moveToNext()) {
            note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            contact.setNotes(note);
            
            
            if (note != null || !note.isEmpty())
            {
                System.out.println("NOTE - NOT NULL");
            	noteLayout.setVisibility(View.VISIBLE);
            }
        }
        lblNoteContent.setText(note);
	}

	@SuppressWarnings("deprecation")
	private void getOrganizationInfo(String contact_id) {

        lblCompany = (TextView) findViewById(R.id.c_detail_header_company);
        lblCompany.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
		
        ContentResolver cr = getContentResolver();
		
		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{contact_id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        
		Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        startManagingCursor(orgCur);

        while (orgCur.moveToNext()) {
            company = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
            contact.setOrganization(company); 
            
        }
        
        lblCompany.setText(company);
        lblCompany.setSingleLine();
        lblCompany.setEllipsize(TextUtils.TruncateAt.END);
        
        lblCompany.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	Uri uri = Uri.parse("http://www.google.com/#q="+lblCompany.getText().toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
	}


	@SuppressWarnings("deprecation")
	private void getWebsiteInfo(String contact_id) {
		
        int count = 0;
		
        lblWebsite = (TextView) findViewById(R.id.c_detail_website_header);
        lblWebsite.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout websiteLayout = (LinearLayout) findViewById(R.id.c_detail_website_layout);
        
		ContentResolver cr = getContentResolver();
		String websiteType = null;
		
		String websiteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] websiteWhereParams = new String[]{contact_id,
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE};
        
		Cursor webCur = cr.query(ContactsContract.Data.CONTENT_URI,
                null, websiteWhere, websiteWhereParams, null);
        startManagingCursor(webCur);

        while (webCur.moveToNext()) {
            website = webCur.getString(webCur.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
            
            String websiteTypeRaw = webCur.getString(webCur.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));

            switch(Integer.parseInt(websiteTypeRaw))
            {
            	case 1:
            		websiteType = "Homepage";
            		break;
            	case 2:
            		websiteType = "Blog";
            		break;
            	case 3:
            		websiteType = "Profile";
            		break;
            	case 4:
            		websiteType = "Home";
            		break;
            	case 5:
            		websiteType = "Work";
            		break;
            	case 7:
            		websiteType = "FTP";
            		break;
            	case 8:
            		websiteType = "Other";
            		break;
        		default:
        			websiteType = "Other";
        			break;
            }
            
            contact.addWebsites(website + "$" + websiteType);  
            count++;
                        
            if (website != null)
            {
            	websiteLayout.setVisibility(View.VISIBLE);
            }
        }
        
		final int N = count; 
        final TextView[] websiteTextViews = new TextView[N]; 
        String currentWebsite = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
            final TextView websiteTextView = new TextView(this);
            final TextView websiteTypeTextView = new TextView(this);
            final LinearLayout websiteContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getWebsiteByIndex(i), "$");            
            
            currentWebsite = tokens.nextToken();;
            currentType = tokens.nextToken();
               
            websiteTypeTextView.setText(currentType);
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
	private void getAddressInfo(String contact_id) {
		
		Geocoder geocoder = new Geocoder(getApplicationContext());  
		List<Address> addresses = null;
		double latitude = 0;
		double longitude = 0;
		
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.the_map)).getMap();
		
		int count = 0;
		
        lblAddress = (TextView) findViewById(R.id.c_detail_address_header);
        lblAddress.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout addressLayout = (LinearLayout) findViewById(R.id.c_detail_address_layout);
        
		ContentResolver cr = getContentResolver();
		String addressType = null;
		
		String addressWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addressWhereParams = new String[]{contact_id,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
        
		Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI,
                null, addressWhere, addressWhereParams, null);
        startManagingCursor(addrCur);

        while (addrCur.moveToNext()) {
            address = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            
            String addressTypeRaw = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            
            switch(Integer.parseInt(addressTypeRaw))
            {
            	case 1:
            		addressType = "Home";
            		break;
            	case 2:
            		addressType = "Work";
            		break;
            	case 3:
            		addressType = "Other";
            		break;
        		default:
        			break;
            }
            
            contact.addAddresses(address + ":" + addressType);  
            Boolean isNetworkAvailable = checkOnlineStatus();

            if (isNetworkAvailable) {
            	try {
    				addresses = geocoder.getFromLocationName(address, 1);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                if(addresses.size() > 0) {
                    latitude= addresses.get(0).getLatitude();
                    longitude= addresses.get(0).getLongitude();
                }
                
                final LatLng latlng = new LatLng(latitude , longitude);
                @SuppressWarnings("unused")
				Marker oneMarker = googleMap.addMarker(new MarkerOptions().position(latlng).title(addressType)); 
                
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                googleMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );
            } else {
            	Toast.makeText(getApplicationContext(), 
                        "Google Map not visible. You are not connected to the Internet. ", Toast.LENGTH_LONG).show();            }

            count++;
            if (address != null)
            {
            	addressLayout.setVisibility(View.VISIBLE);
            }
        }
        
		final int N = count; 
        final TextView[] addressTextViews = new TextView[N]; 
        String currentAddress = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
            final TextView addressTextView = new TextView(this);
            final TextView addressTypeTextView = new TextView(this);
            final LinearLayout addressContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getAddressByIndex(i), ":");            
            
            currentAddress = tokens.nextToken();
            currentType = tokens.nextToken();
               
            addressTypeTextView.setText(currentType);
            addressTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            addressTypeTextView.setTextSize(14);
            addressTypeTextView.setWidth(200);
            addressTypeTextView.setPadding(0, 10, 0, 10);
            addressTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            addressContentLayout.addView(addressTypeTextView);

            addressTextView.setText(currentAddress);
            addressTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            addressTextView.setTextSize(18);
            addressTextView.setPadding(30, 10, 0, 10);
            addressTextView.setEllipsize(TextUtils.TruncateAt.END);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            addressContentLayout.setLayoutParams(layoutParams);
            addressContentLayout.addView(addressTextView);
                        
            addressLayout.addView(addressContentLayout);
            
            addressTextViews[i] = addressTextView;
            
            addressTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	String addressQuery = addressTextView.getText().toString();
                	Intent geoIntent = new Intent (android.content.Intent.ACTION_VIEW, Uri.parse ("geo:0,0?q=" + addressQuery)); // Prepare intent
                	startActivity(geoIntent); 
                }
            });
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

	@SuppressWarnings("deprecation")
	private void getEmailInfo(String contact_id) {
		
		int count = 0;
		
        lblEmail = (TextView) findViewById(R.id.c_detail_email_header);
        lblEmail.setTypeface(Typeface.createFromAsset(getAssets(), font));
		
        LinearLayout emailLayout = (LinearLayout) findViewById(R.id.c_detail_email_layout);
        
		ContentResolver cr = getContentResolver();
		String emailType = null;
		
		Cursor emailCur = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contact_id},
                null);
        startManagingCursor(emailCur);

		
        while (emailCur.moveToNext()) {
            email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            
            String emailTypeRaw = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            
            switch(Integer.parseInt(emailTypeRaw))
            {
            	case 1:
            		emailType = "Home";
            		break;
            	case 2:
            		emailType = "Work";
            		break;
            	case 3:
            		emailType = "Other";
            		break;
            	case 4:
            		emailType = "Mobile";
            		break;
        		default:
        			emailType = "Custom";
        			break;
            }
            contact.addEmailID(email + ":" + emailType);
            count++;
            
            if (email != null)
            {
            	emailLayout.setVisibility(View.VISIBLE);
            }
        }
      
        final int N = count; 
        final TextView[] emailTextViews = new TextView[N]; 
        String currentEmail = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
            final TextView emailTextView = new TextView(this);
            final TextView emailTypeTextView = new TextView(this);
            final LinearLayout emailContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getEmaiIDByIndex(i), ":");            
            
            currentEmail = tokens.nextToken();;
            currentType = tokens.nextToken();
               
            emailTypeTextView.setText(currentType);
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
                	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Contakts for Android.\nGet it today: www.contaktsapp.com");
    		    	startActivity(emailIntent);
                }
            });
        }
	}


	@SuppressWarnings("deprecation")
	private void getNameInfo(String contact_id) {
		
        lblName = (TextView) findViewById(R.id.c_detail_header_name);
        lblName.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
                
		ContentResolver cr = getContentResolver();
		
		Cursor nameCur = cr.query(ContactsContract.Contacts.CONTENT_URI,null,
                ContactsContract.Contacts._ID + " =? ",
                new String[]{contact_id}, null);
        startManagingCursor(nameCur);

        while (nameCur.moveToNext()) {
            name = nameCur.getString(
            		nameCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            
            contact.setName(name);  
            
        }
        lblName.setText(name);
        lblName.setSingleLine();
        lblName.setEllipsize(TextUtils.TruncateAt.END);
        ActionBar ab = getActionBar();
        ab.setTitle(name);
	}

	@SuppressWarnings("deprecation")
	private void getPhoneInfo(String contact_id) {
		
		int count = 0;
		
        lblNumber = (TextView) findViewById(R.id.c_detail_phone_header);
        lblNumber.setTypeface(Typeface.createFromAsset(getAssets(), font));
		
        LinearLayout phoneLayout = (LinearLayout) findViewById(R.id.c_detail_phone_layout);
        
		ContentResolver cr = getContentResolver();
		String phoneType = null;
		
		
		Cursor phoneCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                new String[]{contact_id}, null);
        startManagingCursor(phoneCur);

        while (phoneCur.moveToNext()) {
            number = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            
            String phoneTypeRaw = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            
            switch(Integer.parseInt(phoneTypeRaw))
            {
	        	case 1:
	        		phoneType = "Home";
	        		break;
	        	case 2:
	        		phoneType = "Mobile";
	        		break;
	        	case 3:
	        		phoneType = "Work";
	        		break;
	        	case 4:
	        		phoneType = "Fax (Work)";
	        		break;
	        	case 5:
	        		phoneType = "Fax (Home)";
	        		break;
	        	case 6:
	        		phoneType = "Pager";
	        		break;
	        	case 7:
	        		phoneType = "Other";
	        		break;
	        	case 8:
	        		phoneType = "Callback";
	        		break;
	        	case 9:
	        		phoneType = "Car";
	        		break;
	        	case 10:
	        		phoneType = "Company (Main)";
	        		break;
	        	case 11:
	        		phoneType = "ISDN";
	        		break;
	        	case 12:
	        		phoneType = "Main";
	        		break;
	        	case 13:
	        		phoneType = "Other (Fax)";
	        		break;
	        	case 14:
	        		phoneType = "Radio";
	        		break;
	        	case 15:
	        		phoneType = "Telex";
	        		break;
	        	case 16:
	        		phoneType = "TTY - TDD";
	        		break;
	        	case 17:
	        		phoneType = "Work (Mobile)";
	        		break;
	        	case 18:
	        		phoneType = "Work (Pager)";
	        		break;
	        	case 19:
	        		phoneType = "Assisstant";
	        		break;
	        	case 20:
	        		phoneType = "MMS";
	        		break;
            }
            
            contact.addPhoneNumer(number + ":" + phoneType);
            count++;

            if (number != null)
            {
            	phoneLayout.setVisibility(View.VISIBLE);
            }
        }
   
        final int N = count; 
        final TextView[] phoneTextViews = new TextView[N]; 
        String currentPhone = null;
        String currentType = null;
        String unformattedNumber = null;
        
        for (int i = 0; i < N; i++){
            final TextView phoneNumberTextView = new TextView(this);
            final TextView phoneTypeTextView = new TextView(this);
            final LinearLayout phoneContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getPhoneByIndex(i), ":");
            unformattedNumber = tokens.nextToken();
            
            currentPhone = PhoneNumberUtils.formatNumber(unformattedNumber);
            currentType = tokens.nextToken();
               
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_detail, menu);
		
	
	    // Return true to display menu
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
	        case R.id.menu_edit:
	    		Intent edit_intent = new Intent(Intent.ACTION_EDIT);
	    		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contact_id)); 
	    		edit_intent.setData(contactUri);
	    		edit_intent.putExtra("finishActivityOnSaveCompleted", true);

	    		startActivity(edit_intent);
	        	return true;
	        case R.id.menu_share:
	        	Uri filePath = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
	        	Intent share_intent = new Intent();
	        	share_intent.setAction(android.content.Intent.ACTION_SEND);
	        	share_intent.setType("text/x-vcard");
	        	share_intent.putExtra(Intent.EXTRA_STREAM, (filePath));
	        	
	        	startActivity(Intent.createChooser(share_intent, "Share with"));
	        	return true;
	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	}
	
	
	@SuppressLint("NewApi")
	Bitmap BlurImage (Bitmap input)
	{
		RenderScript rsScript = RenderScript.create(getApplicationContext());
		Allocation alloc = Allocation.createFromBitmap(rsScript, input);

		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
		blur.setRadius (12);
		blur.setInput (alloc);

		Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig ());
		Allocation outAlloc = Allocation.createFromBitmap (rsScript, result);
		blur.forEach (outAlloc);
		outAlloc.copyTo (result);

		rsScript.destroy ();
		return result;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(ContactDetailActivity.this, FavActivity.class);
		   	ContactDetailActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(ContactDetailActivity.this, FrequentActivity.class);
		   ContactDetailActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(ContactDetailActivity.this, MainActivity.class);
	   		ContactDetailActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(ContactDetailActivity.this, GoogleActivity.class);
	   		ContactDetailActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(ContactDetailActivity.this, FBActivity.class);
	   		ContactDetailActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(ContactDetailActivity.this, LoginActivity.class);
		   	ContactDetailActivity.this.startActivity(loIntent);
	   }  else if (selected == 6) {
		   	Intent iIntent = new Intent(ContactDetailActivity.this, InfoActivity.class);
		   	ContactDetailActivity.this.startActivity(iIntent);
	   } 
	}

	@Override
	public void onClick(View v) {
		
	}
}
