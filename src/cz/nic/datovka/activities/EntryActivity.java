package cz.nic.datovka.activities;

import org.kobjects.base64.Base64;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.EnterPinFragment;
import cz.nic.datovka.xmlElements.MyCheckBoxPreference;

public class EntryActivity extends SherlockFragmentActivity{
	private EnterPinFragment epf;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("use_pin_code", true)) {
			startActivity(new Intent(this, MainActivity.class));
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.entry_activity);

		if (epf == null)
			epf = new EnterPinFragment();
		
		if (savedInstanceState == null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.entry_activity, epf);
			ft.commit();
		}
	}
	
	public void okClicked(View v) {
		EditText et = (EditText) ((View) v.getParent()).findViewById(R.id.pin_edittext);
		String enteredPin = et.getText().toString();
		String savedPin = new String(Base64.decode(prefs.getString(MyCheckBoxPreference.PIN_PREF_ID, "NOTFOUND")));
		
		if(enteredPin.equals(savedPin)) {
			startActivity(new Intent(this, MainActivity.class));
		} else {
			Toast.makeText(this, R.string.wrong_pin_code, Toast.LENGTH_LONG).show();
			et.setBackgroundColor(getResources().getColor(R.color.wrong_pin_edit_text_color));
			et.setText("");
			et.setHint(getResources().getString(R.string.wrong_pin_code));
		}
	}
}
