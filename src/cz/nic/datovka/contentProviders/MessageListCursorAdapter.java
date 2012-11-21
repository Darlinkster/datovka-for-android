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
	private String statusChangedColName;
	private static final int READ = 1;
	private static final int CHANGED = 1;

	public MessageListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String readColName, String statusChangedColName, int flags) {
		super(context, layout, c, from, to, flags);
		this.readColName = readColName;
		this.statusChangedColName = statusChangedColName;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		super.bindView(view, context, cursor);
		
		int statusChanged = cursor.getInt(cursor.getColumnIndex(this.statusChangedColName));
		int isReadBoolean = cursor.getInt(cursor.getColumnIndex(this.readColName));
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		View notification = view.findViewById(R.id.message_item_notification_strip);
		
		if(statusChanged == CHANGED){
			notification.setVisibility(View.VISIBLE);
		}
		else{
			notification.setVisibility(View.INVISIBLE);
		}
		
		if(isReadBoolean == READ){
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
