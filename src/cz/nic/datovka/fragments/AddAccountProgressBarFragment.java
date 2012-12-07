package cz.nic.datovka.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.Application;
import cz.nic.datovka.services.AddAccountService;

public class AddAccountProgressBarFragment extends SherlockDialogFragment {
	public static final String PASSWORD = "pass";
	public static final String LOGIN = "login";
	public static final String TEST_ENV = "testenv";
	
	private static ProgressDialog pd;
	private static String login;
	private static String password;
	private static boolean testEnv;
	private static boolean run = true;
	private static FragmentManager fragmentManager;
	
	public static AddAccountProgressBarFragment newInstance(String login, String password, boolean testEnv) {
		AddAccountProgressBarFragment mdpf = new AddAccountProgressBarFragment();
		Bundle bundle = new Bundle();
		bundle.putString(LOGIN, login);
		bundle.putString(PASSWORD, password);
		bundle.putBoolean(TEST_ENV, testEnv);
		
		mdpf.setArguments(bundle);
		return mdpf;
	}
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pd = new ProgressDialog(getActivity());
		
		Bundle args = getArguments();
		login = args.getString(LOGIN);
		password = args.getString(PASSWORD);
		testEnv = args.getBoolean(TEST_ENV);
		
		if (run) {
			run = false;
			Messenger messenger = new Messenger(handler);
			Intent intent = new Intent(getActivity(), AddAccountService.class);
			intent.putExtra(AddAccountService.HANDLER, messenger);
			intent.putExtra(AddAccountService.LOGIN, login);
			intent.putExtra(AddAccountService.PASSWORD, password);
			intent.putExtra(AddAccountService.TESTENV, testEnv);

			getActivity().startService(intent);
		}
	}
	
	@Override
	public void onResume () {
		super.onResume();
		fragmentManager = getFragmentManager();
	}
	
	@Override
	public void onPause () {
		super.onPause();
		fragmentManager = null;
		pd.dismiss();
		pd = null;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle SavedInstanceState) {
		pd.setIndeterminate(true);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(new String(getResources().getString(R.string.account_create_progress)));
		
		return pd;
	}

	public static Handler handler = new Handler() {

		public void handleMessage(Message message) {
			if (pd != null) 
				pd.dismiss();

			if (message.arg1 == AddAccountService.RESULT_OK) {
				Toast.makeText(Application.ctx, R.string.account_created, Toast.LENGTH_SHORT).show();
				return;
			} else if (message.arg1 == AddAccountService.RESULT_EXISTS) {
				Toast.makeText(Application.ctx, R.string.account_exists, Toast.LENGTH_SHORT).show();
			} else if (message.arg1 == AddAccountService.RESULT_ERR) {
				Toast.makeText(Application.ctx, R.string.account_create_error, Toast.LENGTH_SHORT).show();
			} else if (message.arg1 == AddAccountService.RESULT_BAD_LOGIN) {
				Toast.makeText(Application.ctx, R.string.account_create_bad_login, Toast.LENGTH_SHORT).show();
			} else if (message.arg1 == AddAccountService.RESULT_DS_ERR) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_SHORT).show();
			} else if (message.arg1 == AddAccountService.RESULT_NO_CONNECTION) {
				Toast.makeText(Application.ctx, R.string.no_connection, Toast.LENGTH_SHORT).show();
			}

			run = true;
			if (fragmentManager != null) {
				AddAccountFragment aaf = AddAccountFragment.newInstance(login, password, testEnv);
				aaf.show(fragmentManager, null);
			}
		}
	};
}
