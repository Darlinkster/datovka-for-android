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


import java.util.Locale;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.connector.DatabaseHelper;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class AttachmentListCursorAdapter extends SimpleCursorAdapter{
	
	public AttachmentListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		TextView tv = (TextView) view.findViewById(R.id.attachment_item_filename);
		String filePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ATTACHMENTS_PATH));
		String suffix = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase(Locale.getDefault());
		int iconDrawableId = Application.ctx.getResources().getIdentifier("fileicon_"+suffix, "drawable", Application.ctx.getPackageName());
		
		tv.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ATTACHMENTS_FILENAME)));
		if(iconDrawableId != 0){
			tv.setCompoundDrawablesWithIntrinsicBounds(Application.ctx.getResources().getDrawable(iconDrawableId), null, null, null);
		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(Application.ctx.getResources().getDrawable(R.drawable.fileicon_blank), null, null, null);
		}

		tv = null;
		filePath = null;
		suffix = null;
	}
	
	
}
