package cz.nic.datovka.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

public class MessageBoxRefreshService extends Service {

	public int onStart(Intent intent, int flags, int startId) {
		
		return Service.START_FLAG_REDELIVERY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private class DownloaderTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			
			
			return null;
		}
		
	}
}
