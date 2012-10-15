package cz.nic.datovka.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.SQliteConnector;

public class AccountFragment extends ListFragment {
	private SQliteConnector sqlConnector;

	public static AccountFragment newInstance() {
		AccountFragment f = new AccountFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.account_fragment, container, false);

		return v;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		updateList();
	}

	public void updateList() {
		Context context = getActivity();
		sqlConnector = new SQliteConnector(context);

		String[] from = { sqlConnector.ACCOUNT_LOGIN };
		int[] to = { R.id.account_item }; 
		
		
		ListAdapter adapter = new SimpleCursorAdapter(context, 
				R.layout.account_fragment, sqlConnector.getAccounts(),
				from, to, 0);
		
		setListAdapter(adapter);
		sqlConnector.close();
		
	}
	
	@Override
	public void onDestroyView(){
		super.onDestroyView();
		sqlConnector.close();
	}
}
