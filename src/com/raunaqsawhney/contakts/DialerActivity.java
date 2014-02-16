package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class DialerActivity extends Activity implements OnItemClickListener {
	
	// Declare Globals
	String theme;
	String fontContent;
	String fontTitle;
	
    static final Pattern CODE_PATTERN = Pattern.compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");

	
	private SlidingMenu menu;
	private ListView navListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialer);
		
	    getActionBar().hide();

		
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        
        initalizeDialer();
	}
	
	private void initalizeDialer() {
		Button oneBtn = (Button) findViewById(R.id.one);
		oneBtn.setTextColor(Color.parseColor(theme));
		
		Button twoBtn = (Button) findViewById(R.id.two);
		twoBtn.setTextColor(Color.parseColor(theme));

		Button threeBtn = (Button) findViewById(R.id.three);
		threeBtn.setTextColor(Color.parseColor(theme));

		Button fourBtn = (Button) findViewById(R.id.four);
		fourBtn.setTextColor(Color.parseColor(theme));

		Button fiveBtn = (Button) findViewById(R.id.five);
		fiveBtn.setTextColor(Color.parseColor(theme));

		Button sixBtn = (Button) findViewById(R.id.six);
		sixBtn.setTextColor(Color.parseColor(theme));

		Button sevenBtn = (Button) findViewById(R.id.seven);
		sevenBtn.setTextColor(Color.parseColor(theme));

		Button eightBtn = (Button) findViewById(R.id.eight);
		eightBtn.setTextColor(Color.parseColor(theme));

		Button nineBtn = (Button) findViewById(R.id.nine);
		nineBtn.setTextColor(Color.parseColor(theme));

		Button starBtn = (Button) findViewById(R.id.star);
		starBtn.setTextColor(Color.parseColor(theme));

		Button zeroBtn = (Button) findViewById(R.id.zero);
		zeroBtn.setTextColor(Color.parseColor(theme));

		Button hashBtn = (Button) findViewById(R.id.hash);
		hashBtn.setTextColor(Color.parseColor(theme));

		Button callBtn = (Button) findViewById(R.id.call);
		
		Button clearBtn = (Button) findViewById(R.id.clear);
		clearBtn.setTextColor(Color.parseColor(theme));

		
	    final EditText numberEditText = (EditText) findViewById(R.id.number);
	    numberEditText.setTextColor(Color.parseColor(theme));
	    numberEditText.setBackgroundColor(0);
	    numberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
	    numberEditText.addTextChangedListener(new TextWatcher() {

	        @Override
	        public void afterTextChanged(Editable s) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {

	        	PhoneNumberUtils.formatNumber(numberEditText.getText().toString());
	        	
	        	Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberEditText.getText().toString()));
	        	String[] projection = new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME,
	        			ContactsContract.PhoneLookup._ID,
	        			ContactsContract.PhoneLookup.NUMBER,
	        			ContactsContract.PhoneLookup.PHOTO_URI};
	        	
	        	String selection = ContactsContract.PhoneLookup.DISPLAY_NAME;
	        	Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, selection, null, null);
	        	
	        	while(cursor.moveToNext()) {
	        		
	        		System.out.println(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
	        		//System.out.println(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)));
	        		//System.out.println(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)));

	        	}
	        } 
	    });
	    
	    
	    oneBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"1");
			}		
	    });
	    
	    oneBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				
				return false;
			}
			
			
		});
	    
	    twoBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"2");				
			}		
	    	
	    });
	    
	    twoBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				
				return false;
			}
		});
	    
	    threeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"3");
			}		
	    	
	    });
	    
	    threeBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    fourBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"4");				
			}		
	    	
	    });
	    
	    fourBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    fiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"5");				
			}		
	    	
	    });
	    
	    fiveBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    sixBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"6");				
			}		
	    	
	    });
	    
	    sixBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    sevenBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"7");
				
			}		
	    	
	    });
	    
	    sevenBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    eightBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"8");				
			}		
	    	
	    });
	    
	    eightBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    nineBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"9");				
			}		
	    	
	    });
	    
	    nineBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    starBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"*");				
			}		
	    	
	    });
	    
	    starBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    zeroBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"0");				
			}		
	    	
	    });
	    
	    zeroBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    hashBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText(numberEditText.getText().toString()+"#");				
			}		
	    	
	    });
	    
	    hashBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    callBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}		
	    });
	    
	    callBtn.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			    Vibrator vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) ;
				vibe.vibrate(50);
				return false;
			}
		});
	    
	    
	    clearBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				numberEditText.setText((Spanned)numberEditText.getText().delete(numberEditText.length() - 1, numberEditText.length()));
			}		
	    });
	}
	

	private void setupGlobalPrefs() {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        theme = prefs.getString("theme", "#34AADC");
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
	}

	private void setupActionBar() {

		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
       
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
        
		final String[] nav = { "Favourites",
				"Most Contacted",
				"Phone Contacts",
				"Google Contacts",
				"Facebook",
				"Settings",
				"About"
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_settings,
				R.drawable.ic_nav_about
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialer, menu);
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(DialerActivity.this, FavActivity.class);
		   	DialerActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent freqIntent = new Intent(DialerActivity.this, FrequentActivity.class);
		   DialerActivity.this.startActivity(freqIntent);
	   } else if (selected == 2) {
	   		Intent phoneIntent = new Intent(DialerActivity.this, MainActivity.class);
	   		DialerActivity.this.startActivity(phoneIntent);
	   } else if (selected == 3) {
	   		Intent googleIntent = new Intent(DialerActivity.this, GoogleActivity.class);
	   		DialerActivity.this.startActivity(googleIntent);
	   } else if (selected == 4) {
	   		Intent FBIntent = new Intent(DialerActivity.this, FBActivity.class);
	   		DialerActivity.this.startActivity(FBIntent);
	   } else if (selected == 5) {
		   	Intent loIntent = new Intent(DialerActivity.this, LoginActivity.class);
		   	DialerActivity.this.startActivity(loIntent);
	   } else if (selected == 6) {
		   	Intent iIntent = new Intent(DialerActivity.this, InfoActivity.class);
		   	DialerActivity.this.startActivity(iIntent);
	   } 
	}

}
