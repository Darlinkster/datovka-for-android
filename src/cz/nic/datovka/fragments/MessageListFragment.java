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
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class MessageListFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private SimpleCursorAdapter adapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		registerForContextMenu(getListView());
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = DatabaseHelper.received_message_columns;
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				ReceivedMessagesContentProvider.CONTENT_URI, projection, null, null,
				null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}
	
	public void updateList() {
		Context context = getActivity();
		
		String[] from = { DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION,
				DatabaseHelper.SENDER_NAME,
				DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE };
		
		int[] to = { R.id.message_item_annotation, R.id.message_item_sender,
				R.id.message_item_date };
		
		getLoaderManager().initLoader(0, null, this);
		
		adapter = new SimpleCursorAdapter(context,
				R.layout.message_list_fragment, null, from,
				to, 0);
		
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int colIndex) {

				if(view.getId() == R.id.message_item_date){
					TextView textView = (TextView) view;
					String date = cursor.getString(colIndex);
					textView.setText(AndroidUtils.FromXmlToHumanReadableDate(date));
					
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
	}
}
