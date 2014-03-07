package com.raunaqsawhney.contakts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class CustomCursorAdapter extends ResourceCursorAdapter {
	
	private LayoutInflater mLayoutInflater;
    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, R.layout.lv_layout, null, false);    // cursor=null, autoRequery=false
    }
    
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        TextView c_name = (TextView) view.findViewById(R.id.c_name);
        ImageView c_photo = (ImageView) view.findViewById(R.id.c_photo);
        
        c_name.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));

        String photoURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI));
        if (photoURI != null) {
        	c_photo.setImageURI(Uri.parse(photoURI));
        } else {
        	System.out.println("NULL");
        }
		
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		@SuppressWarnings("unused")
		View v = mLayoutInflater.inflate(R.layout.lv_layout, parent, false);
		return null;
	}

}
