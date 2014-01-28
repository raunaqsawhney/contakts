package com.raunaqsawhney.contakts;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LinkedInOAuthActivity extends Activity {
	
	SharedPreferences prefs;
	OAuthService service;
	Token requestToken;
	WebView mWebView;
	
	final static String APIKEY = "75lgygyha7el9w";
	final static String APISECRET = "IztYohC0xBGjrDeI";
	final static String CALLBACK = "oauth://linkedin";

	@Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.linked_in_oauth);
	    
	    service = new ServiceBuilder()
	    	.provider(LinkedInApi.class)
	    	.apiKey(APIKEY)
	    	.apiSecret(APISECRET)
	    	.callback(CALLBACK)
	            .scope("r_basicprofile")
	            .scope("rw_nus")
            .build();
	    
	    mWebView = (WebView) findViewById(R.id.linkedin_webview);
	    LinkedInAuthTask task = new LinkedInAuthTask();
	    task.execute();
	}
	    
	private class LinkedInAuthTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			// Temporary URL
			String authURL = "http://api.linkedin.com/";

			System.out.println("Doing LIN Async Task");
			try {
			        requestToken = service.getRequestToken();
			 authURL = service.getAuthorizationUrl(requestToken);
			}
			catch ( OAuthException e ) {
			 e.printStackTrace();
			 return null;
			}

			return authURL;
		}
		
		protected void onPostExecute(String authURL) { 
		    mWebView.setWebViewClient(new WebViewClient() {

		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		 super.shouldOverrideUrlLoading(view, url);

		 if( url.startsWith("oauth") ) {
		            mWebView.setVisibility(WebView.GONE);

		     final String url1 = url;
		        Thread t1 = new Thread() {
		  public void run() {
		        Uri uri = Uri.parse(url1);

		      String verifier = uri.getQueryParameter("oauth_verifier");
		      Verifier v = new Verifier(verifier);
		      Token accessToken = service.getAccessToken(requestToken, v);          
		      Intent intent = new Intent();
		      intent.putExtra("access_token", accessToken.getToken());
		      intent.putExtra("access_secret", accessToken.getSecret());
		       setResult(RESULT_OK, intent);

		        finish();
		  }
		 };
		 t1.start();
		 }

		 return false;
		        }
		    });

		    mWebView.loadUrl(authURL);
		}
		
		/*
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		     super.onActivityResult(requestCode, resultCode, data);
		      System.out.println("ON ACTIVITY RESULT");

		     if ( resultCode == RESULT_OK && requestCode == LINKEDIN_LOGIN_RESULT_CODE ) {
		      String access_token = data.getStringExtra("access_token");
		      String access_secret = data.getStringExtra("access_secret");

		      // Store the tokens in preferences for further use
		      SharedPreferences.Editor editor = prefs.edit();
		      editor.putString("linkedin_access_token", access_token);
		      editor.putString("linkedin_access_secret", access_secret);
		      editor.commit();          

		      System.out.println("Starting LIN Intent");
		      // Start activity
		      Intent intent = new Intent(this, LinkedInListActivity.class);
		      startActivity(intent);   
		     }
		}*/
	}
}