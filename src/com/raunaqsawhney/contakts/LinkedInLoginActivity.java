package com.raunaqsawhney.contakts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LinkedInLoginActivity extends Activity {
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.linked_in_login);
	    
	    Button loginBtn = (Button) findViewById(R.id.btAuthenticate);
	    
	    loginBtn.setOnClickListener(authenticateUser);
	}
	
	View.OnClickListener authenticateUser = new View.OnClickListener() {
	    public void onClick(View v) {
	    	Intent authIntent = new Intent(LinkedInLoginActivity.this, LinkedInOAuthActivity.class);
	    	LinkedInLoginActivity.this.startActivity(authIntent);
	    	startActivity(authIntent);
	    }
	  };
}
