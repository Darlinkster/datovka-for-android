/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2012  CZ NIC z.s.p.o. <podpora at nic dot cz>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package cz.nic.datovka.activities;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
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
	private static MenuItem refreshButtonItem;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_detail_activity);
		
		Intent i = getIntent();
		this.messageId = i.getLongExtra(ID, 0);
		this.folder = i.getIntExtra(FOLDER,0);
		
		mdf = MessageDetailFragment.newInstance(this.messageId, this.folder);
		maf = MessageAttachmentsFragment.newInstance(this.messageId);
		
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
			mdpf.show(fm, MessageDownloadProgressFragment.DIALOG_ID);
			return true;
		}
		else if (item.getItemId() == R.id.refresh_message_menu_btn) {
			LayoutInflater inflater = (LayoutInflater) Application.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView refreshButtonView = (ImageView) inflater.inflate(R.layout.refresh_button_view, null);
			Animation rotation = AnimationUtils.loadAnimation(Application.ctx, R.anim.anim_rotate);
			refreshButtonView.startAnimation(rotation);
			item.setActionView(refreshButtonView);
			refreshButtonItem = item;
			
			Intent param = new Intent();
			param.putExtra(MessageStatusRefresher.MSG_ID, this.messageId);
			Messenger messenger = new Messenger(handler);
			param.putExtra(MessageDownloadService.RECEIVER, messenger);
			new MessageStatusRefresher(param).start();
		}
		return false;
	}
	
	private static final void removeAnimationFromRefreshButton(){
		if(refreshButtonItem != null) {
			refreshButtonItem.getActionView().clearAnimation();
			refreshButtonItem.setActionView(null);
		}
	}
			
	public void attachmentClicked(View view){
		
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		
		File file = new File(Application.externalStoragePath + view.getTag(R.id.attachment_path_tag_id).toString());
		String mimeType = (String) view.getTag();
		intent.setDataAndType(Uri.fromFile(file), mimeType);
		try{
			startActivity(intent);
		}catch(RuntimeException e){
			// Maybe wrong mime type in XML file, check the file suffix
			openFileBySuffix(file);
		}
	}
	
	private void openFileBySuffix(File file) {
		String mimeType = null;
		String filename = file.getName();
		int lastDotOffset = filename.lastIndexOf('.');
		if(lastDotOffset == -1){
			Toast.makeText(this, getString(R.string.no_default_application, "no suffix"), Toast.LENGTH_LONG).show();
			return;
		}
		if((lastDotOffset + 1) >= filename.length()){
			Toast.makeText(this, getString(R.string.no_default_application, "dot at the end"), Toast.LENGTH_LONG).show();
			return;
		}
		String extension = filename.substring(lastDotOffset + 1).toLowerCase(Locale.getDefault());
		if (!extension.equalsIgnoreCase("")) {
			// check the mime type by suffix
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			mimeType = mime.getMimeTypeFromExtension(extension);
		} else {
			// the file has bad suffix
			Toast.makeText(this, getString(R.string.no_default_application, "null"), Toast.LENGTH_LONG).show();
			return;
		}

		if (mimeType != null) {
			// we have got mime type from suffix, open the file with it
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), mimeType);
			try{
				startActivity(intent);
			}catch(RuntimeException e){
				// the mime type is probably right, but we don't have any app to open the file
				Toast.makeText(this, getString(R.string.no_default_application, mimeType), Toast.LENGTH_LONG).show();
				return;
			}
			
		} else {
			// we cannot get mime type from the file suffix 
			Toast.makeText(this, getString(R.string.no_default_application, new String("suffix: " + extension)), Toast.LENGTH_LONG).show();
			return;
		}

	}
	
	// Handler for handling messages from MessageBoxRefresh service
	private static Handler handler = new Handler() {
		public void handleMessage(Message message) {
			removeAnimationFromRefreshButton();
			if (message.arg1 == MessageStatusRefresher.ERROR) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			} else if (message.arg1 == MessageStatusRefresher.ERROR_BAD_LOGIN) {
				Toast.makeText(Application.ctx, (String) message.obj, Toast.LENGTH_LONG).show();
			} else if (message.arg1 == MessageStatusRefresher.STATUS_UPDATED) {
				if(message.arg2 > 0)
					Toast.makeText(Application.ctx, R.string.status_changed, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(Application.ctx, R.string.status_not_changed, Toast.LENGTH_LONG).show();
			} else if (message.arg1 == MessageStatusRefresher.ERROR_CERT) {
				Toast.makeText(Application.ctx, R.string.cert_error, Toast.LENGTH_LONG).show();
			}
		}
	};
}
