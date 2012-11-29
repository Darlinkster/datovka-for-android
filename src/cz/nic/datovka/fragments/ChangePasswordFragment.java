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

		builder.setTitle(R.string.change_passwd_account_button);
		builder.setNegativeButton(R.string.storno, null);
		builder.setPositiveButton(R.string.change_passwd_account_button, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText passwordTv = (EditText) dialogView.findViewById(R.id.new_password);
				String newEncPass = Base64.encode(passwordTv.getText().toString().getBytes());
				Uri uri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);

				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.MSGBOX_PASSWORD, newEncPass);
				
				if(Application.ctx.getContentResolver().update(uri, values, null, null) > 0){
					Toast.makeText(Application.ctx, R.string.passwd_change_success, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(Application.ctx, R.string.passwd_change_fail, Toast.LENGTH_LONG).show();
				}

			}

		});

		return builder.create();
	}
}
