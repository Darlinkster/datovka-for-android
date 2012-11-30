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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.services.AddAccountService;

public class AddAccountFragment extends SherlockDialogFragment {
	private static ProgressDialog mProgressDialog;

	private static Handler handler = new Handler() {
		public void handleMessage(Message message) {
			if(mProgressDialog.getWindow() != null)
				mProgressDialog.dismiss();

			if (message.arg1 == AddAccountService.RESULT_OK) {
				Toast.makeText(Application.ctx, R.string.account_created, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_EXISTS) {
				Toast.makeText(Application.ctx, R.string.account_exists, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_ERR) {
				Toast.makeText(Application.ctx, R.string.account_create_error, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_BAD_LOGIN) {
				Toast.makeText(Application.ctx, R.string.account_create_bad_login, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_DS_ERR) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_NO_CONNECTION) {
				Toast.makeText(Application.ctx, R.string.no_connection , Toast.LENGTH_SHORT).show();
			}
		}
	};

	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = inflater.inflate(R.layout.add_account_dialog, null);
		builder.setView(dialogView);
		
		builder.setTitle(R.string.add_acount);
		builder.setPositiveButton(R.string.add_account_button, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				EditText loginTv = (EditText) dialogView.findViewById(R.id.username);
				EditText passwordTv = (EditText) dialogView.findViewById(R.id.password);
				CheckBox testEnvCheckbox = (CheckBox) dialogView.findViewById(R.id.test_environment_checkbox);

				Messenger messenger = new Messenger(handler);
				Intent intent = new Intent(getActivity(), AddAccountService.class);
				intent.putExtra(AddAccountService.HANDLER, messenger);
				intent.putExtra(AddAccountService.LOGIN, loginTv.getText().toString());
				intent.putExtra(AddAccountService.PASSWORD, passwordTv.getText().toString());
				intent.putExtra(AddAccountService.TESTENV, testEnvCheckbox.isChecked());

				if (loginTv.getText().toString().length() == 0) {
					return;
				}

				getActivity().startService(intent);

				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog.setIndeterminate(true);
				mProgressDialog.setMessage(new String(getResources().getString(R.string.account_create_progress)));
				mProgressDialog.show();
			}
		});
		builder.setNegativeButton(R.string.storno, null);

		return builder.create();
	}
}
