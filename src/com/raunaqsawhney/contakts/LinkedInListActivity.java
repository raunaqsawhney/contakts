package com.raunaqsawhney.contakts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LinkedInListActivity extends Activity {

	LinkedInListActivity mContext;
	ArrayList<String> mConnections;
	ListView mLinkedInList;
	LinkedInAdapter mLinkedInListAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.linked_in_activity);

	    mContext = this;

	    mConnections = new ArrayList<String>();

	    // List and adapter
	    mLinkedInList = (ListView) findViewById(R.id.linkedInList); 
	    mLinkedInListAdapter = new LinkedInAdapter(this, mContext, mConnections);         
	    mLinkedInList.setAdapter(mLinkedInListAdapter);     

	 // Start async task to get connections
	    LinkedInConnectionsTask task = new LinkedInConnectionsTask();
	    task.execute();
	}

	//List adapter
	private static class LinkedInAdapter extends ArrayAdapter<String> {

	  private LinkedInListActivity mActivity = null;
	  private LayoutInflater mInflater = null;

	  static class ViewHolder {
	  TextView feedItemText;      
	  }         

	  public LinkedInAdapter(LinkedInListActivity activity, Context context,
	                  ArrayList<String> values) {
	     super(context, R.layout.linked_in_list_item, values);
	     this.mActivity = activity;

	      // Cache the LayoutInflate to avoid asking for a new one each time.
	      mInflater = LayoutInflater.from(context);
	  }  

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	      View v = convertView;
	      ViewHolder holder;

	      if ( v == null ) {
	          v = mInflater.inflate(R.layout.linked_in_list_item, null);
	          holder = new ViewHolder();
	          holder.feedItemText = (TextView) v.findViewById(R.id.feedItemText);
	          v.setTag(holder);
	      }
	      else {
	          holder = (ViewHolder) convertView.getTag();
	      }

	      if ( holder.feedItemText != null ) {      
	          holder.feedItemText.setText(mActivity.mConnections.get(position).toString());
	      } 

	      return v;
	  }     

	  public void add(String s) {
	      mActivity.mConnections.add(s);
	  }
	}
	
	public class LinkedInConnectionsTask extends AsyncTask<Void, Void, String> {

	    private String LINKED_IN_CONNECTIONS_API = "http://api.linkedin.com/v1/people/~/connections";
		
		@Override
		protected String doInBackground(Void... arg0) {
			String urlStr = LINKED_IN_CONNECTIONS_API;
	        urlStr += "?format=json";

	        OAuthService linkedInService = new ServiceBuilder()
	        .provider(LinkedInApi.class)
	        .apiSecret( LinkedInOAuthActivity.APISECRET )
	        .callback( LinkedInOAuthActivity.CALLBACK )
	        .build();
	        
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	        String access_token = prefs.getString("linkedin_access_token", null);
	        String access_secret = prefs.getString("linkedin_access_secret", null);

	        OAuthRequest request = new OAuthRequest(Verb.GET, urlStr);
	        Token t = new Token(access_token, access_secret);
	        linkedInService.signRequest(t, request);

	        Response response = null;
	        try {
	          response = request.send();
	         if ( response.isSuccessful() )
	          return response.getBody();
	         else
	          return null;
	        }
	        catch ( Exception e ) {
	         e.printStackTrace();
	         return null;
	        }
		}
		
		@Override
	    protected void onPostExecute(String result) {
	        JSONArray arr = null;
	        JSONObject object = null; 

	        try {
	            object = new JSONObject(result);
	            if ( object.has("values") ) {
	                arr = object.getJSONArray("values");
	            }
	            else {
	                return;
	            }
	        } catch (JSONException e1) {
	            e1.printStackTrace();
	            return;
	        }

	        JSONObject jsonObject = null; 
	        for ( int i = 0; i < arr.length(); i++) {
	            try {
	                jsonObject = arr.getJSONObject(i);
	            } catch (JSONException e1) {
	                e1.printStackTrace();
	            }

	            try {       
	                String fn = jsonObject.getString("firstName");
	                String ln = jsonObject.getString("lastName");
	                mLinkedInListAdapter.add(fn + " " + ln);
	            }
	            catch (JSONException e1) {
	                e1.printStackTrace();
	            }
	        }

	        mLinkedInListAdapter.notifyDataSetChanged();
	    }
	}

}
