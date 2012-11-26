package cz.nic.datovka.fragments;

import java.io.File;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.RecvAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentAttachmentsContentProvider;

public class MessageAttachmentsFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	private static final int INBOX = 0;
	
	public static MessageAttachmentsFragment newInstance(long id, int folder) {
		MessageAttachmentsFragment f = new MessageAttachmentsFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);
		args.putInt(FOLDER, folder);
		
		f.setArguments(args);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		final int folder = getArguments().getInt(FOLDER, 0);
		final long messageId = getArguments().getLong(ID, 0);
		
		String[] from;
		if (folder == INBOX) {
			from = new String[] { DatabaseHelper.RECV_ATTACHMENTS_FILENAME, DatabaseHelper.RECV_ATTACHMENTS_PATH };
		} else {
			from = new String[] { DatabaseHelper.SENT_ATTACHMENTS_FILENAME, DatabaseHelper.SENT_ATTACHMENTS_PATH };
		}
		int[] to = { R.id.attachment_item_filename, R.id.attachment_item_path };
		
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.attachment_item,null, from, to, 0);
		setListAdapter(adapter);
		setEmptyText(getText(R.string.empty_attachments_list));

		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				if (view.getId() == R.id.attachment_item_path) {
					int attachmentMIME;
					if (folder == INBOX) {
						attachmentMIME = cursor.getColumnIndex(DatabaseHelper.RECV_ATTACHMENTS_MIME);
					} else {
						attachmentMIME = cursor.getColumnIndex(DatabaseHelper.SENT_ATTACHMENTS_MIME);
					}

					((View) view.getParent()).setTag(cursor.getString(attachmentMIME));
					// If any file from attachments is missing, then remove all
					// attachments from db, and pretend that attachments wasn't
					// downloaded yet. 
					File tmp = new File(cursor.getString(colIndex));
					if (!tmp.exists()) {
						Uri attachmentUri;
						String where;
						if (folder == INBOX) {
							attachmentUri = RecvAttachmentsContentProvider.CONTENT_URI;
							where = DatabaseHelper.RECV_ATTACHMENTS_MSG_ID + "=?";
						}
						else {
							attachmentUri = SentAttachmentsContentProvider.CONTENT_URI;
							where = DatabaseHelper.SENT_ATTACHMENTS_MSG_ID + "=?";
						}
						getActivity().getContentResolver().delete(attachmentUri, where, new String[]{Long.toString(messageId)});
					}
					tmp = null;
				}
				return false;
			}
		});

	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		int folder = getArguments().getInt(FOLDER, 0);
		long messageId = getArguments().getLong(ID, 0);
		
		String[] projection;
		Uri uri;
		String selection;
		if (folder == INBOX) {
			projection = DatabaseHelper.recv_attachments_columns;
			uri = RecvAttachmentsContentProvider.CONTENT_URI;
			selection = DatabaseHelper.RECV_ATTACHMENTS_MSG_ID + "=?";
		}
		else{
			projection = DatabaseHelper.sent_attachments_columns;
			uri = SentAttachmentsContentProvider.CONTENT_URI;
			selection = DatabaseHelper.SENT_ATTACHMENTS_MSG_ID + "=?";
		}
		String selectionArgs[] = {Long.toString(messageId)};

		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
				projection, selection, selectionArgs, null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
		
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
		
	}
	
}
