package cz.nic.datovka.activities;

import android.content.Context;
import android.os.Environment;

public class Application {
	public static Context ctx;
	public static final String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
}
