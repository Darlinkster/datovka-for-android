package cz.nic.datovka.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.AttachmentsContentProvider;

public class MessageAttachmentsFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	private static final int INBOX = 0;
	private static final int OUTBOX = 1;
	private static final int IS_READ = 1;
	
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
		
		String[] from = { DatabaseHelper.ATTACHMENTS_MSG_ID, DatabaseHelper.ATTACHMENTS_PATH };
		int[] to = { R.id.attachment_item_filename, R.id.attachment_path };
		
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.attachment_item,null, from, to, 0);
		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		int folder = getArguments().getInt(FOLDER, 0);
		long id = getArguments().getLong(ID, 0);
		
		String[] projection = DatabaseHelper.attachments_columns;
		Uri uri = AttachmentsContentProvider.CONTENT_URI;
		

		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
				projection, null, null, null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
		
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
		
	}
}
