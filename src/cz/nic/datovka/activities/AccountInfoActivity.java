package cz.nic.datovka.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.AccountInfoFragment;

public class AccountInfoActivity extends SherlockFragmentActivity{
	public final static String MSGBOX_ID = "msgid";
	private long accountId;
	private FragmentManager fm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_info_activity);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
		Intent i = getIntent();
		this.accountId = i.getLongExtra(MSGBOX_ID, 0);
		
		AccountInfoFragment aif = AccountInfoFragment.newInstance(accountId);
		
		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.account_info_activity, aif);
		ft.commit();
	}
}
