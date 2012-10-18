package cz.nic.datovka.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.services.AddAccountService;

public class AddAccountFragment extends DialogFragment {
	Context context;
	
	 private Handler handler = new Handler() {
		 public void handleMessage(Message message){
			 if(message.arg1 == AddAccountService.RESULT_OK) {
				 Toast.makeText(context, R.string.account_created, Toast.LENGTH_SHORT).show();
			 }
			 else{
				 Toast.makeText(context, R.string.account_not_created, Toast.LENGTH_SHORT).show();
			 }
		 }
	 };

	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		context = getActivity();
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = inflater.inflate(R.layout.add_account_dialog,
				null);
		builder.setView(dialogView);

		builder.setTitle(R.string.add_acount);
		builder.setPositiveButton(R.string.add_account_button,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						EditText loginTv = (EditText) dialogView
								.findViewById(R.id.username);
						EditText passwordTv = (EditText) dialogView
								.findViewById(R.id.password);

						Messenger messenger = new Messenger(handler);
						Intent intent = new Intent(getActivity(),
								AddAccountService.class);
						intent.putExtra(AddAccountService.HANDLER, messenger);
						//intent.putExtra(AddAccountService.LOGIN, loginTv
						//		.getText().toString());
					//	intent.putExtra(AddAccountService.PASSWORD, passwordTv
						//		.getText().toString());
						
						intent.putExtra(AddAccountService.LOGIN, "co55on");
						intent.putExtra(AddAccountService.PASSWORD,"Fx2MAt3u8wDRL5");

						getActivity().startService(intent);
					}
				});
		builder.setNegativeButton(R.string.storno, null);

		return builder.create();
	}
}
