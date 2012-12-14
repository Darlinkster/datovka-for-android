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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.AccountInfoFragment;
import cz.nic.datovka.fragments.ChangePasswordFragment;
import cz.nic.datovka.fragments.DeleteAccountWarningFragment;

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
			DeleteAccountWarningFragment dawf = DeleteAccountWarningFragment.newInstance(msgBoxId);
			dawf.show(fm, DeleteAccountWarningFragment.DIALOG_ID);
			return true;
		}
		else if (item.getItemId() == R.id.change_passwd_account_menu_btn) {
			ChangePasswordFragment cpf = ChangePasswordFragment.newInstance(this.msgBoxId);
			cpf.show(fm, ChangePasswordFragment.DIALOG_ID);
			return true;
		}
		return false;
	}
}
