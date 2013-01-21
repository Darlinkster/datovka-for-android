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

package cz.nic.datovka.activities;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import cz.nic.datovka.fragments.MessageListFragment;
import cz.nic.datovka.fragments.UpdateNoticeFragment;
import cz.nic.datovka.services.MessageBoxRefreshService;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener, LoaderCallbacks<Cursor> {
	private static SimpleCursorAdapter account_adapter;
	private static String selectedMsgBoxID;
	private static int selectedFolder = 0;
	private static boolean animateRefreshIcon = false;

	private ActionBar actionBar;
	private FragmentManager fragmentManager;
	private MyAdapter mAdapter;
	private ViewPager mPager;
	private static MenuItem refreshButtonItem;
	private static AddAccountFragment aaf;
	private SharedPreferences prefs;
	private boolean usePinStatus;
	
	private static final String ICON_ANIMATION_STATE = "refresh_icon_animation"; 
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	//first saving my state, so the bundle wont be empty.
    	//http://code.google.com/p/android/issues/detail?id=19917
    	outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
    	super.onSaveInstanceState(outState);
    	outState.putBoolean(ICON_ANIMATION_STATE, animateRefreshIcon);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		usePinStatus = prefs.getBoolean("use_pin_code", false);
		
		if(savedInstanceState != null) {
			animateRefreshIcon = savedInstanceState.getBoolean(ICON_ANIMATION_STATE, false);
			
		}
		
		setContentView(R.layout.activity_main);
		fragmentManager = getSupportFragmentManager();
		AppUtils.ctx = getApplicationContext();

		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		setupAccountSpinner();
				
		// Set selected account list item after screen rotation
		// Magic numbers - Because database indexes starting from 1, but actionbar spinner indexes from 0
		if (selectedMsgBoxID != null) {
			actionBar.setSelectedNavigationItem(Integer.parseInt(selectedMsgBoxID) - 1);
		}
		else {
			selectedMsgBoxID = Long.toString(actionBar.getSelectedNavigationIndex() + 1);
		}
		
		// If there is no account, jump on the create account dialogfragment
		showAddAccountFragment();
		showOnUpdateWarning();
	}
	
	// Listening on spinner with accounts. Recreates fragments in viewpager when
	// account is switched.
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Another account was selected
		TextView tv = (TextView) account_adapter.getView(itemPosition, null, null);
		selectedMsgBoxID = (String) tv.getTag();
		updateFragmentPager();
		
		return true;
	}
	
	private void updateFragmentPager(){
		mAdapter = new MyAdapter(fragmentManager, selectedMsgBoxID, getString(R.string.inbox), getString(R.string.outbox));
		mPager = (ViewPager) findViewById(R.id.boxpager);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(selectedFolder);
		/* HACK !!!!!!!
		 * Obcas se na Nexus7 s JB stava ze ViewPager zobrazi prazdnou View, 
		 * zrejme kvuli tomu ze adapter nestaci ve svem vlakne dokoncit vytvoreni instance fragmentu.
		 * Nasledujici prikaz zaruci, ze bude View znovu prekreslena (teoreticky je to pomale, prakticky jsem si nevsim)
		 * a tim padem spravne zobrazena.
		 * Mozna se to vstahuje k tomuto http://code.google.com/p/android/issues/detail?id=19211.
		 * */
		mPager.requestLayout();
		
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
	}

	private void setupAccountSpinner() {
		String[] from = new String[] { DatabaseHelper.OWNER_NAME };
		int[] to = new int[] { android.R.id.text1 };
		getSupportLoaderManager().initLoader(0, null, this);

		account_adapter = new SimpleCursorAdapter(this, R.layout.sherlock_spinner_item, null, from, to, 0);
		account_adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		account_adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int colIndex) {
				TextView tv = (TextView) view;
				int indexMsgBoxId = cursor.getColumnIndex(DatabaseHelper.MSGBOX_ID);
				
				// Set msgbox ID as a tag
				tv.setTag(cursor.getString(indexMsgBoxId));
				return false;
			}
		});
		actionBar.setListNavigationCallbacks(account_adapter, this);
	}
	
	private void showAddAccountFragment() {
		Cursor msgBoxes = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
				DatabaseHelper.msgbox_columns, null, null, null);
		int numberOfAccounts = msgBoxes.getCount();
		msgBoxes.close();
		if (numberOfAccounts < 1) {
			if(aaf == null){
				aaf = new AddAccountFragment();
				aaf.show(fragmentManager, AddAccountFragment.DIALOG_ID);
			}
			updateFragmentPager();
		}
		msgBoxes = null;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(!usePinStatus)
			menu.removeItem(R.id.menu_lock_app);
		return super.onPrepareOptionsMenu(menu);
	}

	// Constructs actionbar menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		refreshButtonItem = menu.findItem(R.id.refresh_all);
		return true;
	}
	
	// Clicking on actionbar menu icons
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_accounts:
			startActivity(new Intent(this, AccountActivity.class));
			return true;
	
		case R.id.menu_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			return true; 
			
		case R.id.refresh_all:
			refreshButtonItem = item;
			setAnimationOnRefreshButton();
			
			Messenger messenger = new Messenger(handler);
			Intent intent = new Intent(getApplicationContext(), MessageBoxRefreshService.class);
			intent.putExtra(MessageBoxRefreshService.HANDLER, messenger);
			startService(intent);
			return true;
		
		case R.id.menu_lock_app:
			this.finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus) {
			if(animateRefreshIcon){
				setAnimationOnRefreshButton();
			}
			boolean usePinStatus = prefs.getBoolean("use_pin_code", false);
			if(this.usePinStatus != usePinStatus) {
				this.usePinStatus = usePinStatus;
				invalidateOptionsMenu();
			}
		} else {
			removeAnimationFromRefreshButton();
		}
		super.onWindowFocusChanged(hasFocus);
	}
	
	// This method takes care about clicking on messages
	public void itemClicked(View view) {
		String id = (String) view.getTag();
		Intent i = new Intent(this, MessageDetailActivity.class);
		i.putExtra(MessageDetailActivity.ID, Long.parseLong(id));
		startActivity(i);
	}

	// Three methods for accounts content resolver
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = new String[] { DatabaseHelper.MSGBOX_ID, DatabaseHelper.OWNER_NAME };
		CursorLoader cursorLoader = new CursorLoader(this, MsgBoxContentProvider.CONTENT_URI, projection, null, null, null);

		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		account_adapter.swapCursor(arg1);
	}

	public void onLoaderReset(Loader<Cursor> arg0) {
		account_adapter.swapCursor(null);
	}
	
	private static final void removeAnimationFromRefreshButton(){
		if(refreshButtonItem != null) {
			View actionView = refreshButtonItem.getActionView();
			if(actionView != null){
				actionView.clearAnimation();
				actionView = null;
				refreshButtonItem.setActionView(null);
			}
			//refreshButtonItem = null;
		} 
	}
	
	private static final void setAnimationOnRefreshButton() {
		if(refreshButtonItem != null) {
			animateRefreshIcon = true;
			LayoutInflater inflater = (LayoutInflater) AppUtils.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView refreshButtonView = (ImageView) inflater.inflate(R.layout.refresh_button_view, null);
			Animation rotation = AnimationUtils.loadAnimation(AppUtils.ctx, R.anim.anim_rotate);
			refreshButtonView.startAnimation(rotation);
			refreshButtonItem.setActionView(refreshButtonView);
		}
	}
	
	// Handler for handling messages from MessageBoxRefresh service 
	private static Handler handler = new Handler() {
		public void handleMessage(Message message) {
			removeAnimationFromRefreshButton();
			animateRefreshIcon = false;
			
			if(message.arg1 == MessageBoxRefreshService.ERROR){
				Toast.makeText(AppUtils.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			}
			else if(message.arg1 == MessageBoxRefreshService.ERROR_NO_CONNECTION){
				Toast.makeText(AppUtils.ctx, R.string.no_connection, Toast.LENGTH_LONG).show();
			}
			else if(message.arg1 == MessageBoxRefreshService.ERROR_BAD_LOGIN){
				Toast.makeText(AppUtils.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			}
			else if((message.arg1) == 0 && (message.arg2 == 0)){
				Toast.makeText(AppUtils.ctx, R.string.no_new_messages, Toast.LENGTH_LONG).show();
			} 
			else if(message.arg1 == MessageBoxRefreshService.ERROR_CERT){
				Toast.makeText(AppUtils.ctx, R.string.cert_error, Toast.LENGTH_LONG).show();
			}
			else if(message.arg1 == MessageBoxRefreshService.ERROR_INTERRUPTED){
				Toast.makeText(AppUtils.ctx, R.string.stream_interrupted, Toast.LENGTH_LONG).show();
			}
			else if(message.arg1 == MessageBoxRefreshService.ERROR_MSGBOXID_NOTKNOWN){
				Toast.makeText(AppUtils.ctx, R.string.msgbox_id_not_known, Toast.LENGTH_LONG).show();
			}
			else {
				String newMessages = new String(AppUtils.ctx.getResources().getString(R.string.new_messages_with_count, message.arg1, message.arg2));
				Toast.makeText(AppUtils.ctx, newMessages, Toast.LENGTH_LONG).show();
			}
		}
	};

	// Class for loading fragments to viewpager
	public static class MyAdapter extends FragmentPagerAdapter {
		private static FragmentManager fm;

		private static String[] TITLES;
		private static int NUM_TITLES;
		private static String msgBoxID;
		
		public MyAdapter(FragmentManager fm, String msgBoxID, String inbox, String outbox) {
			super(fm);
			MyAdapter.fm = fm;
			MyAdapter.msgBoxID = msgBoxID;

			TITLES = new String[] { inbox, outbox };
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
				return MessageListFragment.getInstance(msgBoxID, AppUtils.INBOX);
			case 1:
				return MessageListFragment.getInstance(msgBoxID, AppUtils.OUTBOX);
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
			return TITLES[position % NUM_TITLES].toUpperCase(Locale.getDefault());
		}
	}
	
	private void showOnUpdateWarning() {
		final String VERSION_KEY = "datovka_version";
		final int NO_VERSION = -1;
		
		int thisVersion = NO_VERSION;
		try {
			thisVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		int lastVersion = prefs.getInt(VERSION_KEY, NO_VERSION);
		
		if ((thisVersion == 4) && (lastVersion < thisVersion)) {
			Cursor msgBoxes = getContentResolver().query(MsgBoxContentProvider.CONTENT_URI,
					DatabaseHelper.msgbox_columns, null, null, null);
			int numberOfAccounts = msgBoxes.getCount();
			msgBoxes.close();
			msgBoxes = null;
			if (numberOfAccounts > 0) {
				Log.d(this.getLocalClassName(), "Showing warning message. thisVersion " + thisVersion + " lastVersion " + lastVersion);
				String msg = "Vzhledem k rozsáhlým změnám interní databáze této aplikace, bylo nutné odstranit veškeré lokálně stažené zprávy. "
						+ "Nastavení účtů zůstalo zachováno. Pro opětovné stažení vašich zpráv stiskněte tlačítko obnovení v pravém horním rohu (dvě šipky).";
				UpdateNoticeFragment.newInstance(msg).show(fragmentManager, UpdateNoticeFragment.DIALOG_ID);
			}
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(VERSION_KEY, thisVersion);
			editor.commit();
		}
	}
}
