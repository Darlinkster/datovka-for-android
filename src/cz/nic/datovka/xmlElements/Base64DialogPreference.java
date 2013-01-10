package cz.nic.datovka.xmlElements;

import org.kobjects.base64.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cz.nic.datovka.R;

public class Base64DialogPreference extends DialogPreference {
	private EditText oldPasswd, newPasswd2, newPasswd;
	private SharedPreferences prefs;
	private Context ctx;
	
	public static final int MIN_PASSWORD_LENGTH = 3;

	public Base64DialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPersistent(false);
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context;
	}

	public Base64DialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		oldPasswd = (EditText) view.findViewById(R.id.pinChangeOldPin);
		newPasswd = (EditText) view.findViewById(R.id.pinChangeNewPin);
		newPasswd2 = (EditText) view.findViewById(R.id.pinChangeNewPin2);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult) {
			String oldPasswdText = oldPasswd.getText().toString();
			String newPasswdText = newPasswd.getText().toString();
			String newPasswd2Text = newPasswd2.getText().toString();
			String savedPasswdBase64 = prefs.getString(MyCheckBoxPreference.PIN_PREF_ID, "NOT_EXIST");
			String savedPasswd = new String(Base64.decode(savedPasswdBase64));
			
			if(!savedPasswd.equals(oldPasswdText)){
				Toast.makeText(ctx, R.string.wrong_pin_code, Toast.LENGTH_LONG).show();
				return;
			}
			
			if(newPasswdText.length() < MIN_PASSWORD_LENGTH) {
				Toast.makeText(ctx, R.string.short_pin_code, Toast.LENGTH_LONG).show();
				return;
			}
			
			if(!newPasswdText.equals(newPasswd2Text)) {
				Toast.makeText(ctx, R.string.new_pin_code_not_equal, Toast.LENGTH_LONG).show();
				return;
			}
			
			
			prefs.edit().putString(MyCheckBoxPreference.PIN_PREF_ID, Base64.encode(newPasswdText.getBytes())).commit();
			
		}
	}
}
