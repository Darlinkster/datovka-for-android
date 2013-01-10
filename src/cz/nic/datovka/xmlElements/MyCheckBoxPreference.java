package cz.nic.datovka.xmlElements;

import org.kobjects.base64.Base64;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cz.nic.datovka.R;

public class MyCheckBoxPreference extends CheckBoxPreference {
	private AlertDialog dialog;
	private boolean checked;
	private SharedPreferences prefs;
	private Context ctx;
	
	public static final String PIN_PREF_ID = "pin_code";
	
	private static final int ENTER_NEW_PIN = 1;
	private static final int ENTER_PIN = 2;
	
	public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context;
	}

	public MyCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context;
	}

	public MyCheckBoxPreference(Context context) {
		super(context);
		prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context;
	}
	
	private void createDialog(Context ctx, int type) {
		final int typeFinal = type;
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		final View view;
		if(type == ENTER_PIN)
			view = LayoutInflater.from(ctx).inflate(R.layout.enter_pin_code_dialog, null);
		else
			view = LayoutInflater.from(ctx).inflate(R.layout.enter_new_pin_code_dialog, null);
		
		builder.setView(view);
		builder.setNegativeButton(R.string.storno, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {	}
		});
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String pin;
				String pin2;
				if(typeFinal == ENTER_PIN)
					pin = ((TextView) view.findViewById(R.id.enter_pin_dialog)).getText().toString();
				else {
					pin = ((TextView) view.findViewById(R.id.enter_new_pin_dialog)).getText().toString();
					pin2 = ((TextView) view.findViewById(R.id.enter_new_pin_dialog2)).getText().toString();
					
					if(!pin.equals(pin2)) {
						Toast.makeText(getContext(), R.string.new_pin_code_not_equal, Toast.LENGTH_LONG).show();
						return;
					}
				}
				dialogClickOk(pin);
			}
		});
		this.dialog = builder.create();
	}
	
	@Override
	protected void onClick() {
		checked = isChecked();
		if(checked) {
			// turn pin code OFF
			createDialog(ctx, ENTER_PIN);
			dialog.setTitle(R.string.enter_pin_code);
			dialog.show();
		} else {
			// turn pin code ON
			createDialog(ctx, ENTER_NEW_PIN);
			dialog.setTitle(R.string.enter_new_pin_code);
			dialog.show();
		}
	}
	
	private void dialogClickOk(String pin){
		if(checked) {
			// turn pin code OFF
			String base64SavedPin = prefs.getString(PIN_PREF_ID, "NOT_EXIST");
			// TODO osetrit chybovy stav
			String savedPin = new String(Base64.decode(base64SavedPin));
			if(savedPin.equals(pin)){
				prefs.edit().remove(PIN_PREF_ID).commit();
				super.onClick();
			} else {
				Toast.makeText(getContext(), R.string.wrong_pin_code, Toast.LENGTH_LONG).show();
			}
		} else {
			// turn pin code ON
			if(pin.length() < Base64DialogPreference.MIN_PASSWORD_LENGTH) {
				Toast.makeText(ctx, R.string.short_pin_code, Toast.LENGTH_LONG).show();
				return;
			}
			prefs.edit().putString(PIN_PREF_ID, Base64.encode(pin.getBytes())).commit();
			super.onClick();
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable bundle = super.onSaveInstanceState();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		prefs = null;
		return bundle;
	}
}
