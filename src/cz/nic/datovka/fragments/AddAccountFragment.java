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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.services.AddAccountService;

public class AddAccountFragment extends DialogFragment {
	private static ProgressDialog mProgressDialog;

	private static Handler handler = new Handler() {
		public void handleMessage(Message message) {
			if (message.arg1 == AddAccountService.RESULT_OK) {
				mProgressDialog.dismiss();
				Toast.makeText(Application.ctx, R.string.account_created, Toast.LENGTH_SHORT).show();
			}

			else if (message.arg1 == AddAccountService.RESULT_EXISTS) {
				mProgressDialog.dismiss();
				Toast.makeText(Application.ctx, R.string.account_exists, Toast.LENGTH_SHORT).show();
			}

			else if (message.arg1 == AddAccountService.RESULT_ERR) {
				mProgressDialog.dismiss();
				Toast.makeText(Application.ctx, R.string.account_create_error, Toast.LENGTH_SHORT).show();
			}

			else if (message.arg1 == AddAccountService.RESULT_BAD_LOGIN) {
				mProgressDialog.dismiss();
				Toast.makeText(Application.ctx, R.string.account_create_bad_login, Toast.LENGTH_SHORT).show();
			}
			else if (message.arg1 == AddAccountService.RESULT_DS_ERR) {
				mProgressDialog.dismiss();
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_SHORT).show();
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

				// TODO HACK
				if (loginTv.getText().toString().length() == 0) {
					intent.putExtra(AddAccountService.LOGIN, "co55on");
					intent.putExtra(AddAccountService.PASSWORD, "Fx2MAt3u8wDRL5");
					intent.putExtra(AddAccountService.TESTENV, true);
				}
				// TODO END HACK

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
