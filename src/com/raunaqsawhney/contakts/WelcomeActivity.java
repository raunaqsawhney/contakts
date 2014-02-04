package com.raunaqsawhney.contakts;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class WelcomeActivity extends Activity {

	private String ownerPhoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(getAssets(), "Harabara.ttf"));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#34AADC")));
        bar.setDisplayShowHomeEnabled(false);
       
        // Do Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        tintManager.setNavigationBarTintEnabled(true);
	        int actionBarColor = Color.parseColor("#34AADC");
	        tintManager.setStatusBarTintColor(actionBarColor);
	        tintManager.setNavigationBarTintColor(Color.parseColor("#000000"));
        }
		
		getPersonProfile();
	}

	@SuppressLint("NewApi")
	private void getPersonProfile() {
		
		String ownerName = null;

		ContentResolver cr = getContentResolver();
		
		Cursor ownerCur = cr.query(ContactsContract.Profile.CONTENT_URI,null,
                null,
                null, null);
        startManagingCursor(ownerCur);

        while (ownerCur.moveToNext()) {
        	ownerName = ownerCur.getString(
            		ownerCur.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));   
        	System.out.println(ownerName);
        }
        
		Cursor ownerPhotoCur = cr.query(ContactsContract.Profile.CONTENT_URI,null,
                null,
                null, null);
        startManagingCursor(ownerCur);
        
        while (ownerPhotoCur.moveToNext()) {
        	ownerPhoto = ownerPhotoCur.getString(
        			ownerPhotoCur.getColumnIndex(ContactsContract.Profile.PHOTO_URI));   
        	System.out.println(ownerName);
        }
        
        TextView ownerTV = (TextView) findViewById(R.id.c_welcome_text);
        ownerTV.setTextAlignment(4);
        ownerTV.setText("Hello, " + ownerName +"!");
        
        ImageView ownerIV = (ImageView) findViewById(R.id.c_welcome_photo);
        ownerIV.setImageURI(Uri.parse(ownerPhoto));
        
        TextView infoTV = (TextView) findViewById(R.id.c_welcome_info);
        infoTV.setText("Contakts is a beautiful new way to connect with everyone" +
        		" that matters to you.\n\n" + "Just swipe right anywhere on the screen to open the navigation menu," +
        				" and explore your contacts!");
        
        Button welcomeButton = (Button) findViewById(R.id.c_welcome_button);
        welcomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent welcomeIntent = new Intent(WelcomeActivity.this, MainActivity.class);
      		   	WelcomeActivity.this.startActivity(welcomeIntent);
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
