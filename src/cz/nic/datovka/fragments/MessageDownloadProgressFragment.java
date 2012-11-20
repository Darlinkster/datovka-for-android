package cz.nic.datovka.fragments;

import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static ProgressDialog mProgressDialog;
	private static Handler handler;

	private static final String MSG_ID = "msgid";
	private static final String FOLDER = "folder";
	private static boolean runService = true;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public static MessageDownloadProgressFragment newInstance(long messageId, int folder) {
		MessageDownloadProgressFragment mdpf = new MessageDownloadProgressFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(FOLDER, folder);
		bundle.putLong(MSG_ID, messageId);

		mdpf.setArguments(bundle);
		return mdpf;
	}

	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		long messageId = getArguments().getLong(MSG_ID);
		int folder = getArguments().getInt(FOLDER);

		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage(getResources().getString(R.string.download_attachment_progress));
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		if (runService) {
			runService = false;
			logger.log(Level.INFO, "Downloading service started");
			Intent intent = new Intent(getActivity(), MessageDownloadService.class);
			intent.putExtra(MessageDownloadService.FOLDER, folder);
			intent.putExtra(MessageDownloadService.MSG_ID, messageId);
			handler = new Handler();
			intent.putExtra(MessageDownloadService.RECEIVER, new DownloadReceiver(handler));
			getActivity().startService(intent);
		}

		return mProgressDialog;
	}
		
	@Override
	public void onCancel(DialogInterface dialog) {
		getActivity().stopService(new Intent(getActivity(), MessageDownloadService.class));
		runService = true;
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
				if (progress == 100) {
					mProgressDialog.dismiss();
					runService = true;
				}
			} else if (resultCode == MessageDownloadService.ERROR) {
				String msg = resultData.getString("error");
				mProgressDialog.dismiss();
				runService = true;
				Toast.makeText(Application.ctx, msg, Toast.LENGTH_LONG).show();
			}
		}
	}
}
