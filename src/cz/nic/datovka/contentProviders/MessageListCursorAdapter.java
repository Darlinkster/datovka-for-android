package cz.nic.datovka.contentProviders;


import cz.nic.datovka.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class MessageListCursorAdapter extends SimpleCursorAdapter{
	private String readColName;

	public MessageListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String readColName, int flags) {
		super(context, layout, c, from, to, flags);
		this.readColName = readColName;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		super.bindView(view, context, cursor);
		
		int readColIndex = cursor.getColumnIndex(this.readColName);
		int isReadBoolean = cursor.getInt(readColIndex);
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		
		if(isReadBoolean == 1){
			view.setBackgroundResource(R.drawable.message_item_read_background);
			annotation.setTypeface(null, Typeface.NORMAL);
			sender.setTypeface(null, Typeface.NORMAL);
		}
		else{
			view.setBackgroundResource(R.drawable.message_item_unread_background);
			annotation.setTypeface(null, Typeface.BOLD);
			sender.setTypeface(null, Typeface.BOLD);
		}
	}
}
