package com.raunaqsawhney.contakts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class RecentGraph extends Activity {
	
	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	Hashtable<String, Integer> hashtable = new Hashtable<String, Integer>();
	
	Integer morning = 0;
	Integer afternoon = 0;
	Integer evening = 0;
	Integer night = 0;
	private boolean firstRunDoneRecGraph;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent_graph);
		// Show the Up button in the action bar.
		
		setupGlobalPrefs();
		setupActionBar();
		
		showGraphs();
	}
	
	private void setupGlobalPrefs() {
		   
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		
		theme = prefs.getString("theme", "#0099CC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneRecGraph = prefs.getBoolean("firstRunDoneRecGraph", false);
        if (!firstRunDoneRecGraph) {
        	edit.putBoolean("firstRunDoneRecGraph", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.recent_graph_text))
		    .setMessage(getString(R.string.recent_graph_info_text))
		    .setNeutralButton(getString(R.string.okay), null)
		    .show();
        }
                
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
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
        
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	
	
	private void showGraphs() {
		// TODO Auto-generated method stub
		
		Cursor recent_cursor = null;
		recent_cursor = getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI,
				new String[]{CallLog.Calls.DATE},
				CallLog.Calls.TYPE,
                null,
                null);
		
		while (recent_cursor.moveToNext()) { 
			String callDate = recent_cursor.getString(recent_cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)); 

			long seconds=Long.parseLong(callDate);
			SimpleDateFormat formatter = new SimpleDateFormat("HH");
			String dateString = formatter.format(new Date(seconds));
			Integer dateInt = Integer.parseInt(dateString);
			if (dateInt >= 04 && dateInt < 12) {
				morning += 1;
			} else if (dateInt >= 12 && dateInt < 17) {
				afternoon += 1;
			} else if (dateInt >= 17 && dateInt < 22) {
				evening += 1;
			} else if (dateInt >= 22 || (dateInt >= 00 && dateInt < 04)) {
				night += 1;
			}
		}
		
		TextView recent_graph_info = (TextView) findViewById(R.id.recent_graph_info);
		
		Integer max_day = Math.max(morning, afternoon);
		Integer max_night = Math.max(evening, night);
		
		Integer max_final = Math.max(max_day, max_night);
		if (max_final.equals(morning)) {
			recent_graph_info.setText(getString(R.string.morningMAX));
			recent_graph_info.setTextColor(Color.parseColor("#33B5E5"));
		} else if (max_final.equals(afternoon)) {
			recent_graph_info.setText(getString(R.string.afternoonMAX));
			recent_graph_info.setTextColor(Color.parseColor("#99CC00"));
		}  else if (max_final.equals(evening)) {
			recent_graph_info.setText(getString(R.string.eveningMAX));
			recent_graph_info.setTextColor(Color.parseColor("#FFBB33"));
		} else if (max_final.equals(night)) {
			recent_graph_info.setText(getString(R.string.nightMAX));
			recent_graph_info.setTextColor(Color.parseColor("#FF4444"));
		}
		
		ArrayList<Bar> points = new ArrayList<Bar>();
		Bar d = new Bar();
		d.setColor(Color.parseColor("#33B5E5"));
		d.setName(getString(R.string.morning));
		d.setValue(morning);
		
		
		Bar d2 = new Bar();
		d2.setColor(Color.parseColor("#99CC00"));
		d2.setName(getString(R.string.afternoon));
		d2.setValue(afternoon);
		
		
		Bar d3 = new Bar();
		d3.setColor(Color.parseColor("#FFBB33"));
		d3.setName(getString(R.string.evening));
		d3.setValue(evening);
		
		
		Bar d4 = new Bar();
		d4.setColor(Color.parseColor("#FF4444"));
		d4.setName(getString(R.string.night));
		d4.setValue(night);
		
		points.add(d);
		points.add(d2);
		points.add(d3);
		points.add(d4);

		BarGraph g = (BarGraph)findViewById(R.id.graph);
		g.setBars(points);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recent_graph, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
			
			
		}
		return super.onOptionsItemSelected(item);
	}

}
