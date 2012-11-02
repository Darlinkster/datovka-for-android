package cz.nic.datovka.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class MessageDetailFragment extends Fragment {
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	private static final int INBOX = 0;
	private static final int IS_READ = 1;

	public static MessageDetailFragment newInstance(long id, int folder) {
		MessageDetailFragment f = new MessageDetailFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);
		args.putInt(FOLDER, folder);

		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Cursor message = getMessageCursor();
		setMessageRead();

		View v = inflater.inflate(R.layout.message_detail_fragment, container,
				false);

		TextView annotation = (TextView) v
				.findViewById(R.id.message_annotation);
		TextView messageId = (TextView) v.findViewById(R.id.message_id);
		TextView date = (TextView) v.findViewById(R.id.message_date);
		TextView sender = (TextView) v.findViewById(R.id.message_sender);
		TextView senderAddress = (TextView) v
				.findViewById(R.id.message_sender_address);
		TextView messageType = (TextView) v.findViewById(R.id.message_type);
		TextView messageAttachmentSize = (TextView) v
				.findViewById(R.id.message_attachment_size);

		int annotationColId;
		int messageIdColId;
		int messageDateColId;
		int senderRecipientColId;
		int senderRecipientAddressColId;
		int messageTypeColId;
		int messageAttachmentSizeColId;

		int folder = getArguments().getInt(FOLDER, 0);
		if (folder == INBOX) {
			annotationColId = message
					.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION);
			messageIdColId = message
					.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID);
			messageDateColId = message
					.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE);
			senderRecipientColId = message
					.getColumnIndex(DatabaseHelper.SENDER_NAME);
			senderRecipientAddressColId = message
					.getColumnIndex(DatabaseHelper.SENDER_ADDRESS);
			messageTypeColId = message
					.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_TYPE);
			messageAttachmentSizeColId = message
					.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE);
		} else { // OUTBOX
			annotationColId = message
					.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ANNOTATION);
			messageIdColId = message
					.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ISDS_ID);
			messageDateColId = message
					.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENT_DATE);
			senderRecipientColId = message
					.getColumnIndex(DatabaseHelper.RECIPIENT_NAME);
			senderRecipientAddressColId = message
					.getColumnIndex(DatabaseHelper.RECIPIENT_ADDRESS);
			messageTypeColId = message
					.getColumnIndex(DatabaseHelper.SENT_MESSAGE_TYPE);
			messageAttachmentSizeColId = message
					.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE);
		}

		annotation.setText(message.getString(annotationColId));
		messageId.setText(message.getString(messageIdColId));
		date.setText(AndroidUtils.FromXmlToHumanReadableDateWithTime(message
				.getString(messageDateColId)));
		sender.setText(message.getString(senderRecipientColId));
		senderAddress.setText(message.getString(senderRecipientAddressColId));
		messageType.setText(message.getString(messageTypeColId));
		messageAttachmentSize.setText("Velikost prilohy: "
				+ message.getInt(messageAttachmentSizeColId) + "kB");
		
		return v;
	}

	private Cursor getMessageCursor() {
		long id = getArguments().getLong(ID, 0);
		int folder = getArguments().getInt(FOLDER, 0);

		Uri singleUri;
		String[] projection;
		if (folder == INBOX) {
			singleUri = ContentUris.withAppendedId(
					ReceivedMessagesContentProvider.CONTENT_URI, id);
			projection = DatabaseHelper.received_message_columns;
		} else { // if(folder == OUTBOX)
			singleUri = ContentUris.withAppendedId(
					SentMessagesContentProvider.CONTENT_URI, id);
			projection = DatabaseHelper.sent_message_columns;
		}
		Cursor cursor = getActivity().getContentResolver().query(singleUri,
				projection, null, null, null);

		if (cursor.moveToFirst()) {
			return cursor;
		}
		// TODO
		throw new RuntimeException("nic nenalezeno");
	}

	private void setMessageRead() {
		long id = getArguments().getLong(ID, 0);
		int folder = getArguments().getInt(FOLDER, 0);

		Uri singleUri;
		ContentValues value = new ContentValues();
		if (folder == INBOX) {
			singleUri = ContentUris.withAppendedId(
					ReceivedMessagesContentProvider.CONTENT_URI, id);
			value.put(DatabaseHelper.RECEIVED_MESSAGE_IS_READ, IS_READ);
		} else { // if(folder == OUTBOX)
			singleUri = ContentUris.withAppendedId(
					SentMessagesContentProvider.CONTENT_URI, id);
			value.put(DatabaseHelper.SENT_MESSAGE_IS_READ, IS_READ);
		}

		getActivity().getContentResolver().update(singleUri, value, null, null);
	}

}
