package com.raunaqsawhney.contakts;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class EditSpeedActivity extends Activity {
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_speed);
		
		setupGlobalPrefs();
		setupActionBar();
		
		populateEditSpeedList();
		
	}
	
	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), fontTitle));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
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
	
	private void populateEditSpeedList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		LinearLayout oneLayout = (LinearLayout) findViewById(R.id.one);
		LinearLayout twoLayout = (LinearLayout) findViewById(R.id.two);
		LinearLayout threeLayout = (LinearLayout) findViewById(R.id.three);
		LinearLayout fourLayout = (LinearLayout) findViewById(R.id.four);
		LinearLayout fiveLayout = (LinearLayout) findViewById(R.id.five);
		LinearLayout sixLayout = (LinearLayout) findViewById(R.id.six);
		LinearLayout sevenLayout = (LinearLayout) findViewById(R.id.seven);
		LinearLayout eightLayout = (LinearLayout) findViewById(R.id.eight);
		LinearLayout nineLayout = (LinearLayout) findViewById(R.id.nine);
		
		final TextView oneNameTV = (TextView) findViewById(R.id.speed1ContactName);
		final TextView twoNameTV = (TextView) findViewById(R.id.speed2ContactName);
		final TextView threeNameTV = (TextView) findViewById(R.id.speed3ContactName);
		final TextView fourNameTV = (TextView) findViewById(R.id.speed4ContactName);
		final TextView fiveNameTV = (TextView) findViewById(R.id.speed5ContactName);
		final TextView sixNameTV = (TextView) findViewById(R.id.speed6ContactName);
		final TextView sevenNameTV = (TextView) findViewById(R.id.speed7ContactName);
		final TextView eightNameTV = (TextView) findViewById(R.id.speed8ContactName);
		final TextView nineNameTV = (TextView) findViewById(R.id.speed9ContactName);

		TextView oneNumberTV = (TextView) findViewById(R.id.speed1ContactNumber);
		TextView twoNumberTV = (TextView) findViewById(R.id.speed2ContactNumber);
		TextView threeNumberTV = (TextView) findViewById(R.id.speed3ContactNumber);
		TextView fourNumberTV = (TextView) findViewById(R.id.speed4ContactNumber);
		TextView fiveNumberTV = (TextView) findViewById(R.id.speed5ContactNumber);
		TextView sixNumberTV = (TextView) findViewById(R.id.speed6ContactNumber);
		TextView sevenNumberTV = (TextView) findViewById(R.id.speed7ContactNumber);
		TextView eightNumberTV = (TextView) findViewById(R.id.speed8ContactNumber);
		TextView nineNumberTV = (TextView) findViewById(R.id.speed9ContactNumber);

		String oneName = prefs.getString("oneName", null);
		String twoName = prefs.getString("twoName", null);
		String threeName = prefs.getString("threeName", null);
		String fourName = prefs.getString("fourName", null);
		String fiveName = prefs.getString("fiveName", null);
		String sixName = prefs.getString("sixName", null);
		String sevenName = prefs.getString("sevenName", null);
		String eightName = prefs.getString("eightName", null);
		String nineName = prefs.getString("nineName", null);
		
		String oneNumber = prefs.getString("oneBtn", null);
		String twoNumber = prefs.getString("twoBtn", null);
		String threeNumber = prefs.getString("threeBtn", null);
		String fourNumber = prefs.getString("fourBtn", null);
		String fiveNumber = prefs.getString("fiveBtn", null);
		String sixNumber = prefs.getString("sixBtn", null);
		String sevenNumber = prefs.getString("sevenBtn", null);
		String eightNumber = prefs.getString("eightBtn", null);
		String nineNumber = prefs.getString("nineBtn", null);
		
		
		try {
			if (oneName.length() < 1)
				oneNameTV.setText(getString(R.string.noContactAssigned));
			else 
				oneNameTV.setText(oneName);
		} catch (NullPointerException e) {
			oneNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (twoName.length() < 1)
				twoNameTV.setText(getString(R.string.noContactAssigned));
			else 
				twoNameTV.setText(twoName);
		} catch (NullPointerException e) {
			twoNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (threeName.length() < 1) 
				threeNameTV.setText(getString(R.string.noContactAssigned));
			else 
				threeNameTV.setText(threeName);
		} catch (NullPointerException e) {
			threeNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (fourName.length() < 1)
				fourNameTV.setText(getString(R.string.noContactAssigned));
			else 
				fourNameTV.setText(fourName);
		} catch (NullPointerException e) {
			fourNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (fiveName.length() < 1)
				fiveNameTV.setText(getString(R.string.noContactAssigned));
			else 
				fiveNameTV.setText(fiveName);
		} catch (NullPointerException e) {
			fiveNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (sixName.length() < 1)
				sixNameTV.setText(getString(R.string.noContactAssigned));
			else 
				sixNameTV.setText(sixName);
		} catch (NullPointerException e) {
			sixNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (sevenName.length() < 1)
				sevenNameTV.setText(getString(R.string.noContactAssigned));
			else 
				sevenNameTV.setText(sevenName);
		} catch (NullPointerException e) {
			sevenNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (eightName.length() < 1)
				eightNameTV.setText(getString(R.string.noContactAssigned));
			else
				eightNameTV.setText(eightName);
		} catch (NullPointerException e) {
			eightNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (nineName.length() < 1) 
				nineNameTV.setText(getString(R.string.noContactAssigned));
			else 
				nineNameTV.setText(nineName);
		} catch (NullPointerException e) {
			nineNameTV.setText(getString(R.string.noContactAssigned));
		}
		
		try {
			if (oneNumber.length() < 1)
				oneNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				oneNumberTV.setText(oneNumber);
		} catch (NullPointerException e) {
			oneNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (twoNumber.length() < 1 )
				twoNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				twoNumberTV.setText(twoNumber);
		} catch (NullPointerException e) {
			twoNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (threeNumber.length() < 1 )
				threeNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				threeNumberTV.setText(threeNumber);
		} catch (NullPointerException e) {
			threeNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (fourNumber.length() < 1)
				fourNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				fourNumberTV.setText(fourNumber);
		} catch (NullPointerException e) {
			fourNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (fiveNumber.length() < 1)
				fiveNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				fiveNumberTV.setText(fiveNumber);
		} catch (NullPointerException e) {
			fiveNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (sixNumber.length() < 1)
				sixNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				sixNumberTV.setText(sixNumber);
		} catch (NullPointerException e) {
			sixNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (sevenNumber.length() < 1)
				sevenNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				sevenNumberTV.setText(sevenNumber);
		} catch (NullPointerException e) {
			sevenNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (eightNumber.length() < 1)
				eightNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				eightNumberTV.setText(eightNumber);
		} catch (NullPointerException e) {
			eightNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		try {
			if (nineNumber.length() < 1)
				nineNumberTV.setText(getString(R.string.enterSpeedContact));
			else 
				nineNumberTV.setText(nineNumber);
		} catch (NullPointerException e) {
			nineNumberTV.setText(getString(R.string.enterSpeedContact));
		}
		
		oneLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (oneNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(1); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(1);
					    	} else if (item == 1) {
					    		removeSpeedDial(1);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		twoLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (twoNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(2); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(2);
					    	} else if (item == 1) {
					    		removeSpeedDial(2);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		threeLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (threeNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(3); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(3);
					    	} else if (item == 1) {
					    		removeSpeedDial(3);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		fourLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (fourNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned)) ) {
		    		changeSpeedDial(4); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(4);
					    	} else if (item == 1) {
					    		removeSpeedDial(4);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		fiveLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				
				if (fiveNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned)) ) {
		    		changeSpeedDial(5); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(5);
					    	} else if (item == 1) {
					    		removeSpeedDial(5);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		sixLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				
				if (sixNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned)) ) {
		    		changeSpeedDial(6); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(6);
					    	} else if (item == 1) {
					    		removeSpeedDial(6);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		sevenLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				
				if (sevenNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(7); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(7);
					    	} else if (item == 1) {
					    		removeSpeedDial(7);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		eightLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (eightNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(8); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(8);
					    	} else if (item == 1) {
					    		removeSpeedDial(8);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
		
		nineLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {getString(R.string.changeSpeedDial), getString(R.string.removeSpeedDial)};

				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				if (nineNameTV.getText().toString().equalsIgnoreCase(getString(R.string.noContactAssigned))) {
		    		changeSpeedDial(9); 
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(EditSpeedActivity.this);
					builder.setTitle(getString(R.string.editSpeedDial));
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
				    		
					    	if (item == 0) {
					    		changeSpeedDial(9);
					    	} else if (item == 1) {
					    		removeSpeedDial(9);
					    	}
					    }
					});
					AlertDialog alert = builder.create();
					alert.show();
				}			
			}
		});
	}
	
	private void removeSpeedDial(int i) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		
		String whichBtn = null;
		String whichName = null;
		
		if (i == 1) {
    		whichBtn = "oneBtn";
    		whichName = "oneName";
    	} else if (i == 2) {
    		whichBtn = "twoBtn";
    		whichName = "twoName";
    	} else if (i == 3) {
    		whichBtn = "threeBtn";
    		whichName = "threeName";
    	} else if (i == 4) {
    		whichBtn = "fourBtn";
    		whichName = "fourName";
    	} else if (i == 5) {
    		whichBtn = "fiveBtn";
    		whichName = "fiveName";
    	} else if (i == 6) {
    		whichBtn = "sixBtn";
    		whichName = "sixName";
    	} else if (i == 7) {
    		whichBtn = "sevenBtn";
    		whichName = "sevenName";
    	} else if (i == 8) {
    		whichBtn = "eightBtn";
    		whichName = "eightName";
    	} else if (i == 9) {
    		whichBtn = "nineBtn";
    		whichName = "nineName";
    	}
		
		edit.remove(whichBtn);
		edit.remove(whichName);
		edit.remove(String.valueOf(i));
		edit.apply();
		
		Intent intent = new Intent(EditSpeedActivity.this, EditSpeedActivity.class);
		intent.putExtra("dialPadNumber", i);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		EditSpeedActivity.this.startActivity(intent);
		
	}

	private void changeSpeedDial(int i) {
		Intent intent = new Intent(EditSpeedActivity.this, ChooseContactActivity.class);
		intent.putExtra("dialPadNumber", i);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		EditSpeedActivity.this.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_speed, menu);
		return true;
	}
}
