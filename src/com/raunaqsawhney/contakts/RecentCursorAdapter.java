package com.raunaqsawhney.contakts;

import java.util.Date;
import java.util.Random;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        ImageView typePhoto = (ImageView)view.findViewById(R.id.r_type_photo);
        
        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        String number = PhoneNumberUtils.formatNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))).toString();
        String rawDate = (cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
        Date callDayTime = new Date(Long.valueOf(rawDate));
        String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

    	Integer minutes = (Integer.parseInt(duration) % 3600) / 60;
    	Integer seconds = Integer.parseInt(duration) % 60;
    	recentTime.setText(String.format("%d:%02d", minutes, seconds));
        String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
        if (Integer.parseInt(type) == 3) {
        	typePhoto.setImageResource(R.drawable.ic_missed);
        } else if (Integer.parseInt(type) == 2) {
        	typePhoto.setImageResource(R.drawable.ic_outgoing);
        } else if (Integer.parseInt(type) == 1) {
        	typePhoto.setImageResource(R.drawable.ic_incoming);
        }

        try {
        	if (!name.isEmpty())
        		recentName.setText(name);
        	else 
        		recentName.setText(mContext.getResources().getString(R.string.unknown));

            recentNumber.setText(number);
        } catch (NullPointerException e) {
    		recentName.setText(mContext.getResources().getString(R.string.unknown));
        	e.printStackTrace();
        }

        recentDate.setText(callDayTime.toLocaleString());
    }
    
    /*
    private String getPhoto(String contact_id, Context context) {
    	String photo = null;
    	Cursor photoCur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,
                ContactsContract.Contacts._ID +" = ?",
                new String[]{contact_id}, null);
		
		while (photoCur.moveToNext()) {
	        photo = photoCur.getString(photoCur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
        	
		}
		photoCur.close();
		return photo;
	}

	public static String getContactIDFromNumber(String contactNumber,Context context)
    {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(contactNumber)),new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, null, null, null);
            while(contactLookupCursor.moveToNext()){
                phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
            }
            contactLookupCursor.close();

        return String.valueOf(phoneContactID).toString();
    }
    */
}
