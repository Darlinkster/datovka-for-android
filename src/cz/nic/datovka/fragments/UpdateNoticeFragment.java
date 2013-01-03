package cz.nic.datovka.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class UpdateNoticeFragment extends DialogFragment {
	public static final String MESSAGE = "msg";
	public static final String DIALOG_ID = "UpdateNoticeFragment";
	
	String message;
	
	public static UpdateNoticeFragment newInstance(String message) {
		UpdateNoticeFragment unf = new UpdateNoticeFragment();
		Bundle bundle = new Bundle();
		bundle.putString(MESSAGE, message);
		unf.setArguments(bundle);
		
		return unf;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Aktualizace");
		builder.setMessage(this.getArguments().getString(MESSAGE));
		builder.setPositiveButton("OK", null);
		return builder.create();
	}
}
