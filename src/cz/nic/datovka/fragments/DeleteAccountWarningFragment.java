package cz.nic.datovka.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.AccountInfoActivity;
import cz.nic.datovka.connector.DatabaseTools;

public class DeleteAccountWarningFragment extends SherlockDialogFragment{
	public static final String MSGBOX_ID = "msgboxid";
	public static final String DIALOG_ID = "DeleteAccountWarningFragment";
	
	public static DeleteAccountWarningFragment newInstance(long msgboxId) {
		DeleteAccountWarningFragment dawf = new DeleteAccountWarningFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(MSGBOX_ID, msgboxId);
		
		dawf.setArguments(bundle);
		return dawf;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_account_warning)
               .setPositiveButton(R.string.delete_account_btn, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   DatabaseTools.deleteAccount(getArguments().getLong(MSGBOX_ID));
                	   Toast.makeText(getActivity(), R.string.account_deleted, Toast.LENGTH_SHORT).show();
                	   
                	   // if activity is AccountInfoActivity finish it
                	   Activity act = getActivity();
                	   if(act instanceof AccountInfoActivity) {
                		   act.finish();
                		   act = null;
                	   }
                   }
               })
               .setNegativeButton(R.string.storno, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
