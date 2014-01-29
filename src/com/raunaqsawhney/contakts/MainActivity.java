package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.meetme.android.horizontallistview.HorizontalListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends Activity implements OnQueryTextListener, LoaderCallbacks<Cursor>, OnNavigationListener {

    private HorizontalListView mHlvSimpleList;

	
	/*
	 * Declare Globals
	 */
	String theme = "#18A7B5";
	String font = "RobotoCondensed-Regular.ttf";

	SimpleCursorAdapter mAdapter;
	String mFilter;
	String photoURI;
	ListView contactList;
	String itemid;
	
   @Override
   public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);	

        /*
         * Set up the Action Bar
         */
        int titleId = getResources().getIdentifier("action_bar_title", "id",
                "android");
        TextView actionBarTitleText = (TextView) findViewById(titleId);
        Typeface actionBarFont = Typeface.createFromAsset(getAssets(), "Harabara.ttf");
        actionBarTitleText.setTypeface(actionBarFont);
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(24);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
       
        ArrayList<String> itemList = new ArrayList<String>();
        itemList.add("Phone");
        itemList.add("Google");
        itemList.add("Facebook");
        itemList.add("Twitter");
        itemList.add("LinkedIn");
        itemList.add("Dialer");
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
        bar.setListNavigationCallbacks(aAdpt, this);

        /*
         * Do Title Bar Tint only if KITKAT
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        // Holo light action bar color is #DDDDDD
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }
        
        
        contactList = (ListView)findViewById(R.id.list);
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor)parent.getItemAtPosition(position);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));		      
				
				// Explicit Intent Example
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                startActivity(intent);
		        
            }
        });
	        
        /*
         * Fetch the name and contact photo uri
         */
	        
        String[] from = new String[] {
        		ContactsContract.Data.DISPLAY_NAME,
        		ContactsContract.Data.PHOTO_URI
        };
        
        
        
        int[] to = new int[] {
        		R.id.c_name,
        		R.id.c_photo
        };
	        
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        
        /*
	     * Indeterminate Progress Bar for not found contacts
	     */
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        contactList.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
	        
	    LoaderManager loaderManager = getLoaderManager();
	    loaderManager.initLoader(0, null, this);	
	    
        contactList.setAdapter(mAdapter);
        
        setupFavList();
       
	}

	private void setupFavList() {

		ImageView favIcon = (ImageView) findViewById(R.id.fav_photo);
		
		Uri queryUri = ContactsContract.Contacts.CONTENT_URI;

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.LOOKUP_KEY,
	            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
	            ContactsContract.Contacts.STARRED};

	    String selection =ContactsContract.Contacts.STARRED + "='1'";

	    @SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(queryUri, projection, selection,null,null);

	    long id= cursor.getColumnIndex(ContactsContract.Contacts._ID);
	    
	    Bitmap bitmap = loadContactPhoto(getContentResolver(), id);
	    if(bitmap!=null){
	    favIcon.setImageBitmap(bitmap);
	    }
	    else{

	    }
	    
	    String[] from = {ContactsContract.Contacts.Photo.PHOTO_THUMBNAIL_URI};
	    int to[] = new int[]{
	    		R.id.fav_photo,
	    };

	    ListAdapter adapter = new SimpleCursorAdapter(
	            this,
	            R.layout.fav_layout,
	            cursor,
	            from,
	            to,
	            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
        final HorizontalListView mHlvSimpleList = (HorizontalListView) findViewById(R.id.HorizontalListView);
        
        // Assign adapter to the HorizontalListView
        mHlvSimpleList.setAdapter(adapter);
	
}

	private Bitmap loadContactPhoto(ContentResolver contentResolver, long id) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
	    if (input == null) {

	        return null;
	    }
	    return BitmapFactory.decodeStream(input);		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.options_menu, menu);
    	
		/*
		* Set up the Action Bar Menu
		*/
                  
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("Find contacts");
        searchView.setQueryHint(Html.fromHtml("<font color = #F7F7F7>" + getResources().getString(R.string.search_hint) + "</font>"));
        
        searchView.setOnQueryTextListener(this);        
        
        AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        search_text.setTextColor(Color.WHITE);
        search_text.setTypeface(Typeface.createFromAsset(getAssets(), font));
        
        
        return true;

    
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri baseUri;
        if (mFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mFilter));
        } else {
            baseUri = ContactsContract.Contacts.CONTENT_URI;
        }
        
        String query = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
        
        
        /*
        String photoURI = null;
        int imageResource = getResources().getIdentifier("@drawable/ic_contact_picture", null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
         */

        String[] projection = new String[] {
        	ContactsContract.Contacts._ID,
        	ContactsContract.Contacts.DISPLAY_NAME,
        	ContactsContract.Contacts.PHOTO_URI
        };
        
        
        //System.out.println(photoURI);
		System.out.println(ContactsContract.Contacts.PHOTO_URI);

        CursorLoader cursorLoader = new CursorLoader(
        		MainActivity.this, 
        		baseUri,
                projection, 
                query, 
                null,
                Contacts.DISPLAY_NAME);
        
        return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
		System.out.println(ContactsContract.Contacts.PHOTO_URI);
		if (ContactsContract.Contacts.PHOTO_URI == null)
		{
			ImageView c_photo = (ImageView) findViewById(R.id.c_photo);
			c_photo.setImageResource(R.drawable.ic_contact_picture);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);		
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		mFilter = !TextUtils.isEmpty(arg0) ? arg0 : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	            createNewContact();
	            return true;    
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	}

	private void createNewContact() {
		Intent intent = new Intent(Intent.ACTION_INSERT, 
                ContactsContract.Contacts.CONTENT_URI);
		startActivity(intent);	
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {

		switch(arg0)
        {
        	case 0:
        		// Show default (Phone included) list
        		System.out.println("PHONE");
        		break;
        	case 1:
        		// Show all Google synced contacts
        		System.out.println("GOOGLE");
        		Intent gIntent = new Intent(MainActivity.this, GoogleActivity.class);
        		MainActivity.this.startActivity(gIntent);
        		break;
        	case 2:
        		// Show Facebook contacts 
        		//System.out.println("CALL DATA");
        		//Intent fbIntent = new Intent(MainActivity.this, CallDataActivity.class);
        		//MainActivity.this.startActivity(fbIntent);
        		break;
        	case 3:
        		// Show Twitter contacts
        		System.out.println("TWITTER");
        		break;
        	case 4:
        		// Show LinkedIn contacts
        		System.out.println("LINKEDIN");
        	case 5:
        		//Intent liIntent = new Intent(MainActivity.this, LinkedInLoginActivity.class);
        		//MainActivity.this.startActivity(liIntent);
    		default:
    			break;
        }

		return false;
	}
}