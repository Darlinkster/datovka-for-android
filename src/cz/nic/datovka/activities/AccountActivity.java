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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.AccountListFragment;
import cz.nic.datovka.fragments.AddAccountFragment;

public class AccountActivity extends SherlockFragmentActivity {
	private FragmentManager fm;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_activity);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getText(R.string.title_activity_accounts));

		AccountListFragment af = AccountListFragment.newInstance();

		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.account_activity, af);
		ft.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.account_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_account_menu_btn) {
			AddAccountFragment aaf = new AddAccountFragment();
			aaf.show(fm, null);
			return true;
		}
		return false;
	}
	
	public void accountClicked(View view){
		Long msgBoxId = Long.parseLong((String) view.getTag());
		Intent intent = new Intent(this, AccountInfoActivity.class);
		intent.putExtra(AccountInfoActivity.MSGBOX_ID, msgBoxId);
		startActivity(intent);
	}
}
