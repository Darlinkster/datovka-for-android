package cz.nic.datovka.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;

public class AccountFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

	public static AccountFragment newInstance() {
		AccountFragment f = new AccountFragment();
		return f;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		registerForContextMenu(getListView());
	}

	public void updateList() {
		Context context = getActivity();
		
		String[] from = { DatabaseHelper.OWNER_NAME, DatabaseHelper.USER_NAME };
		int[] to = { R.id.account_item_owner, R.id.account_item_user };
		
		getLoaderManager().initLoader(0, null, this);
		
		adapter = new SimpleCursorAdapter(context,
				R.layout.account_fragment, null, from,
				to, 0);

		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = DatabaseHelper.msgbox_columns;
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				MsgBoxContentProvider.CONTENT_URI, projection, null, null,
				null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}
}
