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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockListFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.AccountInfoActivity;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;

public class AccountListFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;

	public static AccountListFragment newInstance() {
		AccountListFragment f = new AccountListFragment();
		return f;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		updateList();
		this.setEmptyText(getString(R.string.empty_account_list));
	}

	public void updateList() {

		String[] from = { DatabaseHelper.OWNER_NAME, DatabaseHelper.USER_NAME };
		int[] to = { R.id.account_item_owner, R.id.account_item_user};

		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.account_fragment, null, from, to, 0);

		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				// If the owner_firm_name is empty set that textview to
				// owner_name
				if (tv.getId() == R.id.account_item_owner) {
					// Set Owner Name
					tv.setText(cursor.getString(colIndex));
					// Append ISDS ID
					int isdsIDindex = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID);
					tv.append(" (ID: " + cursor.getString(isdsIDindex) + ")");
					// Set parent view tag to database id
					int msgBoxId = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
					((View) tv.getParent()).setTag(Long.toString(cursor.getLong(msgBoxId)));
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.OWNER_NAME,
				DatabaseHelper.USER_NAME, DatabaseHelper.MSGBOX_ISDS_ID };
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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor cursor = (Cursor) adapter.getItem(info.position);
		int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
		Long msgBoxId = cursor.getLong(idColumnIndex);
		
		switch (item.getItemId()) {
		case R.id.account_delete:
			DeleteAccountWarningFragment dawf = DeleteAccountWarningFragment.newInstance(msgBoxId);
			dawf.show(getFragmentManager(), DeleteAccountWarningFragment.DIALOG_ID);
			return true;
		case R.id.account_info:
			Intent intent = new Intent(getActivity(), AccountInfoActivity.class);
			intent.putExtra(AccountInfoActivity.MSGBOX_ID, msgBoxId);
			startActivity(intent);
			
			return true;
		}

		return true;
	}
	
	
}
