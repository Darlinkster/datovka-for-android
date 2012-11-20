package cz.nic.datovka.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseTools;
import cz.nic.datovka.fragments.AccountInfoFragment;

public class AccountInfoActivity extends SherlockFragmentActivity{
	public final static String MSGBOX_ID = "msgid";
	private long msgBoxId;
	private FragmentManager fm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_info_activity);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getText(R.string.title_activity_account_info));
		
		Intent i = getIntent();
		this.msgBoxId = i.getLongExtra(MSGBOX_ID, 0);
		
		AccountInfoFragment aif = AccountInfoFragment.newInstance(msgBoxId);
		
		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.account_info_activity, aif);
		ft.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.account_info_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.remove_account_menu_btn) {
			DatabaseTools.deleteAccount(msgBoxId);
			Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
			this.finish();
			return true;
		}
		return false;
	}
}
