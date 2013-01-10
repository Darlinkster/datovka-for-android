package cz.nic.datovka.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import cz.nic.datovka.R;

public class PrefsActivity extends SherlockPreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
	}
}
