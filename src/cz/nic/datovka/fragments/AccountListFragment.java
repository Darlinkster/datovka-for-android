package cz.nic.datovka.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.TextView;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class AccountListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

	public static AccountListFragment newInstance() {
		AccountListFragment f = new AccountListFragment();
		return f;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		registerForContextMenu(getListView());
	}

	public void updateList() {
		Context context = getActivity();
		
		String[] from = { DatabaseHelper.OWNER_FIRM_NAME, DatabaseHelper.USER_NAME };
		int[] to = { R.id.account_item_owner, R.id.account_item_user };
		
		getLoaderManager().initLoader(0, null, this);
		
		adapter = new SimpleCursorAdapter(context,
				R.layout.account_fragment, null, from,
				to, 0);
		
		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				// If the owner_firm_name is empty set that textview to owner_name
				if(tv.getId() == R.id.account_item_owner){
					if(cursor.getString(colIndex).length() == 0){
						int index = cursor.getColumnIndex(DatabaseHelper.OWNER_NAME);
						tv.setText(cursor.getString(index));
						return true;
					}
				}
				return false;	
			}
		});

		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[]{ DatabaseHelper.MSGBOX_ID,
				DatabaseHelper.OWNER_NAME, DatabaseHelper.OWNER_FIRM_NAME,
				DatabaseHelper.USER_NAME };
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
