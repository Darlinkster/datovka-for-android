package cz.nic.datovka.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import cz.nic.datovka.R;
import cz.nic.datovka.fragments.AccountFragment;

public class AccountActivity extends FragmentActivity{
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_activity);
						
		AccountFragment af = AccountFragment.newInstance();
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.account_activity, af);
		ft.commit();
	}
}
