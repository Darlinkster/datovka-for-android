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

package cz.nic.datovka.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;

public class AddAccountFragment extends SherlockDialogFragment {
	public static final String PASSWORD = "pass";
	public static final String LOGIN = "login";
	public static final String TESTENV = "testenv";
	
	private String loginText;
	private String passwordText;

	public static AddAccountFragment newInstance(String login, String password, boolean testEnv) {
		AddAccountFragment aaf = new AddAccountFragment();
		Bundle args = new Bundle();
		args.putString(LOGIN, login);
		args.putString(PASSWORD, password);
		args.putBoolean(TESTENV, testEnv);
		
		aaf.setArguments(args);
		
		return aaf;
	}
	
	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View dialogView = inflater.inflate(R.layout.add_account_dialog, null);
		builder.setView(dialogView);
		builder.setTitle(R.string.add_acount);
		
		final EditText loginTv = (EditText) dialogView.findViewById(R.id.username);
		final EditText passwordTv = (EditText) dialogView.findViewById(R.id.password);
		final CheckBox testEnvCheckbox = (CheckBox) dialogView.findViewById(R.id.test_environment_checkbox);

		Bundle args = getArguments();
		String loginArg = null;
		String passwordArg = null;
		boolean testEnvArg = false;
		if (args != null) {
			loginArg = args.getString(LOGIN);
			passwordArg = args.getString(PASSWORD);
			testEnvArg = args.getBoolean(TESTENV);

			if (loginArg != null) {
				loginTv.setText(loginArg);
			}
			if (passwordArg != null) {
				passwordTv.setText(passwordArg);
			}
			testEnvCheckbox.setChecked(testEnvArg);
		}

		
		builder.setPositiveButton(R.string.add_account_button, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				loginText = loginTv.getText().toString();
				passwordText = passwordTv.getText().toString();
				boolean testEnv = testEnvCheckbox.isChecked();
				
				if ((loginText.length() == 0) || (passwordText.length() < 8) || (passwordText.length() > 32)) {
					Toast.makeText(Application.ctx, R.string.account_create_bad_login, Toast.LENGTH_SHORT).show();
					reShow(loginText, passwordText, testEnv);
					return;
				}
				AddAccountProgressBarFragment ipbf = AddAccountProgressBarFragment.newInstance(loginText, passwordText, testEnv);
				ipbf.show(getFragmentManager(), null);
				dialog.dismiss();
				
			}
		});
		builder.setNegativeButton(R.string.storno, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});

		return builder.create();
	}

	private void reShow(String login, String password, boolean testEnv) {
		AddAccountFragment aaf = AddAccountFragment.newInstance(login, password, testEnv);
		aaf.show(getFragmentManager(), null);
	}
}
