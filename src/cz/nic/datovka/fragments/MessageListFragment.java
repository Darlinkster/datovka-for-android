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

package cz.nic.datovka.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessageListCursorAdapter;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class MessageListFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>{
	private SimpleCursorAdapter adapter;
	private static String MSGBOXID = "msgboxid";
	private static String MSG_FOLDER = "msgfolder";
	
	public static MessageListFragment getInstance(String arg, int folder){
		MessageListFragment rmlf = new MessageListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MSGBOXID, arg);
		bundle.putInt(MSG_FOLDER, folder);
		
		rmlf.setArguments(bundle);
		return rmlf;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		this.setEmptyText(getString(R.string.empty_inbox_message));
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { DatabaseHelper.MESSAGE_ID,
				DatabaseHelper.MESSAGE_FOLDER,
				DatabaseHelper.MESSAGE_MSGBOX_ID, 
				DatabaseHelper.MESSAGE_ANNOTATION,
				DatabaseHelper.MESSAGE_OTHERSIDE_NAME, 
				DatabaseHelper.MESSAGE_SENT_DATE,
				DatabaseHelper.MESSAGE_IS_READ,
				DatabaseHelper.MESSAGE_STATUS_CHANGED,
				DatabaseHelper.MESSAGE_STATE};

		String selection = DatabaseHelper.MESSAGE_MSGBOX_ID + "=? and " + DatabaseHelper.MESSAGE_FOLDER + "=?";
		String[] selectionArgs = { getArguments().getString(MSGBOXID), Integer.toString(getArguments().getInt(MSG_FOLDER)) };

		CursorLoader cursorLoader = new CursorLoader(getActivity(), MessagesContentProvider.CONTENT_URI,
				projection, selection, selectionArgs, DatabaseHelper.MESSAGE_SENT_DATE + " DESC");

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}
	
	public void updateList() {
		String[] from = { DatabaseHelper.MESSAGE_ANNOTATION,
				DatabaseHelper.MESSAGE_OTHERSIDE_NAME,
				DatabaseHelper.MESSAGE_SENT_DATE };
		
		int[] to = { R.id.message_item_annotation, R.id.message_item_sender,
				R.id.message_item_date };
		
		getLoaderManager().initLoader(0, null, this);
		
		adapter = new MessageListCursorAdapter(getActivity(),
				R.layout.message_list_fragment, null, from, to,
				DatabaseHelper.MESSAGE_IS_READ,
				DatabaseHelper.MESSAGE_STATUS_CHANGED,
				DatabaseHelper.MESSAGE_STATE,
				getArguments().getInt(MSG_FOLDER), 0);
		
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				int messageIdIndex = cursor.getColumnIndex(DatabaseHelper.MESSAGE_ID);
				TextView textView = (TextView) view;
				// covert date to human readable format
				if(view.getId() == R.id.message_item_date){
					String date = cursor.getString(colIndex);
					textView.setText(AndroidUtils.FromXmlToHumanReadableDate(date));
					
					return true;
				}
				
				// Add database id to the parent tag
				((View) view.getParent()).setTag(cursor.getString(messageIdIndex));
				return false;
			}
		});

		setListAdapter(adapter);
	}
}

