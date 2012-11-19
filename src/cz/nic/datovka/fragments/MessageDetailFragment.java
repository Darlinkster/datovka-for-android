package cz.nic.datovka.fragments;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.ReceivedMessagesContentProvider;
import cz.nic.datovka.contentProviders.SentMessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class MessageDetailFragment extends SherlockFragment {
	public static final String ID = "id";
	public static final String FOLDER = "folder";
	private static final int INBOX = 0;
	private static final int IS_READ = 1;
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static MessageDetailFragment newInstance(long id, int folder) {
		MessageDetailFragment f = new MessageDetailFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);
		args.putInt(FOLDER, folder);

		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Cursor message = getMessageCursor();
		setMessageRead();

		View v = inflater.inflate(R.layout.message_detail_fragment, container, false);

		TextView annotation = (TextView) v.findViewById(R.id.message_annotation);
		TextView messageId = (TextView) v.findViewById(R.id.message_id);
		TextView deliveryDate = (TextView) v.findViewById(R.id.message_delivery_date);
		TextView acceptanceDate = (TextView) v.findViewById(R.id.message_acceptance_date);
		TextView sender = (TextView) v.findViewById(R.id.message_sender);
		TextView senderAddress = (TextView) v.findViewById(R.id.message_sender_address);
		TextView messageStatus = (TextView) v.findViewById(R.id.message_type);
		TextView messageAttachmentSize = (TextView) v.findViewById(R.id.message_attachment_size);

		int annotationColId;
		int messageIdColId;
		int messageDeliveryDateColId;
		int messageAcceptanceDateColId;
		int senderRecipientColId;
		int senderRecipientAddressColId;
		int messageStatusColId;
		int messageAttachmentSizeColId;

		int folder = getArguments().getInt(FOLDER, 0);
		if (folder == INBOX) {
			annotationColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION);
			messageIdColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID);
			messageDeliveryDateColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE);
			messageAcceptanceDateColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ACCEPTANCE_DATE);
			senderRecipientColId = message.getColumnIndex(DatabaseHelper.SENDER_NAME);
			senderRecipientAddressColId = message.getColumnIndex(DatabaseHelper.SENDER_ADDRESS);
			messageStatusColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_STATE);
			messageAttachmentSizeColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE);
		} else { // OUTBOX
			annotationColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ANNOTATION);
			messageIdColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ISDS_ID);
			messageDeliveryDateColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENT_DATE);
			messageAcceptanceDateColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ACCEPTANCE_DATE);
			senderRecipientColId = message.getColumnIndex(DatabaseHelper.RECIPIENT_NAME);
			senderRecipientAddressColId = message.getColumnIndex(DatabaseHelper.RECIPIENT_ADDRESS);
			messageStatusColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_STATE);
			messageAttachmentSizeColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE);
		}

		annotation.setText(message.getString(annotationColId));
		messageId.setText(getString(R.string.ID, message.getString(messageIdColId)));
		deliveryDate.setText(getString(R.string.delivery_date, AndroidUtils.FromXmlToHumanReadableDateWithTime(message.getString(messageDeliveryDateColId))));
		acceptanceDate.setText(getString(R.string.acceptance_date,
				AndroidUtils.FromXmlToHumanReadableDateWithTime(message.getString(messageAcceptanceDateColId))));
		sender.setText(message.getString(senderRecipientColId));
		senderAddress.setText(message.getString(senderRecipientAddressColId));
		
		int status = message.getInt(messageStatusColId);
		switch(status) {
			case 1:  messageStatus.setText(R.string.message_status_1); break;
			case 2:  messageStatus.setText(R.string.message_status_2); break;
			case 3:  messageStatus.setText(R.string.message_status_3); break;
			case 4:  messageStatus.setText(R.string.message_status_4); break;
			case 5:  messageStatus.setText(R.string.message_status_5); break;
			case 6:  messageStatus.setText(R.string.message_status_6); break;
			case 7:  messageStatus.setText(R.string.message_status_7); break;
			case 8:  messageStatus.setText(R.string.message_status_8); break;
			case 9:  messageStatus.setText(R.string.message_status_9); break;
			case 10:  messageStatus.setText(R.string.message_status_10); break;
			default:	messageStatus.setText(R.string.message_status_unknown);
						logger.log(Level.WARNING, "Unknown message status: " + status);
						
		}
		messageAttachmentSize.setText(getString(R.string.size_of_attachments, message.getInt(messageAttachmentSizeColId)));

		message.close();
		return v;
	}

	private Cursor getMessageCursor() {
		long id = getArguments().getLong(ID, 0);
		int folder = getArguments().getInt(FOLDER, 0);

		Uri singleUri;
		String[] projection;
		if (folder == INBOX) {
			singleUri = ContentUris.withAppendedId(ReceivedMessagesContentProvider.CONTENT_URI, id);
			projection = DatabaseHelper.received_message_columns;
		} else { // if(folder == OUTBOX)
			singleUri = ContentUris.withAppendedId(SentMessagesContentProvider.CONTENT_URI, id);
			projection = DatabaseHelper.sent_message_columns;
		}
		Cursor cursor = getActivity().getContentResolver().query(singleUri, projection, null, null, null);

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
			singleUri = ContentUris.withAppendedId(ReceivedMessagesContentProvider.CONTENT_URI, id);
			value.put(DatabaseHelper.RECEIVED_MESSAGE_IS_READ, IS_READ);
		} else { // if(folder == OUTBOX)
			singleUri = ContentUris.withAppendedId(SentMessagesContentProvider.CONTENT_URI, id);
			value.put(DatabaseHelper.SENT_MESSAGE_IS_READ, IS_READ);
		}

		getActivity().getContentResolver().update(singleUri, value, null, null);
	}

}
