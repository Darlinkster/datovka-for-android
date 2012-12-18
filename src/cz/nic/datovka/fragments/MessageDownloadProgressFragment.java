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
import android.os.ResultReceiver;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.services.MessageDownloadService;

public class MessageDownloadProgressFragment extends SherlockDialogFragment {
	public static final String DIALOG_ID = "MessageDownloadProgressFragment";
	private static ProgressDialog mProgressDialog;
	private static Handler handler;

	private static final String MSG_ID = "msgid";
	private static boolean runService = true;

	public static MessageDownloadProgressFragment newInstance(long messageId) {
		MessageDownloadProgressFragment mdpf = new MessageDownloadProgressFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(MSG_ID, messageId);

		mdpf.setArguments(bundle);
		return mdpf;
	}

	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		long messageId = getArguments().getLong(MSG_ID);

		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage(getResources().getString(R.string.download_attachment_progress));
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		if (runService) {
			runService = false;
			Intent intent = new Intent(getActivity(), MessageDownloadService.class);
			intent.putExtra(MessageDownloadService.MSG_ID, messageId);
			handler = new Handler();
			intent.putExtra(MessageDownloadService.RECEIVER, new DownloadReceiver(handler));
			getActivity().startService(intent);
		}

		return mProgressDialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Application.ctx.stopService(new Intent(Application.ctx, MessageDownloadService.class));
		runService = true;
		super.onCancel(dialog);
	}

	private static class DownloadReceiver extends ResultReceiver {
		public DownloadReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			if (resultCode == MessageDownloadService.UPDATE_PROGRESS) {
				int progress = resultData.getInt("progress");
				mProgressDialog.setProgress(progress);
			} else if (resultCode == MessageDownloadService.ERROR) {
				String msg = resultData.getString("error");
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, msg, Toast.LENGTH_LONG).show();
			} else if (resultCode == MessageDownloadService.ERROR_NO_CONNECTION) {
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, R.string.no_connection, Toast.LENGTH_LONG).show();
			} else if (resultCode == MessageDownloadService.ERROR_STORAGE_NOT_AVAILABLE) {
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, R.string.storage_not_available, Toast.LENGTH_LONG).show();
			} else if (resultCode == MessageDownloadService.ERROR_STORAGE_LOW_SPACE) {
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, R.string.storage_low_space, Toast.LENGTH_LONG).show();
			} else if (resultCode == MessageDownloadService.SERVICE_FINISHED) {
				mProgressDialog.dismiss();
				runService = true;
			} else if (resultCode == MessageDownloadService.ERROR_CERT) {
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, R.string.cert_error, Toast.LENGTH_LONG).show();
			}
			

		}
	}
}
