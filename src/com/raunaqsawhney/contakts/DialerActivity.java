package com.raunaqsawhney.contakts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class DialerActivity extends Activity implements OnItemClickListener {
	
	// Declare Globals
	String theme;
	String fontContent;
	String fontTitle;
	
	Vibrator vibe;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	String name;
	String type;
	
	Cursor cursor;
	
	LinearLayout dialerLayout;
	
	TextView number;
	
	Integer rateIt = 0;
	private String font;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialer);
		
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        
        
        initalizeDialer();
	}
	
	private void initalizeDialer() {
		
		final TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
		
		final Button oneBtn = (Button) findViewById(R.id.one);
		oneBtn.setTextColor(Color.parseColor(theme));
		
		final Button twoBtn = (Button) findViewById(R.id.two);
		twoBtn.setTextColor(Color.parseColor(theme));
	    SpannableString twoText = new SpannableString("2  ABC"); 
	    twoText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    twoBtn.setText(twoText);

		final Button threeBtn = (Button) findViewById(R.id.three);
		threeBtn.setTextColor(Color.parseColor(theme));
	    SpannableString threeText = new SpannableString("3  DEF"); 
	    threeText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    threeBtn.setText(threeText);

		final Button fourBtn = (Button) findViewById(R.id.four);
		fourBtn.setTextColor(Color.parseColor(theme));
	    SpannableString fourText = new SpannableString("4  GHI"); 
	    fourText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    fourBtn.setText(fourText);

		final Button fiveBtn = (Button) findViewById(R.id.five);
		fiveBtn.setTextColor(Color.parseColor(theme));
	    SpannableString fiveText = new SpannableString("5  JKL"); 
	    fiveText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    fiveBtn.setText(fiveText);

		final Button sixBtn = (Button) findViewById(R.id.six);
		sixBtn.setTextColor(Color.parseColor(theme));
	    SpannableString sixText = new SpannableString("6  MNO"); 
	    sixText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    sixBtn.setText(sixText);

		final Button sevenBtn = (Button) findViewById(R.id.seven);
		sevenBtn.setTextColor(Color.parseColor(theme));
	    SpannableString sevenText = new SpannableString("7  PQRS"); 
	    sevenText.setSpan(new RelativeSizeSpan(0.4f), 3, 7, 0);  
	    sevenBtn.setText(sevenText);

		final Button eightBtn = (Button) findViewById(R.id.eight);
		eightBtn.setTextColor(Color.parseColor(theme));
	    SpannableString eightText = new SpannableString("8  TUV"); 
	    eightText.setSpan(new RelativeSizeSpan(0.4f), 3, 6, 0);  
	    eightBtn.setText(eightText);

		final Button nineBtn = (Button) findViewById(R.id.nine);
		nineBtn.setTextColor(Color.parseColor(theme));
	    SpannableString nineText = new SpannableString("9  WXYZ"); 
	    nineText.setSpan(new RelativeSizeSpan(0.4f), 3, 7, 0);  
	    nineBtn.setText(nineText);

		final Button starBtn = (Button) findViewById(R.id.star);
		starBtn.setTextColor(Color.parseColor(theme));

		final Button zeroBtn = (Button) findViewById(R.id.zero);
		zeroBtn.setTextColor(Color.parseColor(theme));
	    SpannableString zeroText = new SpannableString("0  +"); 
	    zeroText.setSpan(new RelativeSizeSpan(0.4f), 3, 4, 0);  
	    zeroBtn.setText(zeroText);

		final Button hashBtn = (Button) findViewById(R.id.hash);
		hashBtn.setTextColor(Color.parseColor(theme));

		final Button callBtn = (Button) findViewById(R.id.call);
		
		final Button clearBtn = (Button) findViewById(R.id.clear);
		
		final String[] copypaste = new String[2];
		copypaste[0] = getString(R.string.copy);
		copypaste[1] = getString(R.string.paste);
		
	    number = (TextView) findViewById(R.id.number);
	    number.setTextColor(Color.parseColor(theme));
	    number.setBackgroundColor(0);
	
	    number.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				new AlertDialog.Builder(DialerActivity.this)
					.setItems(copypaste, new DialogInterface.OnClickListener() {

		            @SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int item) {
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

		            	switch (item) {
		            	case 0:
		            	    clipboard.setText(number.getText());
		            	    break;
		            	case 1:
		            	    number.setText(clipboard.getText());
		            	    break;
		            	}
		            }
		        })
		       	.show();
		        return false;
			}
		});
	    
	    number.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
	    number.addTextChangedListener(new TextWatcher() {

	        private String contact_id;

			@Override
	        public void afterTextChanged(Editable s) {
				
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	            // TODO Auto-generated method stub

	        }

	        @SuppressWarnings("deprecation")
			@Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	
	        	try {

		        	Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number.getText().toString()));
		        	String[] projection = new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME,
		        			ContactsContract.PhoneLookup._ID,
		        			ContactsContract.PhoneLookup.NUMBER,
		        			ContactsContract.PhoneLookup.TYPE,
		        			ContactsContract.PhoneLookup.PHOTO_URI};
		        	
		        	String selection = ContactsContract.PhoneLookup.DISPLAY_NAME;
		        	cursor = getContentResolver().query(uri, projection, selection, null, null);
		        	
		        	if(cursor.moveToFirst()) {
		        		
		        		contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
		        		name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
		        		String phoneTypeRaw = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.TYPE));
		        		
		        		try {
		                	switch(Integer.parseInt(phoneTypeRaw))
		                    {
		        	        	case 1:
		        	        		type = getString(R.string.home);
		        	        		break;
		        	        	case 2:
		        	        		type = getString(R.string.mobile);
		        	        		break;
		        	        	case 3:
		        	        		type = getString(R.string.work);
		        	        		break;
		        	        	case 4:
		        	        		type = getString(R.string.fax_work);
		        	        		break;
		        	        	case 5:
		        	        		type = getString(R.string.fax_home);
		        	        		break;
		        	        	case 6:
		        	        		type = getString(R.string.pager);
		        	        		break;
		        	        	case 7:
		        	        		type = getString(R.string.other);
		        	        		break;
		        	        	case 8:
		        	        		type = getString(R.string.callback);
		        	        		break;
		        	        	case 9:
		        	        		type = getString(R.string.car);
		        	        		break;
		        	        	case 10:
		        	        		type = getString(R.string.company_main);
		        	        		break;
		        	        	case 11:
		        	        		type = "ISDN";
		        	        		break;
		        	        	case 12:
		        	        		type = getString(R.string.main);
		        	        		break;
		        	        	case 13:
		        	        		type = getString(R.string.other);
		        	        		break;
		        	        	case 14:
		        	        		type = getString(R.string.radio);
		        	        		break;
		        	        	case 15:
		        	        		type = getString(R.string.telex);
		        	        		break;
		        	        	case 16:
		        	        		type = "TTY - TDD";
		        	        		break;
		        	        	case 17:
		        	        		type = getString(R.string.work_mobile);
		        	        		break;
		        	        	case 18:
		        	        		type = getString(R.string.work_pager);
		        	        		break;
		        	        	case 19:
		        	        		type = getString(R.string.assistant);
		        	        		break;
		        	        	case 20:
		        	        		type = getString(R.string.mms);
		        	        		break;
		                    }
		                } catch (NumberFormatException e) {
		                	type = getString(R.string.other);
		                }
		        		
		        		dialerLayout = (LinearLayout) findViewById(R.id.dialerLayout);
		        		
		        		InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
		                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contact_id)));
		                
		                try {
		                	if (inputStream != null) {
		                    	
		                		Bitmap bitmap = BlurImageLegacy(BitmapFactory.decodeStream(inputStream), 50);
		                		BitmapDrawable background = new BitmapDrawable(bitmap);
		                		dialerLayout.setBackground(background);
		                		
		                		oneBtn.setTextColor(Color.WHITE);
		                		twoBtn.setTextColor(Color.WHITE);
		                		threeBtn.setTextColor(Color.WHITE);
		                		fourBtn.setTextColor(Color.WHITE);
		                		fiveBtn.setTextColor(Color.WHITE);
		                		sixBtn.setTextColor(Color.WHITE);
		                		sevenBtn.setTextColor(Color.WHITE);
		                		eightBtn.setTextColor(Color.WHITE);
		                		nineBtn.setTextColor(Color.WHITE);
		                		starBtn.setTextColor(Color.WHITE);
		                		zeroBtn.setTextColor(Color.WHITE);
		                		hashBtn.setTextColor(Color.WHITE);
		                		number.setTextColor(Color.WHITE);
		                		contactInfo.setTextColor(Color.WHITE);
		                			                		
		                		Animation mAnim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
				        		dialerLayout.startAnimation(mAnim);	
		        	
		                    }
		                } catch (OutOfMemoryError e) {
		                	e.printStackTrace();
		                }     
		                
		        		contactInfo.setText(Html.fromHtml(name + " " + "<b>" + type + "</b>"));
		        		Animation mAnim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
		        		contactInfo.startAnimation(mAnim);
		        	}
	        	} catch (IllegalArgumentException e) {
	        		e.printStackTrace();
	        	} catch (NullPointerException f) {
	        		f.printStackTrace();
	        	}
	        } 
	    });
	    	    
	    oneBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"1"));
				vibe.vibrate(20);	
				}		
	    });
	    
	    oneBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(1)) {
						String number = prefs.getString("oneBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 1);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    twoBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"2"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    twoBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(2)) {
						String number = prefs.getString("twoBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 2);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    threeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"3"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    threeBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(3)) {
						String number = prefs.getString("threeBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 3);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    fourBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"4"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    fourBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(4)) {
						String number = prefs.getString("fourBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 4);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    fiveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"5"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    fiveBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(5)) {
						String number = prefs.getString("fiveBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 5);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    sixBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"6"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    sixBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(6)) {
						String number = prefs.getString("sixBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 6);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    sevenBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"7"));
				vibe.vibrate(20);	
			}		
	    });
	    
	    sevenBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(7)) {
						String number = prefs.getString("sevenBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 7);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});

	    eightBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"8"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    eightBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(8)) {
						String number = prefs.getString("eightBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 8);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    nineBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"9"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    nineBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if (number.length() < 1) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DialerActivity.this);
				
					if (checkIfInSpeedDial(9)) {
						String number = prefs.getString("nineBtn", null);
						
						Intent callIntent = new Intent(Intent.ACTION_CALL);          
        	            callIntent.setData(Uri.parse("tel:" + number));          
        	            startActivity(callIntent);
        	            
					} else {
						
						new AlertDialog.Builder(DialerActivity.this)
					    .setTitle(getString(R.string.setSpeedDial))
					    .setMessage(getString(R.string.setSpeedDialText))
					    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int id) {
			                	
			                	Intent chooseContactIntent = new Intent(DialerActivity.this, ChooseContactActivity.class);
			        	   		chooseContactIntent.putExtra("dialPadNumber", 9);
			                	
			                	DialerActivity.this.startActivity(chooseContactIntent);

			                    dialog.cancel();
			                }
			            })
					    .setNegativeButton(getString(R.string.cancel), null)
					    .show();
					}
				}
				return true;
			}
		});
	    
	    starBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"*"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    zeroBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"0"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    zeroBtn.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"+"));
				vibe.vibrate(20);	
	            return true;
			}
	    });

	    hashBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				number.setText(PhoneNumberUtils.formatNumber(number.getText().toString()+"#"));
				vibe.vibrate(20);	
			}		
	    	
	    });
	    
	    callBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibe.vibrate(20);	

	        	number.setInputType(InputType.TYPE_CLASS_PHONE);

				//vibe.vibrate(50);
				
		    	Intent callIntent = new Intent(Intent.ACTION_CALL);          
	            callIntent.setData(Uri.parse("tel:"+number.getText()));          
	            startActivity(callIntent); 
	            
	            
			}		
	    });
	    
	    clearBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibe.vibrate(20);	

        		oneBtn.setTextColor(Color.parseColor(theme));
        		twoBtn.setTextColor(Color.parseColor(theme));
        		threeBtn.setTextColor(Color.parseColor(theme));
        		fourBtn.setTextColor(Color.parseColor(theme));
        		fiveBtn.setTextColor(Color.parseColor(theme));
        		sixBtn.setTextColor(Color.parseColor(theme));
        		sevenBtn.setTextColor(Color.parseColor(theme));
        		eightBtn.setTextColor(Color.parseColor(theme));
        		nineBtn.setTextColor(Color.parseColor(theme));
        		starBtn.setTextColor(Color.parseColor(theme));
        		zeroBtn.setTextColor(Color.parseColor(theme));
        		hashBtn.setTextColor(Color.parseColor(theme));
        		number.setTextColor(Color.parseColor(theme));
        		contactInfo.setTextColor(Color.BLACK);
        		

				try {
					String contents = number.getText().toString();
					number.setText(contents.substring(0, contents.length()-1));
					contactInfo.setText("");
					dialerLayout.setBackgroundColor(Color.WHITE);

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (StringIndexOutOfBoundsException d) {
					d.printStackTrace();
				} catch (NullPointerException f) {
					f.printStackTrace();
				}
			}		
	    });
	    
	    clearBtn.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				vibe.vibrate(20);	

				contactInfo.setText("");				

        		oneBtn.setTextColor(Color.parseColor(theme));
        		twoBtn.setTextColor(Color.parseColor(theme));
        		threeBtn.setTextColor(Color.parseColor(theme));
        		fourBtn.setTextColor(Color.parseColor(theme));
        		fiveBtn.setTextColor(Color.parseColor(theme));
        		sixBtn.setTextColor(Color.parseColor(theme));
        		sevenBtn.setTextColor(Color.parseColor(theme));
        		eightBtn.setTextColor(Color.parseColor(theme));
        		nineBtn.setTextColor(Color.parseColor(theme));
        		starBtn.setTextColor(Color.parseColor(theme));
        		zeroBtn.setTextColor(Color.parseColor(theme));
        		hashBtn.setTextColor(Color.parseColor(theme));
        		number.setTextColor(Color.parseColor(theme));
        		contactInfo.setTextColor(Color.BLACK);

				try {
					number.setText("");
					dialerLayout.setBackgroundColor(Color.WHITE);
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (StringIndexOutOfBoundsException d) {
					d.printStackTrace();
				} catch (NullPointerException f) {
					f.printStackTrace();
				}
				return true;
			}
	    });
	}
	
	private void setupGlobalPrefs() {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();

        theme = prefs.getString("theme", "#0099CC");
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        font = prefs.getString("font", null);

        
        rateIt = prefs.getInt("rateIt", 0);
    	Integer doneRate = prefs.getInt("doneRate", 0);

        if (rateIt != 10 ) {
        	if (doneRate == 0) {
        		rateIt += 1;
            	edit.putInt("rateIt", rateIt);
            	edit.apply();
        	}
        } else {
        	if (doneRate != 1) {
        		new AlertDialog.Builder(this)
            	.setCancelable(true)
    		    .setTitle(getString(R.string.rateItHeader))
    		    .setMessage(getString(R.string.rateItText))
    		    .setPositiveButton(getString(R.string.playstore), new DialogInterface.OnClickListener() {
    		    	public void onClick(DialogInterface dialog, int id) {
    		    		final String appPackageName = getPackageName();
    	        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        dialog.cancel();
    		    	}
    		    })
    		    .setNegativeButton(getString(R.string.cancel), null)
    		    .show();
            	
            	edit.putInt("doneRate", 1);
            	edit.apply();	
        	}
        }
        
        Boolean firstRunDoneDialer = prefs.getBoolean("firstRunDoneDialer", false);
        if (!firstRunDoneDialer) {
        	edit.putBoolean("firstRunDoneDialer", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.dialDialogHeader))
		    .setMessage(getString(R.string.dialDialogText))
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
        actionBarTitleText.setText(getString(R.string.dialer).toUpperCase());
        
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	        	try {
		    		Intent addIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
		    		addIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number.getText().toString());
		    		startActivity(addIntent);
		    		return true;
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(this, getString(R.string.addNotFound), Toast.LENGTH_LONG).show();
	        	}
	        	return true;
	        case R.id.menu_edit_speed:
	        	Intent editSpeedIntent = new Intent(DialerActivity.this, EditSpeedActivity.class);
			   	DialerActivity.this.startActivity(editSpeedIntent);
			   	return true;
	        case R.id.menu_add_shortcut:
	        
	        	Intent shortcutIntent = new Intent(getApplicationContext(),
    		            DialerActivity.class);
    			
    		    shortcutIntent.setAction(Intent.ACTION_MAIN);
    	         
    	        Intent intent = new Intent();
    	        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    	        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.dialerShortcut));
        		
    	        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(),
    	                R.drawable.dialer_launcher));
        		
    	        
    	        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
    	        getApplicationContext().sendBroadcast(intent);
    	        
        		Toast.makeText(this, getString(R.string.already_shortcut), Toast.LENGTH_LONG).show();

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
		   	Intent dialIntent = new Intent(DialerActivity.this, DialerActivity.class);
		   	DialerActivity.this.startActivity(dialIntent);
	   } else if (selected == 1) {
		   Intent favIntent = new Intent(DialerActivity.this, FavActivity.class);
		   DialerActivity.this.startActivity(favIntent);
	   } else if (selected == 2) {
		   Intent recIntent = new Intent(DialerActivity.this, RecentActivity.class);
		   DialerActivity.this.startActivity(recIntent);
	   } else if (selected == 3) {
		   Intent freqIntent = new Intent(DialerActivity.this, GraphActivity.class);
		   DialerActivity.this.startActivity(freqIntent);
	   } else if (selected == 4) {
		   Intent phoneIntent = new Intent(DialerActivity.this, MainActivity.class);
		   DialerActivity.this.startActivity(phoneIntent);
	   }  else if (selected == 5) {
		   Intent fbIntent = new Intent(DialerActivity.this, GroupActivity.class);
		   DialerActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
			Intent loIntent = new Intent(DialerActivity.this, ShuffleActivity.class);
			DialerActivity.this.startActivity(loIntent);
	   }   else if (selected == 7) {
		   Intent iIntent = new Intent(DialerActivity.this, FBActivity.class);
		   DialerActivity.this.startActivity(iIntent);
	   } else if (selected == 8) {
		   Intent iIntent = new Intent(DialerActivity.this, LoginActivity.class);
		   	DialerActivity.this.startActivity(iIntent);
	   }
	}
	
	private boolean checkIfInSpeedDial(Integer i) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Boolean isInSpeedDial = false;
		String dialNumber = i.toString(); 
		
		isInSpeedDial = prefs.getBoolean(dialNumber, false);
		
		return isInSpeedDial;
		
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
	  public void onResume() {
	      super.onResume();  // Always call the superclass method first
	      setupActionBar();
	  }
	  
	  @Override
	  public void onStart() {
	    super.onStart();
	    cursor = null;
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
		   if (cursor != null) {
		      cursor.close();
		   }
		}

}
