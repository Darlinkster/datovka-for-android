package cz.nic.datovka.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kobjects.base64.Base64;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.bugsense.trace.BugSenseHandler;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.EnterPinFragment;
import cz.nic.datovka.xmlElements.MyCheckBoxPreference;

public class EntryActivity extends SherlockFragmentActivity{
	private EnterPinFragment epf;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Properties prop = new Properties();
		try {
			InputStream is = this.getClass().getResourceAsStream("prop.cfg");
			if(is == null)
				throw new IOException();
			prop.load(is);
			String bugSenseId = prop.getProperty("BugSenseID");
			BugSenseHandler.initAndStartSession(getApplicationContext(), bugSenseId);
			Log.d(this.getPackageName(), "BugSense plugin loaded and initialized.");
		} catch (IOException e) {
			Log.w(this.getPackageName(), "Cannot find prop.cfg, BugSense not initialized.");
		}
		
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
			Toast toast = Toast.makeText(this, R.string.wrong_pin_code, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 200);
			toast.show();
			
			et.setBackgroundColor(getResources().getColor(R.color.wrong_pin_edit_text_color));
			et.setText("");
		}
	}
}
