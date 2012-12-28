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

import java.io.File;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.AppUtils;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.AttachmentListCursorAdapter;
import cz.nic.datovka.contentProviders.AttachmentsContentProvider;

public class MessageAttachmentsFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {
	private AttachmentListCursorAdapter adapter;
	public static final String ID = "id";

	public static MessageAttachmentsFragment newInstance(long id) {
		MessageAttachmentsFragment f = new MessageAttachmentsFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);

		f.setArguments(args);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fillList();
		setEmptyText(getText(R.string.empty_attachments_list));
	}

	public void fillList() {
		final long messageId = getArguments().getLong(ID, 0);

		String[] from = new String[] { DatabaseHelper.ATTACHMENTS_FILENAME };
		int[] to = { R.id.attachment_item_filename };

		getLoaderManager().initLoader(0, null, this);
		adapter = new AttachmentListCursorAdapter(getActivity(), R.layout.attachment_item, null, from, to, 0);

		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				if (view.getId() == R.id.attachment_item_filename) {
					int attachmentMIME = cursor.getColumnIndex(DatabaseHelper.ATTACHMENTS_MIME);
					int attachmentPathColId = cursor.getColumnIndex(DatabaseHelper.ATTACHMENTS_PATH);

					String attachmentPath = cursor.getString(attachmentPathColId);
					((View) view.getParent()).setTag(cursor.getString(attachmentMIME));
					((View) view.getParent()).setTag(R.id.attachment_path_tag_id, attachmentPath);
					// If any file from attachments is missing, then remove all
					// attachments from db, and pretend that attachments wasn't
					// downloaded yet.
					File tmp = new File(AppUtils.externalStoragePath + attachmentPath);
					if (!tmp.exists()) {
						Uri attachmentUri = AttachmentsContentProvider.CONTENT_URI;
						String where = DatabaseHelper.ATTACHMENTS_MSG_ID + "=?";
						getActivity().getContentResolver().delete(attachmentUri, where, new String[] { Long.toString(messageId) });
					}
					tmp = null;
				}
				return false;
			}
		});
		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		long messageId = getArguments().getLong(ID, 0);

		String[] projection = DatabaseHelper.attachments_columns;
		Uri uri = AttachmentsContentProvider.CONTENT_URI;
		String selection = DatabaseHelper.ATTACHMENTS_MSG_ID + "=?";
		String selectionArgs[] = { Long.toString(messageId) };

		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);

	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);

	}

}
