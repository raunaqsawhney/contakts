package com.raunaqsawhney.contakts;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class WelcomeActivity extends Activity {

	private String ownerPhoto;
	int i = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		setupGlobalPrefs();
		setupActionBar();
		getPersonProfile();
	}

	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
        Boolean firstRunDone = false;
        firstRunDone = prefs.getBoolean("firstRunDone", false);
        
        if (firstRunDone) {
        	Intent firstRunDoneIntent = new Intent(WelcomeActivity.this, MainActivity.class);
  		   	WelcomeActivity.this.startActivity(firstRunDoneIntent);
  		   	finish();
        } else {
        	edit.putBoolean("firstRunDone", true);
    		edit.putString("font","RobotoCondensed-Regular.ttf");
    		edit.putString("fontContent","Roboto-Light.ttf");
    		edit.putString("fontTitle", "Harabara.ttf");
    		edit.apply();
        }		
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#34AADC")));
        bar.setDisplayShowHomeEnabled(false);
       
        // Do Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        
	        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
	        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, -150, 0,0);
	        config.getPixelInsetBottom();
	        
	        int actionBarColor = Color.parseColor("#34AADC");
	        tintManager.setStatusBarTintColor(actionBarColor);
        }		
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void getPersonProfile() {
		
		String ownerName = null;
		Cursor ownerCur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI,null,
                null,
                null, null);
        startManagingCursor(ownerCur);

    	while (ownerCur.moveToNext()) {
        	ownerName = ownerCur.getString(
            		ownerCur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));   
        }
        
        Cursor ownerPhotoCur = getContentResolver().query(ContactsContract.Profile.CONTENT_URI,null,
                null,
                null, null);
        startManagingCursor(ownerCur);
        
        while (ownerPhotoCur.moveToNext()) {
        	ownerPhoto = ownerPhotoCur.getString(
        			ownerPhotoCur.getColumnIndex(ContactsContract.Profile.PHOTO_URI));   
        }
        
        TextView ownerTV = (TextView) findViewById(R.id.c_welcome_text);
        
        if (ownerName == null)
            ownerTV.setText("Hello!");
        else 
            ownerTV.setText("Hello, " + ownerName +"!");

        ImageView ownerIV = (ImageView) findViewById(R.id.c_welcome_photo);
        try {
            ownerIV.setImageURI(Uri.parse(ownerPhoto));
        } catch (NullPointerException e) {
            ownerIV.setImageURI(null);
        }
        
        TextView infoTV = (TextView) findViewById(R.id.c_welcome_info);
        infoTV.setText(R.string.welcomeInfo);
        infoTV.setGravity(Gravity.FILL_HORIZONTAL);
        
        Button welcomeButton = (Button) findViewById(R.id.c_welcome_button);
        welcomeButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
            	
            	final ProgressBar mProgressBar;
                final TextView mProgressText;

            	CountDownTimer mCountDownTimer;
            	
            	mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
            	mProgressText = (TextView) findViewById(R.id.progress_text);
            	mProgressText.setVisibility(View.VISIBLE);
            	mProgressBar.setVisibility(View.VISIBLE);
            	mProgressBar.setIndeterminate(true);
            	
            	mProgressBar.setProgress(i);
            	mCountDownTimer = new CountDownTimer(4000, 1000) {

            	        @Override
            	        public void onTick(long millisUntilFinished) {
            	            i++;
            	            mProgressBar.setProgress(i);
            	        }

            	        @Override
            	        public void onFinish() {
            	            i++;
            	            mProgressBar.setProgress(i);
            	            Intent welcomeIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                  		   	WelcomeActivity.this.startActivity(welcomeIntent);
                  		   	finish();
            	        }
            	    };
            	    mCountDownTimer.start();
            }
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}
}
