package cz.nic.datovka.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.fragments.AddAccountFragment;
import cz.nic.datovka.fragments.ReceivedMessageListFragment;
import cz.nic.datovka.fragments.SentMessageListFragment;
import cz.nic.datovka.services.MessageBoxRefreshService;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener,
		LoaderCallbacks<Cursor> {

	private static SimpleCursorAdapter account_adapter;
	private static String selectedMsgBoxID;

	private static int selectedFolder = 0;

	// private static final int INBOX = 0;
	// private static final int OUTBOX = 1;

	private FragmentManager fragmentManager;
	private MyAdapter mAdapter;
	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fragmentManager = getSupportFragmentManager();
		ActionBar actionBar = getSupportActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		// account spinner setup
		String[] from = new String[] { DatabaseHelper.OWNER_FIRM_NAME };
		int[] to = new int[] { android.R.id.text1 };
		getSupportLoaderManager().initLoader(0, null, this);

		account_adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, from, to, 0);
		account_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		account_adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				int indexMsgBoxId = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
				// If the owner_firm_name is empty set that textview to
				// owner_name
				if (cursor.getString(colIndex).length() == 0) {
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
		actionBar.setListNavigationCallbacks(account_adapter, this);
		
		// Set selected account list item after screen rotation
		// Magic numbers - Because database indexes starting from 1, but actionbar spinner indexes from 0
		if (selectedMsgBoxID != null) {
			actionBar.setSelectedNavigationItem(Integer.parseInt(selectedMsgBoxID) - 1);
		}
		else {
			selectedMsgBoxID = Long.toString(actionBar.getSelectedNavigationIndex() + 1);
		}

		mAdapter = new MyAdapter(fragmentManager, selectedMsgBoxID, getApplicationContext());
		mPager = (ViewPager) findViewById(R.id.boxpager);
		mPager.setAdapter(mAdapter);
		
		TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.pagertitles);
		titleIndicator.setViewPager(mPager);
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// Folder was changed from INBOX to OUTBOX or vice-versa
				selectedFolder = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		// There is no account, jump on the create account dialogfragment
		int numberOfAccounts = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
				DatabaseHelper.msgbox_columns, null, null, null).getCount();
		if (numberOfAccounts < 1) {
			AddAccountFragment aaf = new AddAccountFragment();
			aaf.show(fragmentManager, null);
		}

	}

	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Another account was selected
		TextView tv = (TextView) account_adapter.getView(itemPosition, null, null);
		selectedMsgBoxID = (String) tv.getTag();

		mAdapter = new MyAdapter(fragmentManager, selectedMsgBoxID, getApplicationContext());
		mPager = (ViewPager) findViewById(R.id.boxpager);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(selectedFolder);

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
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
		case R.id.refresh_all:
			Intent intent = new Intent(getApplicationContext(), MessageBoxRefreshService.class);
			startService(intent);
			return true;
		}

		return false;
	}

	public void itemClicked(View view) {
		String id = (String) view.getTag();

		Intent i = new Intent(this, MessageDetailActivity.class);
		i.putExtra(MessageDetailActivity.ID, Long.parseLong(id));
		i.putExtra(MessageDetailActivity.FOLDER, selectedFolder);

		startActivity(i);
	}

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.OWNER_NAME,
				DatabaseHelper.OWNER_FIRM_NAME, DatabaseHelper.MSGBOX_ID };
		CursorLoader cursorLoader = new CursorLoader(this, MsgBoxContentProvider.CONTENT_URI, projection, null, null,
				null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		account_adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		account_adapter.swapCursor(null);
	}

	public static class MyAdapter extends FragmentPagerAdapter {
		private FragmentManager fm;

		private static String[] TITLES;
		private static int NUM_TITLES;
		private static String msgBoxID;
		
		public MyAdapter(FragmentManager fm, String msgBoxID, Context ctx) {
			super(fm);
			this.fm = fm;
			this.msgBoxID = msgBoxID;

			TITLES = new String[] { ctx.getResources().getString(R.string.inbox),
					ctx.getResources().getString(R.string.outbox) };
			NUM_TITLES = TITLES.length;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ReceivedMessageListFragment.getInstance(msgBoxID);
			case 1:
				return SentMessageListFragment.getInstance(msgBoxID);
			default:
				return null;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			FragmentTransaction bt = fm.beginTransaction();
			bt.remove((Fragment) object);
			bt.commit();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position % NUM_TITLES].toUpperCase();
		}
	}

}
