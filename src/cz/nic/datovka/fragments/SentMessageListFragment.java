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
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class SentMessageListFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private SimpleCursorAdapter adapter;
	private static String MSGBOXID = "msgboxid";
	
	public static SentMessageListFragment getInstance(String arg){
		SentMessageListFragment smlf = new SentMessageListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MSGBOXID, arg);
		smlf.setArguments(bundle);
		return smlf;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		registerForContextMenu(getListView());
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String selectionArgs = DatabaseHelper.SENT_MESSAGE_MSGBOX_ID + " = " + getArguments().getString(MSGBOXID);
		String[] projection = DatabaseHelper.sent_message_columns;
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				SentMessagesContentProvider.CONTENT_URI, projection, selectionArgs, null,
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
		
		String[] from = { DatabaseHelper.SENT_MESSAGE_ANNOTATION,
				DatabaseHelper.RECIPIENT_NAME,
				DatabaseHelper.SENT_MESSAGE_SENT_DATE };
		
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
