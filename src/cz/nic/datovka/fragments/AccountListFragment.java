package cz.nic.datovka.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.RecvAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentAttachmentsContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;

public class AccountListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

	public static AccountListFragment newInstance() {
		AccountListFragment f = new AccountListFragment();
		return f;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		updateList();
	}

	public void updateList() {

		String[] from = { DatabaseHelper.OWNER_FIRM_NAME, DatabaseHelper.USER_NAME };
		int[] to = { R.id.account_item_owner, R.id.account_item_user };

		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.account_fragment, null, from, to, 0);

		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				// If the owner_firm_name is empty set that textview to
				// owner_name
				if (tv.getId() == R.id.account_item_owner) {
					if (cursor.getString(colIndex).length() == 0) {
						int index = cursor.getColumnIndex(DatabaseHelper.OWNER_NAME);
						tv.setText(cursor.getString(index));
					}
					else {
						int index = cursor.getColumnIndex(DatabaseHelper.OWNER_FIRM_NAME);
						tv.setText(cursor.getString(index));
					}
					// Append ISDS ID
					int isdsIDindex = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID);
					String msgBoxIsdsID = cursor.getString(isdsIDindex);
					tv.append(" (ID: " + msgBoxIsdsID + ")");
					return true;
				}
				
				return false;
			}
		});

		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.OWNER_NAME,
				DatabaseHelper.OWNER_FIRM_NAME, DatabaseHelper.USER_NAME, DatabaseHelper.MSGBOX_ISDS_ID };
		CursorLoader cursorLoader = new CursorLoader(getActivity(), MsgBoxContentProvider.CONTENT_URI, projection,
				null, null, null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.account_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.account_delete:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

			Cursor cursor = (Cursor) adapter.getItem(info.position);
			int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
			Long msgBoxId = cursor.getLong(idColumnIndex);

			deleteAccount(msgBoxId);
			
			Toast.makeText(getActivity(), R.string.account_deleted, Toast.LENGTH_SHORT).show();
			return true;
		case R.id.account_info:
			Toast.makeText(getActivity(), "infoinfo", Toast.LENGTH_SHORT).show();
			return true;
		}

		return true;
	}
	
	private void deleteAccount(Long msgBoxId){
		Uri userUri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		getActivity().getContentResolver().delete(userUri, null, null);
		getActivity().getContentResolver().notifyChange(SentMessagesContentProvider.CONTENT_URI, null);
		getActivity().getContentResolver().notifyChange(ReceivedMessagesContentProvider.CONTENT_URI, null);
		getActivity().getContentResolver().notifyChange(RecvAttachmentsContentProvider.CONTENT_URI, null);
		getActivity().getContentResolver().notifyChange(SentAttachmentsContentProvider.CONTENT_URI, null);
		
	}
}
