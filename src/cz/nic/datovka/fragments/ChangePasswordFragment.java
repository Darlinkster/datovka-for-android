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

import org.kobjects.base64.Base64;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;

public class ChangePasswordFragment extends SherlockDialogFragment {
	public static final String MSGBOX_ID = "msgboxid";
	public static final String DIALOG_ID = "ChangePasswordFragment";
	
	
	public static ChangePasswordFragment newInstance(long msgBoxId){
		ChangePasswordFragment cpf = new ChangePasswordFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(MSGBOX_ID, msgBoxId);
		cpf.setArguments(bundle);
		return cpf;
	}

	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = inflater.inflate(R.layout.change_passwd_account_dialog, null);
		builder.setView(dialogView);
		
		final long msgBoxId = getArguments().getLong(MSGBOX_ID);

		builder.setTitle(R.string.change_login_credentials);
		builder.setNegativeButton(R.string.storno, null);
		builder.setPositiveButton(R.string.change_passwd_account_button, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText passwordTv = (EditText) dialogView.findViewById(R.id.new_password);
				String newPass = passwordTv.getText().toString();
				String newEncPass = Base64.encode(newPass.getBytes());
				Uri uri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);

				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.MSGBOX_PASSWORD, newEncPass);
				
				int passwordLength = newPass.length();
				if ((passwordLength < 8) || (passwordLength > 32)) {
					Toast.makeText(Application.ctx, R.string.passwd_change_fail, Toast.LENGTH_LONG).show();
				} else {

					if (Application.ctx.getContentResolver().update(uri, values, null, null) > 0) {
						Toast.makeText(Application.ctx, R.string.passwd_change_success, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(Application.ctx, R.string.passwd_change_fail, Toast.LENGTH_LONG).show();
					}
				}

			}

		});

		return builder.create();
	}
}
