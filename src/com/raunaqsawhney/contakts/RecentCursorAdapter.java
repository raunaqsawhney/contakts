package com.raunaqsawhney.contakts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RecentCursorAdapter extends SimpleCursorAdapter {
	
    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private final LayoutInflater inflater;

	public RecentCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
        this.layout=layout;
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.cr=c;
    }
	
	@Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView recentName = (TextView)view.findViewById(R.id.r_name);
        TextView recentNumber = (TextView)view.findViewById(R.id.r_number);
        TextView recentDate = (TextView)view.findViewById(R.id.r_date);
        TextView recentTime = (TextView)view.findViewById(R.id.r_time);
        
        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        String number = PhoneNumberUtils.formatNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))).toString();
        String rawDate = (cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
        Date callDayTime = new Date(Long.valueOf(rawDate));
        String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

    	Integer minutes = (Integer.parseInt(duration) % 3600) / 60;
    	Integer seconds = Integer.parseInt(duration) % 60;
    	recentTime.setText(String.format("%d:%02d", minutes, seconds));
        String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
        if (Integer.parseInt(type) == 1) {
        	recentTime.setTextColor(Color.parseColor("#CC0000"));
        } else if (Integer.parseInt(type) == 2) {
        	recentTime.setTextColor(Color.parseColor("#669900"));
        } else if (Integer.parseInt(type) == 3) {
        	recentTime.setTextColor(Color.parseColor("#0099CC"));
        }
        
        recentName.setText(name);
        recentNumber.setText(number);
        recentDate.setText(callDayTime.toLocaleString());
    }
}
