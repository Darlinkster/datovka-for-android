package cz.nic.datovka.contentProviders;


import cz.nic.datovka.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

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
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		
		if(isReadBoolean == 1){
			view.setBackgroundColor(this.readColor);
			annotation.setTypeface(null, Typeface.NORMAL);
			sender.setTypeface(null, Typeface.NORMAL);
		}
		else{
			view.setBackgroundColor(this.unReadColor);
			annotation.setTypeface(null, Typeface.BOLD);
			sender.setTypeface(null, Typeface.BOLD);
		}
	}
}
