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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.services.AddAccountService;

public class AddAccountProgressBarFragment extends SherlockDialogFragment {
	public static final String PASSWORD = "pass";
	public static final String LOGIN = "login";
	public static final String TEST_ENV = "testenv";
	public static final String DIALOG_ID = "AddAccountProgressBarFragment";
	
	private static ProgressDialog pd;
	private static String login;
	private static String password;
	private static boolean testEnv;
	private static boolean run = true;
	private static FragmentManager fragmentManager;
	
	public static AddAccountProgressBarFragment newInstance(String login, String password, boolean testEnv) {
		AddAccountProgressBarFragment mdpf = new AddAccountProgressBarFragment();
		Bundle bundle = new Bundle();
		bundle.putString(LOGIN, login);
		bundle.putString(PASSWORD, password);
		bundle.putBoolean(TEST_ENV, testEnv);
		
		mdpf.setArguments(bundle);
		return mdpf;
	}

	@Override
	public void onResume () {
		super.onResume();
		fragmentManager = getActivity().getSupportFragmentManager();
	}
	
	@Override
	public void onPause () {
		super.onPause();
		fragmentManager = null;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		SherlockDialogFragment sdf = (SherlockDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(AddAccountFragment.DIALOG_ID);
		if(sdf != null){
			sdf.dismiss();
		}
		
		pd = new ProgressDialog(getActivity());

		if (run) {
			run = false;
			
			Bundle args = getArguments();
			login = args.getString(LOGIN);
			password = args.getString(PASSWORD);
			testEnv = args.getBoolean(TEST_ENV);
			
			Messenger messenger = new Messenger(handler);
			Intent intent = new Intent(getActivity(), AddAccountService.class);
			intent.putExtra(AddAccountService.HANDLER, messenger);
			intent.putExtra(AddAccountService.LOGIN, login);
			intent.putExtra(AddAccountService.PASSWORD, password);
			intent.putExtra(AddAccountService.TESTENV, testEnv);

			getActivity().startService(intent);
		}

		pd.setIndeterminate(true);
		pd.setCancelable(true);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(new String(getResources().getString(R.string.account_create_progress)));

		return pd;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Application.ctx.stopService(new Intent(Application.ctx, AddAccountService.class));
		super.onCancel(dialog);
	}
	
	public static Handler handler = new Handler() {

		public void handleMessage(Message message) {

			if (message.arg1 == AddAccountService.RESULT_OK) {
				Toast.makeText(Application.ctx, R.string.account_created, Toast.LENGTH_SHORT).show();
				dismissProgressBar();
			} else if (message.arg1 == AddAccountService.RESULT_EXISTS) {
				Toast.makeText(Application.ctx, R.string.account_exists, Toast.LENGTH_SHORT).show();
				showLoginForm();
			} else if (message.arg1 == AddAccountService.RESULT_ERR) {
				Toast.makeText(Application.ctx, R.string.account_create_error, Toast.LENGTH_SHORT).show();
				showLoginForm();
			} else if (message.arg1 == AddAccountService.RESULT_BAD_LOGIN) {
				Toast.makeText(Application.ctx, R.string.account_create_bad_login, Toast.LENGTH_SHORT).show();
				showLoginForm();
			} else if (message.arg1 == AddAccountService.RESULT_DS_ERR) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_SHORT).show();
				showLoginForm();
			} else if (message.arg1 == AddAccountService.RESULT_NO_CONNECTION) {
				Toast.makeText(Application.ctx, R.string.no_connection, Toast.LENGTH_SHORT).show();
				showLoginForm();
			} else if (message.arg1 == AddAccountService.RESULT_BAD_CERT) {
				Toast.makeText(Application.ctx, R.string.cert_error, Toast.LENGTH_SHORT).show();
				showLoginForm();
			}

		}
		
		private void showLoginForm() {
			dismissProgressBar();
			if (fragmentManager != null) {
				AddAccountFragment.newInstance(login, password, testEnv).show(fragmentManager, AddAccountFragment.DIALOG_ID);
			}
		}
		
		private void dismissProgressBar() {
			run = true;
			if (pd != null) 
				pd.dismiss();
		}
	};
}
