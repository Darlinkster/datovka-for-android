package cz.nic.datovka.contentProviders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;

public class ModifiedSimpleCursorAdapter extends SimpleCursorAdapter{
	private String readColName;
	private int readColor;
	private int unReadColor;

	public ModifiedSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String readColName, int readColor, int unReadColor, int flags) {
		super(context, layout, c, from, to, flags);
		this.readColName = readColName;
		this.readColor = readColor;
		this.unReadColor = unReadColor;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		super.bindView(view, context, cursor);
		
		int readColIndex = cursor.getColumnIndex(this.readColName);
		int isReadBoolean = cursor.getInt(readColIndex);
		
		if(isReadBoolean == 1){
			view.setBackgroundColor(this.readColor);
			
		}
		else{
			view.setBackgroundColor(this.unReadColor);
		}
	}
}
