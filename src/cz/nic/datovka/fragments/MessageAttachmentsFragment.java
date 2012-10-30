package cz.nic.datovka.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.AttachmentsContentProvider;

public class MessageAttachmentsFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	
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
		
		String[] from = { DatabaseHelper.ATTACHMENTS_FILENAME, DatabaseHelper.ATTACHMENTS_PATH, DatabaseHelper.ATTACHMENTS_MIME };
		int[] to = { R.id.attachment_item_filename, R.id.attachment_item_path, R.id.attachment_item_mime };
		
		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.attachment_item,null, from, to, 0);
		setListAdapter(adapter);
		
		
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		int folder = getArguments().getInt(FOLDER, 0);
		long messageId = getArguments().getLong(ID, 0);
		
		String[] projection = DatabaseHelper.attachments_columns;
		Uri uri = AttachmentsContentProvider.CONTENT_URI;
		String selection = DatabaseHelper.ATTACHMENTS_MSG_ID + "=? and " + DatabaseHelper.ATTACHMENTS_MSG_FOLDER_ID + "=?";
		String selectionArgs[] = new String[]{Long.toString(messageId), Integer.toString(folder)};

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
