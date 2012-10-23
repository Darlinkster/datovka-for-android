package cz.nic.datovka.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cz.nic.datovka.R;
import cz.nic.datovka.R.color;
import cz.nic.datovka.R.id;
import cz.nic.datovka.R.layout;
import cz.nic.datovka.R.menu;
import cz.nic.datovka.connector.Connector;
import cz.nic.datovka.fragments.ReceivedMessageListFragment;

public class MainActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button folder_button = (Button) findViewById(R.id.folder_button);
		Button account_button = (Button) findViewById(R.id.account_button);
		
		folder_button.setText("Inbox");
		account_button.setText("martin.strbacka@nic.cz");

		ReceivedMessageListFragment mlf = new ReceivedMessageListFragment();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.main_linearlayout, mlf);
		ft.commit();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_accounts:
				startActivity(new Intent(this, AccountActivity.class));
				return true;
			case R.id.menu_settings:
				Toast.makeText(this, "nastaveni", Toast.LENGTH_SHORT).show();
				return true;
		}
		
		return true;
	}

	public void itemClicked(View view) {
		view.setBackgroundColor(getResources().getColor(R.color.gray));
		TextView annotation = (TextView) view.findViewById(R.id.message_item_annotation);
		TextView sender = (TextView) view.findViewById(R.id.message_item_sender);
		
		annotation.setTypeface(null, Typeface.NORMAL);
		sender.setTypeface(null, Typeface.NORMAL);
		
		int id = view.getId();
		Intent i = new Intent(this, MessageDetailActivity.class);
		i.putExtra(MessageDetailActivity.ID, id);
		startActivity(i);
	}
}
