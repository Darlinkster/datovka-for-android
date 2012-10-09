package cz.nic.datovka;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.connector.Connector;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
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

		Connector connector = new Connector("co55on", "Fx2MAt3u8wDRL5", this);
		try {
			connector.connectToTesting();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		

		LinearLayout messageView = (LinearLayout) findViewById(R.id.message_view);
		List<MessageEnvelope> messageList = connector.getMessageList();
		Iterator<MessageEnvelope> messageIterator = messageList.iterator();

		int i = 0;
		while (messageIterator.hasNext()) {
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView message = (TextView) inflater.inflate(
					R.layout.message_item, messageView, false);

			message.setTag(i++);
			message.setText(messageIterator.next().getAnnotation());
			messageView.addView(message);
		}

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
