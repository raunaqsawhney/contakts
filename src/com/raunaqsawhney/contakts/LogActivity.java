package com.raunaqsawhney.contakts;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.Menu;

public class LogActivity extends Activity {
	
	ArrayList<callHistory> callLog = new ArrayList<callHistory>();
	callHistory callHistory = new callHistory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		getCallDetails();
	}

	private void getCallDetails() {
		
		int count = 0;
		@SuppressWarnings("deprecation")
		Cursor logCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
		
		while (logCursor.moveToNext())
		{
			String name = logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
			String number = logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.NUMBER));
			String type = logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.TYPE));
			String date = logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.DATE));
			String duration = logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.DURATION));
			
			callHistory.setName(name);
			callHistory.setNumber(number);
			callHistory.setType(type);
			callHistory.setDate(date);
			callHistory.setDuration(duration);
			
			callLog.add(callHistory);
			count++;
		}
		
		System.out.println("COUNT: " + count);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}

}
