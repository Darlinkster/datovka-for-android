package cz.nic.datovka.activities;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.nic.datovka.R;
import cz.nic.datovka.fragments.MessageAttachmentsFragment;
import cz.nic.datovka.fragments.MessageDetailFragment;
import cz.nic.datovka.fragments.MessageDownloadProgressFragment;
import cz.nic.datovka.services.MessageDownloadService;
import cz.nic.datovka.services.MessageStatusRefresher;

public class MessageDetailActivity  extends SherlockFragmentActivity {
	
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	
	private long messageId;
	private int folder;
	
	private FragmentManager fm;
	private MessageDetailFragment mdf;
	private MessageAttachmentsFragment maf;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_detail_activity);
		
		Intent i = getIntent();
		this.messageId = i.getLongExtra(ID, 0);
		this.folder = i.getIntExtra(FOLDER,0);
		
		mdf = MessageDetailFragment.newInstance(this.messageId, this.folder);
		maf = MessageAttachmentsFragment.newInstance(this.messageId, this.folder);
		
		fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.message_detail_activity, mdf);
		ft.replace(R.id.message_attachment_activity, maf);
		ft.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.message_detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.download_attachment_menu_btn) {
			MessageDownloadProgressFragment mdpf = MessageDownloadProgressFragment.newInstance(this.messageId, this.folder);
			mdpf.show(fm, null);
			return true;
		}
		else if (item.getItemId() == R.id.refresh_message_menu_btn) {
			Intent param = new Intent();
			param.putExtra(MessageStatusRefresher.MSG_ID, this.messageId);
			param.putExtra(MessageStatusRefresher.FOLDER, this.folder);
			Messenger messenger = new Messenger(handler);
			param.putExtra(MessageDownloadService.RECEIVER, messenger);
			new MessageStatusRefresher(param).start();
		}
		return false;
	}
			
	public void attachmentClicked(View view){
		
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		
		File file = new File(Application.externalStoragePath + view.getTag(R.id.attachment_path_tag_id).toString());
		intent.setDataAndType(Uri.fromFile(file), (String) view.getTag());
		try{
			startActivity(intent);
		}catch(RuntimeException e){
			Toast.makeText(this, R.string.no_default_application, Toast.LENGTH_LONG).show();
		}
	}
	
	// Handler for handling messages from MessageBoxRefresh service
	private static Handler handler = new Handler() {
		public void handleMessage(Message message) {
			if (message.arg1 == MessageStatusRefresher.ERROR) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			} else if (message.arg1 == MessageStatusRefresher.ERROR_BAD_LOGIN) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			} else if (message.arg1 == MessageStatusRefresher.STATUS_UPDATED) {
				if(message.arg2 > 0)
					Toast.makeText(Application.ctx, R.string.status_changed, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(Application.ctx, R.string.status_not_changed, Toast.LENGTH_LONG).show();
			}
		}
	};
}
