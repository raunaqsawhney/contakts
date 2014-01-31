package com.raunaqsawhney.contakts;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;

public class LinkedInActivity extends Activity {

	private SocialAuthAdapter adapter;
	private Button linkedin_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_linked_in);
		
		adapter = new SocialAuthAdapter(new ResponseListener());
		adapter.authorize(LinkedInActivity.this, Provider.LINKEDIN);
	}
	
	private final class ResponseListener implements DialogListener 
	{
	   public void onComplete(Bundle values) {
	       Log.d("Custom -UI" , "Authentication Successful");
	       adapter.updateStatus("Test", null, false);  
	       
	   }

		@Override
		public void onBack() {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub
			
		} 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.linked_in, menu);
		return true;
	}
}
