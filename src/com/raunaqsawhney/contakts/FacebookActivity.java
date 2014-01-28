package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class FacebookActivity extends Activity {

	//String[] names = {};
	
    
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_facebook);

		ListView lvDialog = new ListView(this);
		final ArrayList<String> names = new ArrayList<String>();
		
		
		
	    // start Facebook Login
	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	    // callback when session changes state
		@Override
	      public void call(Session session, SessionState state, Exception exception) {
	        if (session.isOpened()) {
	 
	        	Request request = Request.newMyFriendsRequest(
	        			 Session.getActiveSession(),
	        			 new Request.GraphUserListCallback() {
	        			 
	        				 @Override
		        			 public void onCompleted(List<GraphUser> users, Response response) {

		                         for (int i=0; i<users.size();i++){
		                             names.add(i, users.get(i).getName());
		                             Log.d("USER", users.get(i).getName());
		                         }
	        				 
	        			 }});
	        			 request.executeAsync();
	        }
	      }
	    });
	    
	    ArrayAdapter<String> arrayAdapter =      
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, names);
        lvDialog.setAdapter(arrayAdapter); 
	    
       
	  }

	  @Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	  }
}

