package cz.nic.datovka.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import cz.nic.datovka.R;
import cz.nic.datovka.fragments.AccountListFragment;
import cz.nic.datovka.fragments.AddAccountFragment;

public class AccountActivity extends FragmentActivity {
	private FragmentManager fm;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_activity);
						
		AccountListFragment af = AccountListFragment.newInstance();
		
		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.account_activity, af);
		ft.commit();
	}
	
	public void onClick(View view) {
		if (view.getId() == R.id.add_account_button) {
			
			AddAccountFragment aaf = new AddAccountFragment();
			aaf.show(fm, null);
		}
	}
}
