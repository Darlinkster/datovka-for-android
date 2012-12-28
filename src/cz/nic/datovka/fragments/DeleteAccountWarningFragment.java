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
