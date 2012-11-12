package cz.nic.datovka.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.services.MessageDownloadService;

public class MessageDownloadProgressFragment extends DialogFragment {
	private static ProgressDialog mProgressDialog;
	
	private static final String MSG_ID = "msgid";
	private static final String FOLDER = "folder";
	
	public static MessageDownloadProgressFragment newInstance(long messageId, int folder){
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
		
		Intent intent = new Intent(getActivity(), MessageDownloadService.class);
		intent.putExtra(MessageDownloadService.FOLDER, folder);
		intent.putExtra(MessageDownloadService.MSG_ID, messageId);
		intent.putExtra(MessageDownloadService.RECEIVER, new DownloadReceiver(new Handler()));
		getActivity().startService(intent);
		
		return mProgressDialog;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		getActivity().stopService(new Intent(getActivity(), MessageDownloadService.class));
	}
	
	private class DownloadReceiver extends ResultReceiver {
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
				}
			}
			else if(resultCode == MessageDownloadService.ERROR){
				String msg = resultData.getString("error");
				mProgressDialog.dismiss();
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			}
		}
	}
}
