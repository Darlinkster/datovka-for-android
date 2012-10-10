package cz.nic.datovka;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import cz.nic.datovka.connector.Connector;

public class MainActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		try {
			Connector.connect("co55on", "Fx2MAt3u8wDRL5", Connector.TESTING,
					this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button folder_button = (Button) findViewById(R.id.folder_button);
		Button account_button = (Button) findViewById(R.id.account_button);
		
		folder_button.setText("Inbox");
		account_button.setText("martin.strbacka@nic.cz");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void itemClicked(View view) {
		view.setBackgroundColor(getResources().getColor(R.color.dimwhite));
		int id = view.getId();
		Intent i = new Intent(this, MessageDetailActivity.class);
		i.putExtra(MessageDetailActivity.ID, id);
		startActivity(i);
	}
}
