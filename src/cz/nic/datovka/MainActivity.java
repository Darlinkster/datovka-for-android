package cz.nic.datovka;

import cz.nic.datovka.connector.Connector;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

		Spinner folder_spinner = (Spinner) findViewById(R.id.folder_spinner);
		Spinner account_spinner = (Spinner) findViewById(R.id.account_spinner);

		ArrayAdapter<CharSequence> adapter_accounts = ArrayAdapter
				.createFromResource(this, R.array.accounts,
						R.layout.top_spinner_layout);
		ArrayAdapter<CharSequence> adapter_folders = ArrayAdapter
				.createFromResource(this, R.array.folders,
						R.layout.top_spinner_layout);

		adapter_accounts
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter_folders
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		account_spinner.setAdapter(adapter_accounts);
		folder_spinner.setAdapter(adapter_folders);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void messageClicked(View textview) {
		TextView tv = (TextView) textview;
		tv.setBackgroundColor(getResources().getColor(R.color.dimwhite));
		Toast.makeText(this, tv.getText(), Toast.LENGTH_SHORT).show();
	}
}
