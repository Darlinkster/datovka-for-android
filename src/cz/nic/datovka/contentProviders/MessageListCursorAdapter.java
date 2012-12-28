/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2012  CZ NIC z.s.p.o. <podpora at nic dot cz>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cz.nic.datovka.contentProviders;


import cz.nic.datovka.R;
import cz.nic.datovka.activities.AppUtils;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class MessageListCursorAdapter extends SimpleCursorAdapter{
	private String readColName;
	private String statusChangedColName;
	private String statusColName;
	private int folder;
	private static final int READ = 1;
	private static final int CHANGED = 1;

	public MessageListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String readColName, String statusChangedColName, String statusColName, int folder, int flags) {
		super(context, layout, c, from, to, flags);
		this.readColName = readColName;
		this.statusChangedColName = statusChangedColName;
		this.statusColName = statusColName;
		this.folder = folder;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		super.bindView(view, context, cursor);
		
		int statusChanged = cursor.getInt(cursor.getColumnIndex(this.statusChangedColName));
		int isReadBoolean = cursor.getInt(cursor.getColumnIndex(this.readColName));
		int status = cursor.getInt(cursor.getColumnIndex(this.statusColName));
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		View notification = view.findViewById(R.id.message_item_notification_strip);
		View pictogram = view.findViewById(R.id.message_item_pictogram);
		
		if(folder == AppUtils.OUTBOX){
			if(status < 6) {
				pictogram.setBackgroundResource(R.drawable.message_sent);
			}
			else {
				pictogram.setBackgroundResource(R.drawable.content_read);
			}
		}
		
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
