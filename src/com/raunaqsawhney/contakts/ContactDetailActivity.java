package com.raunaqsawhney.contakts;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ContactDetailActivity extends Activity implements OnClickListener {
	
	private SlidingMenu menu;
	private ArrayAdapter<String> listAdapter;
	private ListView navListView;
	
	String theme = "#18A7B5";
	String font = "RobotoCondensed-Regular.ttf";
	String fontContent = "RobotoCondensed-Light.ttf";
	
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
	
	String contact_id;
	String lookupkey;
	
    Contact contact = new Contact();
    
    GoogleMap googleMap;

    	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        
        // Set up Action Bar
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitleText = (TextView) findViewById(titleId);
        Typeface actionBarFont = Typeface.createFromAsset(getAssets(), font);
        actionBarTitleText.setTypeface(actionBarFont);
        actionBarTitleText.setTextColor(Color.WHITE);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));

        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(false);

        // Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }
        
        
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
      
		String[] nav = new String[] { "Favourites", "Phone Contacts", "Google Contacts" };
		ArrayList<String> navList = new ArrayList<String>();
		navList.addAll(Arrays.asList(nav));
		
		listAdapter = new ArrayAdapter<String>(this,
	            R.layout.nav_item_layout, R.id.nav_name, navList);
		
		navListView.setAdapter(listAdapter);
		navListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String item = String.valueOf(navListView.getItemAtPosition(position));
                if (item == "Favourites") {
                	Intent stIntent = new Intent(ContactDetailActivity.this, FavActivity.class);
                	ContactDetailActivity.this.startActivity(stIntent);
                } else if (item == "Phone Contacts") {
                	Intent pIntent = new Intent(ContactDetailActivity.this, MainActivity.class);
                	ContactDetailActivity.this.startActivity(pIntent);
                } else if (item == "Google Contacts") {
                	Intent gIntent = new Intent(ContactDetailActivity.this, GoogleActivity.class);
                	ContactDetailActivity.this.startActivity(gIntent);
                }
            }
        });
        
        
        contact_id = getIntent().getStringExtra("contact_id");
        getContactInfo(contact_id);

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
                    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Contakts for Android.\nGet it today: contakts.com");
        		    	startActivity(Intent.createChooser(emailIntent, "Send Email"));
        		    }
        		});
        	}
        });
    }

	private Boolean checkStarredStatus(String contact_id2) {

		int starred = 0;
		Boolean boolStarred = false;
		
	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.STARRED};

	    @SuppressWarnings("deprecation")
	    final Cursor cursor = managedQuery(
	            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
	            projection,
	            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
	            new String[]{contact_id},
	            null);
	
	    while (cursor.moveToNext()) {
	        starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED));

	        System.out.println("isStarredStatus:" + starred);
	    }
	    
	    if (starred == 1)
	    {
	    	boolStarred = true;
	    	System.out.println("boolStarred = true");
	    }
	    else if (starred ==  0)
	    {
	    	boolStarred = false;
	    	System.out.println("boolStarred = false");
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
 
	}


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
        //photoCur.close();
        
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

	private void getIMInfo(String contact_id) {
		
		int count = 0;
		
		// View
        lblIMInfo = (TextView) findViewById(R.id.c_detail_im_header);
        lblIMInfo.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout imLayout = (LinearLayout) findViewById(R.id.c_detail_im_layout);
		
		// Logic 
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
            
            // Debug
            System.out.println("IM: " + im + "\tType: " + imType);
            
            if (im != null && !im.isEmpty())
            {
                ////System.out.println("IM - NOT NULL");
            	imLayout.setVisibility(View.VISIBLE);
            }
        }
        //imCur.close();
        
        /*
         * IM is now in object, now populate fields
         */
		final int N = count; // total number of textviews to add
        final TextView[] imTextViews = new TextView[N]; // create an empty array;
        String currentIM = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++) {
	    	// create a new textview
	        final TextView imTextView = new TextView(this);
	        final TextView imTypeTextView = new TextView(this);
	        final LinearLayout imContentLayout = new LinearLayout(this);
	                    
	        StringTokenizer tokens = new StringTokenizer(contact.getIMByIndex(i), ":");            
	        
	        currentIM = tokens.nextToken();;
	        currentType = tokens.nextToken();
	           
	        // set some properties of phoneTypeTextView
	        imTypeTextView.setText(currentType);
	        imTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
	        imTypeTextView.setTextSize(15);
	        imTypeTextView.setWidth(200);
	        imTypeTextView.setPadding(0, 10, 0, 10);
	        imTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

	        imContentLayout.addView(imTypeTextView);

            // set some properties of phoneNumberTextView
            imTextView.setText(currentIM);
            imTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            imTextView.setTextSize(20);
            imTextView.setPadding(30, 10, 0, 10);
            imTextView.setEllipsize(TextUtils.TruncateAt.END);
	        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	        	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        
	        final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
	
	        layoutParams.setMargins(margin, 0, margin, 0);
	        
	        imContentLayout.setLayoutParams(layoutParams);
	        imContentLayout.addView(imTextView);
	                    
	        // add the textview to the linearlayout
	        imLayout.addView(imContentLayout);
	        
	        // save a reference to the textview for later
	        imTextViews[i] = imTextView;
        }
	}

	private void getRelationshipInfo(String contact_id) {
		
		// View
        lblRelationship = (TextView) findViewById(R.id.c_detail_relationship_header);
        lblRelationship.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        LinearLayout relationshipLayout = (LinearLayout) findViewById(R.id.c_detail_relationship_layout);
        
		// Logic
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
            
            // DEBUG
            //System.out.println("Relationship: " + relationship + "\tType: " + relationshipType);
            
            if (relationship != null && !relationship.isEmpty())
            {
                ////System.out.println("Relationship - NOT NULL");
            	relationshipLayout.setVisibility(View.VISIBLE);
            }
        }
        //relationshipCur.close();
        
        final TextView lblRelationshipType = new TextView(this);
        final TextView lblRelationshipContent = new TextView(this);
        final LinearLayout relationshipContentLayout = new LinearLayout(this);

        // set some properties of phoneTypeTextView
        lblRelationshipType.setText(contact.getRelationshipType());
        lblRelationshipType.setTypeface(Typeface.createFromAsset(getAssets(), font));
        lblRelationshipType.setTextSize(15);
        lblRelationshipType.setWidth(200);
        lblRelationshipType.setPadding(0, 10, 0, 10);
        lblRelationshipType.setEllipsize(TextUtils.TruncateAt.END);


        // set some properties of phoneNumberTextView
        lblRelationshipContent.setText(contact.getRelationship());
        lblRelationshipContent.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
        lblRelationshipContent.setTextSize(21);
        lblRelationshipContent.setPadding(30, 10, 0, 10);
        lblRelationshipContent.setEllipsize(TextUtils.TruncateAt.END);
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
       	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
       
       final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

       layoutParams.setMargins(margin, 0, margin, 0);
       
       relationshipContentLayout.setLayoutParams(layoutParams);
       relationshipContentLayout.addView(lblRelationshipType);
       relationshipContentLayout.addView(lblRelationshipContent);
                   
       // add the textview to the linearlayout
       relationshipLayout.addView(relationshipContentLayout);   
	}

	private void getDatesInfo(String contact_id) {
		
		int count = 0;
		
		// View
        lblDate = (TextView) findViewById(R.id.c_detail_date_header);
        lblDate.setTypeface(Typeface.createFromAsset(getAssets(), font));
		
        LinearLayout dateLayout = (LinearLayout) findViewById(R.id.c_detail_date_layout);
        
		// Logic
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
            
            // DEBUG
            //System.out.println("Date: " + date + "\tType: " + dateType);
            
            if (date != null && !date.isEmpty())
            {
                ////System.out.println("DATE - NOT NULL");
            	dateLayout.setVisibility(View.VISIBLE);
            }
            
        }
        //dateCur.close();	
        
        /*
         * Date is now in object, now populate fields
         */
		final int N = count; // total number of textviews to add
        final TextView[] dateTextViews = new TextView[N]; // create an empty array;
        String currentDate = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++) {
	    	// create a new textview
	        final TextView dateTextView = new TextView(this);
	        final TextView dateTypeTextView = new TextView(this);
	        final LinearLayout dateContentLayout = new LinearLayout(this);
	                    
	        StringTokenizer tokens = new StringTokenizer(contact.getDatesByIndex(i), ":");            
	        
	        currentDate = tokens.nextToken();;
	        currentType = tokens.nextToken();
	           
	        // set some properties of phoneTypeTextView
	        dateTypeTextView.setText(currentType);
	        dateTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
	        dateTypeTextView.setTextSize(15);
	        dateTypeTextView.setWidth(200);
	        dateTypeTextView.setPadding(0, 10, 0, 10);
	        dateTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            dateContentLayout.addView(dateTypeTextView);

            // set some properties of phoneNumberTextView
            dateTextView.setText(currentDate);
            dateTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            dateTextView.setTextSize(21);
            dateTextView.setPadding(30, 10, 0, 10);
            dateTextView.setEllipsize(TextUtils.TruncateAt.END);
	  
	        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	        	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        
	        final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
	
	        layoutParams.setMargins(margin, 0, margin, 0);
	        
	        dateContentLayout.setLayoutParams(layoutParams);
	        dateContentLayout.addView(dateTextView);
	                    
	        // add the textview to the linearlayout
	        dateLayout.addView(dateContentLayout);
	        
	        // save a reference to the textview for later
	        dateTextViews[i] = dateTextView;
        }
	}

	private void getNotesInfo(String contact_id) {

		// View
        lblNote = (TextView) findViewById(R.id.c_detail_note_header);
        lblNote.setTypeface(Typeface.createFromAsset(getAssets(), font));
        		
        lblNoteContent = (TextView) findViewById(R.id.c_detail_note_content);
        lblNoteContent.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
        
        LinearLayout noteLayout = (LinearLayout) findViewById(R.id.c_detail_note_layout);
        
		// Logic
		ContentResolver cr = getContentResolver();
		
        String noteWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{contact_id,
        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        
        Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
        startManagingCursor(noteCur);

        while (noteCur.moveToNext()) {
            note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            contact.setNotes(note);
            
            // DEBUG
            //System.out.println("Note: " + note);
            
            if (note != null || !note.isEmpty())
            {
                System.out.println("NOTE - NOT NULL");
            	noteLayout.setVisibility(View.VISIBLE);
            }
        }
        //noteCur.close();
        lblNoteContent.setText(note);
	}

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
            
            // DEBUG
            //System.out.println("Company: " + company);
        }
        //orgCur.close();
        lblCompany.setText(company);
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
            
            // Don't need to show type in actual layout
            contact.addWebsites(website + "$" + websiteType);  
            count++;
            
            // DEBUG
            //System.out.println("Website: " + website + "\tType: " + websiteType);
            
            if (website != null)
            {
                ////System.out.println("WEBSITE - NOT NULL");
            	websiteLayout.setVisibility(View.VISIBLE);
            }
        }
        //webCur.close();
        
        /*
         * Date is now in object, now populate fields
         */
		final int N = count; // total number of textviews to add
        final TextView[] websiteTextViews = new TextView[N]; // create an empty array;
        String currentWebsite = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
        	// create a new textview
            final TextView websiteTextView = new TextView(this);
            final TextView websiteTypeTextView = new TextView(this);
            final LinearLayout websiteContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getWebsiteByIndex(i), "$");            
            
            currentWebsite = tokens.nextToken();;
            currentType = tokens.nextToken();
               
            // set some properties of phoneTypeTextView
            websiteTypeTextView.setText(currentType);
            websiteTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            websiteTypeTextView.setTextSize(15);
            websiteTypeTextView.setWidth(200);
            websiteTypeTextView.setPadding(0, 10, 0, 10);
            websiteTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            websiteContentLayout.addView(websiteTypeTextView);

            // set some properties of phoneNumberTextView
            websiteTextView.setText(currentWebsite);
            websiteTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            websiteTextView.setTextSize(21);
            websiteTextView.setPadding(30, 10, 0, 10);
            websiteTextView.setEllipsize(TextUtils.TruncateAt.END);
      
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            websiteContentLayout.setLayoutParams(layoutParams);
            websiteContentLayout.addView(websiteTextView);
                        
            // add the textview to the linearlayout
            websiteLayout.addView(websiteContentLayout);
            
            // save a reference to the textview for later
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
            try {
				addresses = geocoder.getFromLocationName(address, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(addresses.size() > 0) {
                latitude= addresses.get(0).getLatitude();
                longitude= addresses.get(0).getLongitude();
            }
            
            final LatLng latlng = new LatLng(latitude , longitude);
            Marker oneMarker = googleMap.addMarker(new MarkerOptions().position(latlng).title(addressType)); 
            
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            googleMap.animateCamera( CameraUpdateFactory.zoomTo( 20.0f ) );    

            count++;

            // DEBUG
            //System.out.println("Address: " + address + "\tType: " + addressType);
            
            if (address != null)
            {
                ////System.out.println("WEBSITE - NOT NULL");
            	addressLayout.setVisibility(View.VISIBLE);
            }
        }
        //addrCur.close();
        
        /*
         * Address is now in object, now populate fields
         */
		final int N = count; // total number of textviews to add
        final TextView[] addressTextViews = new TextView[N]; // create an empty array;
        String currentAddress = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
        	// create a new textview
            final TextView addressTextView = new TextView(this);
            final TextView addressTypeTextView = new TextView(this);
            final LinearLayout addressContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getAddressByIndex(i), ":");            
            
            currentAddress = tokens.nextToken();
            currentType = tokens.nextToken();
               
            // set some properties of phoneTypeTextView
            addressTypeTextView.setText(currentType);
            addressTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            addressTypeTextView.setTextSize(15);
            addressTypeTextView.setWidth(200);
            addressTypeTextView.setPadding(0, 10, 0, 10);
            addressTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            addressContentLayout.addView(addressTypeTextView);

            // set some properties of phoneNumberTextView
            addressTextView.setText(currentAddress);
            addressTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            addressTextView.setTextSize(21);
            addressTextView.setPadding(30, 10, 0, 10);
            addressTextView.setEllipsize(TextUtils.TruncateAt.END);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            addressContentLayout.setLayoutParams(layoutParams);
            addressContentLayout.addView(addressTextView);
                        
            // add the textview to the linearlayout
            addressLayout.addView(addressContentLayout);
            
            // save a reference to the textview for later
            addressTextViews[i] = addressTextView;
            
            addressTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	String addressQuery = addressTextView.getText().toString();
                	Intent geoIntent = new Intent (android.content.Intent.ACTION_VIEW, Uri.parse ("geo:0,0?q=" + addressQuery)); // Prepare intent
                	startActivity(geoIntent); // Initiate lookup
                }
            });
        }
	}


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
            
            // DEBUG
            //System.out.println("Email: " + email + "\tType: " + emailType);
            
            if (email != null)
            {
                ////System.out.println("WEBSITE - NOT NULL");
            	emailLayout.setVisibility(View.VISIBLE);
            }
        }
        //emailCur.close();	
      
        /*
         * Email is now in object, now populate fields
         */
        final int N = count; // total number of textviews to add
        final TextView[] emailTextViews = new TextView[N]; // create an empty array;
        String currentEmail = null;
        String currentType = null;
        
        for (int i = 0; i < N; i++){
        	// create a new textview
            final TextView emailTextView = new TextView(this);
            final TextView emailTypeTextView = new TextView(this);
            final LinearLayout emailContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getEmaiIDByIndex(i), ":");            
            
            currentEmail = tokens.nextToken();;
            currentType = tokens.nextToken();
               
            // set some properties of phoneTypeTextView
            emailTypeTextView.setText(currentType);
            emailTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            emailTypeTextView.setTextSize(15);
            emailTypeTextView.setWidth(200);
            emailTypeTextView.setPadding(0, 10, 0, 10);
            emailTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            emailContentLayout.addView(emailTypeTextView);

            // set some properties of phoneNumberTextView
            emailTextView.setText(currentEmail);
            emailTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            emailTextView.setTextSize(21);
            emailTextView.setPadding(30, 10, 0, 10);
            emailTextView.setEllipsize(TextUtils.TruncateAt.END);

            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            layoutParams.setMargins(margin, 0, margin, 0);
            
            emailContentLayout.setLayoutParams(layoutParams);
            emailContentLayout.addView(emailTextView);
                        
            // add the textview to the linearlayout
            emailLayout.addView(emailContentLayout);
            
            // save a reference to the textview for later
            emailTextViews[i] = emailTextView;
            
            emailTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                	Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
    		                "mailto",emailTextView.getText().toString(), null));
                	//TODO: change domain name signature
                	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Contakts for Android");
    		    	startActivity(Intent.createChooser(emailIntent, "Send Email"));
                }
            });
        }
	}


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
            
            // DEBUG
            //System.out.println("Name: " + name);
        }
        //nameCur.close();
        lblName.setText(name);
        lblName.setEllipsize(TextUtils.TruncateAt.END);
        ActionBar ab = getActionBar();
        ab.setTitle(name);
	}


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
            
            // DEBUG
            //System.out.println("Phone: " + number + "\tType: " + phoneType);
            
            if (number != null)
            {
                ////System.out.println("WEBSITE - NOT NULL");
            	phoneLayout.setVisibility(View.VISIBLE);
            }
        }
        //phoneCur.close();    
   
        /*
         * Phone is now in object, now populate fields
         */
        final int N = count; // total number of textviews to add
        final TextView[] phoneTextViews = new TextView[N]; // create an empty array;
        String currentPhone = null;
        String currentType = null;
        String unformattedNumber = null;
        
        for (int i = 0; i < N; i++){
        	// create a new textview
            final TextView phoneNumberTextView = new TextView(this);
            final TextView phoneTypeTextView = new TextView(this);
            final LinearLayout phoneContentLayout = new LinearLayout(this);
                        
            StringTokenizer tokens = new StringTokenizer(contact.getPhoneByIndex(i), ":");
            unformattedNumber = tokens.nextToken();
            
            currentPhone = PhoneNumberUtils.formatNumber(unformattedNumber);
            currentType = tokens.nextToken();
               
            // set some properties of phoneTypeTextView
            phoneTypeTextView.setText(currentType);
            phoneTypeTextView.setTypeface(Typeface.createFromAsset(getAssets(), font));
            phoneTypeTextView.setTextSize(15);
            phoneTypeTextView.setWidth(200);
            phoneTypeTextView.setPadding(0, 10, 0, 10);
            phoneTypeTextView.setEllipsize(TextUtils.TruncateAt.END);

            
            phoneContentLayout.addView(phoneTypeTextView);

            // set some properties of phoneNumberTextView
            phoneNumberTextView.setText(currentPhone);
            phoneNumberTextView.setTypeface(Typeface.createFromAsset(getAssets(), fontContent));
            phoneNumberTextView.setTextSize(21);
            phoneNumberTextView.setPadding(30, 10, 0, 10);
            phoneNumberTextView.setEllipsize(TextUtils.TruncateAt.END);

                       
            phoneContentLayout.addView(phoneNumberTextView);
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            	     LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            
            final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

            
            layoutParams.setMargins(margin, 0, margin, 0);
            phoneContentLayout.setLayoutParams(layoutParams);
                        
            phoneLayout.addView(phoneContentLayout);
            
            // save a reference to the textview for later
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
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_edit:
	    		Intent i = new Intent(Intent.ACTION_EDIT);
	    		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contact_id)); 
	    		i.setData(contactUri);
	    	    i.putExtra("finishActivityOnSaveCompleted", true);

	    		startActivity(i);

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

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
