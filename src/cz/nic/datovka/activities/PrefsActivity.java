package cz.nic.datovka.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import cz.nic.datovka.R;

public class PrefsActivity extends SherlockPreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preference);
	//	}
		
	}
/*	
	@SuppressLint("NewApi")
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}
	*/
}
