package cz.nic.datovka.fragments;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.AccountContentProvider;
import cz.nic.datovka.connector.DatabaseHelper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddAccountFragment extends DialogFragment{

	public Dialog onCreateDialog(Bundle SavedInstanceState){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = inflater.inflate(
				R.layout.add_account_dialog, null);
		builder.setView(dialogView);

		builder.setTitle(R.string.add_acount);
		builder.setPositiveButton(R.string.add_account_button,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						EditText loginTv = (EditText) dialogView
								.findViewById(R.id.username);
						EditText passwordTv = (EditText) dialogView
								.findViewById(R.id.password);

						ContentValues values = new ContentValues();
						values.put(DatabaseHelper.ACCOUNT_LOGIN, loginTv
								.getText().toString());
						values.put(DatabaseHelper.ACCOUNT_PASSWORD,
								passwordTv.getText().toString());
						getActivity().getContentResolver().insert(
								AccountContentProvider.CONTENT_URI, values);
					}
				});
		builder.setNegativeButton(R.string.storno, null);
		
		return builder.create();
	}
}
