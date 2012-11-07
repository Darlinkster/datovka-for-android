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
import android.widget.ListView;
import android.widget.TextView;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessageListCursorAdapter;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class ReceivedMessageListFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private SimpleCursorAdapter adapter;
	private static String MSGBOXID = "msgboxid";
	
	public static ReceivedMessageListFragment getInstance(String arg){
		ReceivedMessageListFragment rmlf = new ReceivedMessageListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MSGBOXID, arg);
		rmlf.setArguments(bundle);
		return rmlf;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
		ListView v = getListView();
		registerForContextMenu(v);
		//v.setSelector(R.drawable.message_listitem_background);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] {
				DatabaseHelper.RECEIVED_MESSAGE_ID,
				DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID,
				DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION,
				DatabaseHelper.SENDER_NAME,
				DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE,
				DatabaseHelper.RECEIVED_MESSAGE_IS_READ};
		
		String selectionArgs = DatabaseHelper.RECEIVED_MESSAGE_MSGBOX_ID
				+ " = " + getArguments().getString(MSGBOXID);

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				ReceivedMessagesContentProvider.CONTENT_URI, projection,
				selectionArgs, null, DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE + " DESC");

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
		
		adapter = new MessageListCursorAdapter(context,
				R.layout.message_list_fragment, null, from, to,
				DatabaseHelper.RECEIVED_MESSAGE_IS_READ, 
				0);
		
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				int messageIdIndex = cursor.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ID);
				TextView textView = (TextView) view;
				// covert date to human readable format
				if(view.getId() == R.id.message_item_date){
					String date = cursor.getString(colIndex);
					textView.setText(AndroidUtils.FromXmlToHumanReadableDate(date));
					
					return true;
				}
				
				// Add database id to the parent tag
				((View) view.getParent()).setTag(cursor.getString(messageIdIndex));
				return false;
			}
		});

		setListAdapter(adapter);
	}
}

