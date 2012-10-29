package cz.nic.datovka.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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
import cz.nic.datovka.contentProviders.MessageListCursorAdapter;
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
		String selectionArgs = DatabaseHelper.SENT_MESSAGE_MSGBOX_ID + " = "
				+ getArguments().getString(MSGBOXID);
		
		String[] projection = new String[] {
				DatabaseHelper.SENT_MESSAGE_MSGBOX_ID,
				DatabaseHelper.SENT_MESSAGE_ID,
				DatabaseHelper.SENT_MESSAGE_ANNOTATION,
				DatabaseHelper.RECIPIENT_NAME,
				DatabaseHelper.SENT_MESSAGE_SENT_DATE,
				DatabaseHelper.SENT_MESSAGE_IS_READ };
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				SentMessagesContentProvider.CONTENT_URI, projection,
				selectionArgs, null, null);

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
		
		adapter = new MessageListCursorAdapter(context,
				R.layout.message_list_fragment, null, from,
				to, DatabaseHelper.SENT_MESSAGE_IS_READ, 
				getResources().getColor(R.color.gray), 
				getResources().getColor(R.color.dimwhite), 0);
		
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				int messageIdIndex = cursor.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ID);
				TextView textView = (TextView) view;
				// covert date to human readable format
				if(view.getId() == R.id.message_item_date){
					String date = cursor.getString(colIndex);
					textView.setText(AndroidUtils.FromXmlToHumanReadableDate(date));
					
					return true;
				}
				
				// Add database id to tag parent tag
				((View) view.getParent()).setTag(cursor.getString(messageIdIndex));
				return false;
			}
		});

		setListAdapter(adapter);
	}
}


