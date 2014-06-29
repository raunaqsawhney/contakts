package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ShuffleActivity extends Activity implements OnItemClickListener {
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	private boolean firstRunDoneShuffle;
	
	String contact_id;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	SimpleCursorAdapter mAdapter;
	
	Cursor c;
	String number;
	Contact contact = new Contact();
	
	int count = 0;
	ArrayList<String> shuffleContacts = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shuffle);
		
		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		
		
		shuffleItUp();

	}
	

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneShuffle = prefs.getBoolean("firstRunDoneShuffle", false);
        if (!firstRunDoneShuffle) {
        	edit.putBoolean("firstRunDoneShuffle", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.shuffleHeader))
		    .setMessage(getString(R.string.shuffleText))
		    .setNeutralButton(getString(R.string.okay), null)
		    .show();
        }
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), fontContent));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        actionBarTitleText.setText(getString(R.string.sMShuffle).toUpperCase());
        
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
      
        final String[] nav = { 
        		getString(R.string.dialer).toUpperCase(),
        		getString(R.string.sMfavourites).toUpperCase(),
        		getString(R.string.sMRecent).toUpperCase(),
				getString(R.string.sMMostContacted).toUpperCase(),
				getString(R.string.sMPhoneContacts).toUpperCase(),
				getString(R.string.sMGroups).toUpperCase(),
				getString(R.string.sMShuffle).toUpperCase(),
				getString(R.string.sMFacebook).toUpperCase(),
				getString(R.string.sMSettings).toUpperCase()
		};
		
		final Integer[] navPhoto = { 
				R.drawable.ic_nav_dial,
				R.drawable.ic_nav_star,
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
	
	private void shuffleItUp() {

		String contact_id = null;
		
		String [] projection = new String [] {  ContactsContract.Contacts._ID };
		
		String selection = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
		
		c = getContentResolver().query(
	            ContactsContract.Contacts.CONTENT_URI,  
	            projection,
	            selection,
	            null,
	            null);
 
		while (c.moveToNext()) {
			contact_id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
	        shuffleContacts.add(contact_id);

	        ++count;
		}
		
		Random rnd = new Random();
		int randno = rnd.nextInt(count);
		
		showShuffledContact(shuffleContacts.get(randno).toString());
	}


	private void showShuffledContact(final String contact_id) {
		
		String name = null;
		String photo = null;
		Long last_time_contacted = null;
		Integer time_contacted = null;
		String organization = null;
		
		c = null;
		c = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				null,
                ContactsContract.Contacts._ID + " =? ",
                new String[]{contact_id},
                null
        );
 
		while (c.moveToNext()) {
			name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			
			photo = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
						
			last_time_contacted = c.getLong(c.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
			time_contacted = c.getInt(c.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
		}
		
		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{contact_id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        c = null;
		c = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);

        while (c.moveToNext()) {
            organization = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));            
        }
        
        TextView nameTV = (TextView) findViewById(R.id.shuffle_name);
		nameTV.setText(name);
		
		TextView companyTV = (TextView) findViewById(R.id.shuffle_company);
		companyTV.setText(organization);
		
		ImageView photoIV = (ImageView) findViewById(R.id.shuffle_photo);
		
		try {
			photoIV.setImageURI(Uri.parse(photo));
		} catch (NullPointerException e) {
			e.printStackTrace();
			photoIV.setImageURI(null);
		}
		
		ImageView headerBG = (ImageView) findViewById(R.id.shuffle_header_bg);
        
        InputStream inputStream;
        try {
        	inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contact_id)));
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        	inputStream = null;
        }
        
        try {
        	if (inputStream != null) {
            	
        		headerBG.setImageBitmap(BlurImageLegacy(BitmapFactory.decodeStream(inputStream), 10));

            } else {
        		headerBG.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.default_bg));
            }
        } catch (OutOfMemoryError e) {
        	e.printStackTrace();
        } 

		/*
        Button viewProfileBtn = (Button) findViewById(R.id.shuffleProfileBtn);
		
		GradientDrawable drawable = new GradientDrawable();
	    drawable.setShape(GradientDrawable.RECTANGLE);
	    drawable.setStroke(1, Color.WHITE);
	    drawable.setColor(Color.TRANSPARENT);
	    viewProfileBtn.setBackgroundDrawable(drawable);
		
	
		viewProfileBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","fav");
                startActivity(intent);
            }
        });	
		*/
        
        
		SimpleDateFormat formatter = new SimpleDateFormat("dd LLLL yyyy", Locale.getDefault()); 
		String dateString = formatter.format(new Date(last_time_contacted));
		
		TextView last_contacted_header = (TextView) findViewById(R.id.shuffle_ls_contacted_header);
		last_contacted_header.setTextColor(Color.parseColor(theme));
		
		TextView last_contacted = (TextView) findViewById(R.id.shuffle_ls_contacted);
		
		String firstname = null;
		try {
	        StringTokenizer tokens = new StringTokenizer(name, " ");            
	        
	        firstname = tokens.nextToken();
        } catch (NoSuchElementException e) {
        	e.printStackTrace();
        }
		
		if (Long.valueOf(last_time_contacted).equals(Long.valueOf(0))) {
			last_contacted.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-LightItalic.ttf"));
			last_contacted.setText(getString(R.string.noContactYet1) + " " + firstname + ". " + getString(R.string.noContactYet2));
		} else {
			last_contacted.setText(dateString.toString());
		}
		
		TextView times_contacted_header = (TextView) findViewById(R.id.shuffle_tc_contacted_header);
		times_contacted_header.setTextColor(Color.parseColor(theme));
		
		TextView times_contacted = (TextView) findViewById(R.id.shuffle_tc_contacted);
		TextView times_contacted_descr = (TextView) findViewById(R.id.shuffle_tc_contacted_descr);
		
		if (time_contacted.equals(0)) {
			times_contacted.setVisibility(View.GONE);
			times_contacted_descr.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-LightItalic.ttf"));
			times_contacted_descr.setText(getString(R.string.noTimeContacted1) + " " + firstname + ". " + getString(R.string.noTimeContacted2));
			
		} else if (time_contacted >= 1 && time_contacted <= 20) {
			times_contacted.setText(time_contacted.toString());
			times_contacted_descr.setText(getString(R.string.rare));
			
		}  else if (time_contacted >= 21 && time_contacted <= 80) {
			times_contacted.setText(time_contacted.toString());
			times_contacted_descr.setText(getString(R.string.occasional));
			
		}   else if (time_contacted >= 81 && time_contacted <= 200) {
			times_contacted.setText(time_contacted.toString());
			times_contacted_descr.setText(getString(R.string.frequent));

		}   else if (time_contacted >= 201 && time_contacted <= 500) {
			times_contacted.setText(time_contacted.toString());
			times_contacted_descr.setText(getString(R.string.heavy));
			
		}   else if (time_contacted > 500) {
			times_contacted.setText(time_contacted.toString());
			times_contacted_descr.setText(getString(R.string.crazy));
			
		} else if (time_contacted.toString().isEmpty()) {
			times_contacted.setVisibility(View.GONE);
			times_contacted_descr.setText(getString(R.string.unavailable));
		}
		
		TextView sayHello = (TextView) findViewById(R.id.shuffleHello);
		sayHello.setText(getString(R.string.sayHello) + " " + firstname + "!");
		
		
		String randQuote = getRandomQuote();
		TextView quote = (TextView) findViewById(R.id.quote);
		quote.setTypeface(Typeface.createFromAsset(this.getAssets(), "RobotoSlab-Light.ttf"));
		quote.setText(randQuote);
		
		setupQuickButtons(contact_id);

	}
	
	private String getRandomQuote() {

		String randomQuote = null;
		
		ArrayList<String> quotes = new ArrayList<String>();
		
		quotes.add("Friendship is always a sweet responsibility, never an opportunity. - Khalil Gibran");
		quotes.add("The language of friendship is not words but meanings. - Henry David Thoreau");
		quotes.add("Friendship is the only cement that will ever hold the world together. - Woodrow T. Wilson");
		quotes.add("True friendship comes when the silence between two people is comfortable. - David Tyson Gentry");
		quotes.add("Friendship consists in forgetting what one gives and remembering what one receives. - Alexander Dumas");
		quotes.add("Be slow to fall into friendship; but when thou art in, continue firm & constant. - Socrates");
		quotes.add("Friendship is like money, easier made than kept. - Samuel Butler");
		quotes.add("I don't need a friend who changes when I change and who nods when I nod; my shadow does that much better. - Plutarch");
		quotes.add("Wishing to be friends is quick work, but friendship is a slow ripening fruit. - Aristotle");
		quotes.add("There is nothing better than a friend, unless it is a friend with chocolate. - Linda Grayson");
		quotes.add("A friend is someone who knows all about you and still loves you. - Elbert Hubbard");
		quotes.add("Do I not destroy my enemies when I make them my friends? - Abraham Lincoln");
		quotes.add("My best friend is the one who brings out the best in me. - Henry Ford");
		quotes.add("Love is blind; friendship closes its eyes. - Friedrich Nietzsche");
		quotes.add("Friends are the family you choose - Jess C. Scott");
		quotes.add("A friendship that can end never really began - Publilius Syrus");
		quotes.add("In prosperity our friends know us; in adversity we know our friends. - John Churton Collins");
		quotes.add("Some people go to priests, others to poetry, I to my friends. - Virginia Woolf");
		quotes.add("Friendship improves happiness, and abates misery, by doubling our joys, and dividing our grief. - Marcus Tullius Cicero");
		quotes.add("No person is your friend who demands your silence, or denies your right to grow. - Alice Walker");
		
		Random rnd = new Random();
		int randno = rnd.nextInt(20);
		
		randomQuote = quotes.get(randno);
		
		return randomQuote;
	}


	private void setupQuickButtons(final String contact_id) {

		// Profile Button
		ImageView viewProfileBtn = (ImageView) findViewById(R.id.shuffleProfilePhoto);

		viewProfileBtn.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
		        intent.putExtra("contact_id", contact_id);
		        intent.putExtra("activity","fav");
		        startActivity(intent);
			}
		});	
		
		//Phone Button
		ImageView callBtn = (ImageView) findViewById(R.id.shufflePhonePhoto);
		
		callBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

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
                    		count++;
                }
	        		
                if (count > 1) {
            		ListView lvDialog = new ListView(ShuffleActivity.this);
            		
            		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ShuffleActivity.this,android.R.layout.simple_list_item_1, allContacts);
            		lvDialog.setAdapter(arrayAdapter); 
            		
            		AlertDialog.Builder builder = new AlertDialog.Builder(ShuffleActivity.this);
            		
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
			}
		});
		
		//SMS Button
		ImageView smsBtn = (ImageView) findViewById(R.id.shuffleSMSPhoto);
		
		smsBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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
		            		count++;
		        }
		    		
		        if (count > 1) {
		        	ListView lvDialog = new ListView(ShuffleActivity.this);
		    		
		    		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ShuffleActivity.this,android.R.layout.simple_list_item_1, allContacts);
		    		lvDialog.setAdapter(arrayAdapter); 
		    		
		    		AlertDialog.Builder builder = new AlertDialog.Builder(ShuffleActivity.this);
		    		
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
			}
		});
		
		//EMAIL Button
		ImageView emailBtn = (ImageView) findViewById(R.id.shuffleEmailPhoto);
		
		emailBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				int count = 0;

				final ArrayList<String> allContacts = new ArrayList<String>();
		        
				c = null;
				c = getContentResolver().query(
		                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
		                null,
		                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
		                new String[]{contact_id},
		                null);

		        while (c.moveToNext()) {
		            allContacts.add(c.getString(
		            		c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))); 
		            		count++;
		        }
		    		
		        if (count > 1) {
		        	ListView lvDialog = new ListView(ShuffleActivity.this);
		    		
		    		ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(ShuffleActivity.this,android.R.layout.simple_list_item_1, allContacts);
		    		lvDialog.setAdapter(arrayAdapter); 
		    		
		    		AlertDialog.Builder builder = new AlertDialog.Builder(ShuffleActivity.this);
		    		
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
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.shuffle, menu);
		return true;
	}

	public Bitmap BlurImageLegacy(Bitmap input, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = input.copy(input.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_shuffle:
	        	Intent intent = new Intent(ShuffleActivity.this, ShuffleActivity.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			   	ShuffleActivity.this.startActivity(intent);
	            return true; 
	            
	        case android.R.id.home:
        		menu.toggle(true);
        		
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent dialIntent = new Intent(ShuffleActivity.this, DialerActivity.class);
		   	ShuffleActivity.this.startActivity(dialIntent);
	   } else if (selected == 1) {
		   Intent favIntent = new Intent(ShuffleActivity.this, FavActivity.class);
		   ShuffleActivity.this.startActivity(favIntent);
	   } else if (selected == 2) {
		   Intent recIntent = new Intent(ShuffleActivity.this, RecentActivity.class);
		   ShuffleActivity.this.startActivity(recIntent);
	   } else if (selected == 3) {
		   Intent freqIntent = new Intent(ShuffleActivity.this, GraphActivity.class);
		   ShuffleActivity.this.startActivity(freqIntent);
	   } else if (selected == 4) {
		   Intent phoneIntent = new Intent(ShuffleActivity.this, MainActivity.class);
		   ShuffleActivity.this.startActivity(phoneIntent);
	   }  else if (selected == 5) {
		   Intent fbIntent = new Intent(ShuffleActivity.this, GroupActivity.class);
		   ShuffleActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
			Intent loIntent = new Intent(ShuffleActivity.this, ShuffleActivity.class);
			ShuffleActivity.this.startActivity(loIntent);
	   }   else if (selected == 7) {
		   Intent iIntent = new Intent(ShuffleActivity.this, FBActivity.class);
		   ShuffleActivity.this.startActivity(iIntent);
	   } else if (selected == 8) {
		   Intent iIntent = new Intent(ShuffleActivity.this, LoginActivity.class);
		   ShuffleActivity.this.startActivity(iIntent);
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
