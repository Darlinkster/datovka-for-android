package cz.nic.datovka.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.R.color;
import cz.nic.datovka.R.id;
import cz.nic.datovka.R.layout;
import cz.nic.datovka.R.menu;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.fragments.ReceivedMessageListFragment;
import cz.nic.datovka.fragments.SentMessageListFragment;

public class MainActivity extends FragmentActivity implements OnItemSelectedListener,
LoaderCallbacks<Cursor>{
	
	private FragmentManager fragmentManager;
	private SimpleCursorAdapter account_adapter;
	private String selectedMsgBoxID;
	private int selectedFolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fragmentManager = getSupportFragmentManager();
		
		Spinner folder_spinner = (Spinner) findViewById(R.id.folder_spinner);
		Spinner account_spinner = (Spinner) findViewById(R.id.account_spinner);

		// folder spinner setup 
		ArrayAdapter<CharSequence> folder_adapter = ArrayAdapter.createFromResource(this, R.array.folder_spinner, android.R.layout.simple_spinner_item);
		folder_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		folder_spinner.setAdapter(folder_adapter);
		folder_spinner.setOnItemSelectedListener(this);
		
		// account spinner setup
		String[] from = new String[]{DatabaseHelper.OWNER_FIRM_NAME};
		int[] to = new int[]{android.R.id.text1};
		getSupportLoaderManager().initLoader(0, null, this);
		account_adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, from, to, 0);
		account_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		account_adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				int indexMsgBoxId = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
				// If the owner_firm_name is empty set that textview to owner_name
				if(cursor.getString(colIndex).length() == 0){
					int indexOwnerName = cursor.getColumnIndex(DatabaseHelper.OWNER_NAME);
					tv.setText(cursor.getString(indexOwnerName));
					// Set msgbox ID as a tag
					tv.setTag(cursor.getString(indexMsgBoxId));
					return true;
				}
				
				// Set msgbox ID as a tag
				tv.setTag(cursor.getString(indexMsgBoxId));
				return false;	
			}
		});
		account_spinner.setAdapter(account_adapter);
		account_spinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_accounts:
				startActivity(new Intent(this, AccountActivity.class));
				return true;
			case R.id.menu_settings:
				Toast.makeText(this, "nastaveni", Toast.LENGTH_SHORT).show();
				return true;
		}
		
		return true;
	}

	public void itemClicked(View view) {
		view.setBackgroundColor(getResources().getColor(R.color.gray));
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		
		annotation.setTypeface(null, Typeface.NORMAL);
		sender.setTypeface(null, Typeface.NORMAL);
		
		int id = view.getId();
		Intent i = new Intent(this, MessageDetailActivity.class);
		i.putExtra(MessageDetailActivity.ID, id);
		startActivity(i);
	}

	public void onItemSelected(AdapterView<?> parent, View row, int pos, long id) {
		if(parent.getId() == R.id.folder_spinner){
			if (pos == 0){
				// Inbox
				selectedFolder = pos;
				ReceivedMessageListFragment rmlf = ReceivedMessageListFragment.getInstance(selectedMsgBoxID);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_linearlayout, rmlf);
				ft.commit();
			}
			else if(pos == 1){
				// Outbox
				selectedFolder = pos;
				SentMessageListFragment smlf = SentMessageListFragment.getInstance(selectedMsgBoxID);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_linearlayout, smlf);
				ft.commit();
			}
		}
		else if (parent.getId() == R.id.account_spinner){
			// Get msgbox ID from textview tag
			TextView tv = (TextView) row;
			selectedMsgBoxID = (String) tv.getTag();
			if (selectedFolder == 0){
				// Inbox
				ReceivedMessageListFragment rmlf = ReceivedMessageListFragment.getInstance(selectedMsgBoxID);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_linearlayout, rmlf);
				ft.commit();
			}
			else if(selectedFolder == 1){
				// Outbox
				SentMessageListFragment smlf = SentMessageListFragment.getInstance(selectedMsgBoxID);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_linearlayout, smlf);
				ft.commit();
			}
		}
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[]{ DatabaseHelper.MSGBOX_ID,
				DatabaseHelper.OWNER_NAME, DatabaseHelper.OWNER_FIRM_NAME, DatabaseHelper.MSGBOX_ID};
		CursorLoader cursorLoader = new CursorLoader(this,
				MsgBoxContentProvider.CONTENT_URI, projection, null, null,
				null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		account_adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		account_adapter.swapCursor(null);
	}
}
