package cz.nic.datovka.activities;

import cz.nic.datovka.R;
import cz.nic.datovka.R.id;
import cz.nic.datovka.R.layout;
import cz.nic.datovka.fragments.MessageDetailFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MessageDetailActivity  extends FragmentActivity {
	
	public static final String ID = "id";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_detail_activity);
		
		Intent i = getIntent();
		int id = i.getIntExtra(ID, 0);
		
		MessageDetailFragment mdf = MessageDetailFragment.newInstance(id);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.message_detail_activity, mdf);
		ft.commit();
	}
}
