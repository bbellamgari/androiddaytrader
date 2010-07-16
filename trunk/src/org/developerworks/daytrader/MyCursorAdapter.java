package org.developerworks.daytrader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter{
	
		private final LayoutInflater mInflater;
	
		public MyCursorAdapter(Context context, Cursor c) {
			super(context, c, true);
		      mInflater = LayoutInflater.from(context);
		}
	 
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tag = (TextView)view.findViewById(R.id.text1);
			TextView name = (TextView)view.findViewById(R.id.text2);
			TextView price = (TextView)view.findViewById(R.id.text3);
			tag.setText(cursor.getString(
					cursor.getColumnIndex(MyDbAdapter.KEY_TAG)));
			name.setText(cursor.getString(
					cursor.getColumnIndex(MyDbAdapter.KEY_NAME)));
			price.setText(cursor.getString(
					cursor.getColumnIndex(MyDbAdapter.KEY_PRICE)));
		}
	 
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.stockline, parent, false);
		      return view;
		}

		

	
}
