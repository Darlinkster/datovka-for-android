package cz.nic.datovka.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

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
		int folder = getArguments().getInt(FOLDER, 0);
		
		String[] from;
		if (folder == INBOX) {
			from = new String[] { DatabaseHelper.RECV_ATTACHMENTS_FILENAME, DatabaseHelper.RECV_ATTACHMENTS_PATH,
					DatabaseHelper.RECV_ATTACHMENTS_MIME };
		} else {
			from = new String[] { DatabaseHelper.SENT_ATTACHMENTS_FILENAME, DatabaseHelper.SENT_ATTACHMENTS_PATH,
					DatabaseHelper.SENT_ATTACHMENTS_MIME };
		}
		int[] to = { R.id.attachment_item_filename, R.id.attachment_item_path, R.id.attachment_item_mime };
		
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.attachment_item,null, from, to, 0);
		setListAdapter(adapter);
		setEmptyText(getText(R.string.empty_attachments_list));
		
		
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
