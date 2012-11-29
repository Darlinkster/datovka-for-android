package cz.nic.datovka.fragments;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
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
	private static final int NOT_CHANGED = 0;
	private static final int IS_READ = 1;
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Updater updater;
	private Uri singleUri;
	
	public static MessageDetailFragment newInstance(long id, int folder) {
		MessageDetailFragment f = new MessageDetailFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);
		args.putInt(FOLDER, folder);

		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(updater);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getActivity().getContentResolver().registerContentObserver(singleUri, false, updater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View v = inflater.inflate(R.layout.message_detail_fragment, container, false);
		View customActionBarView = inflater.inflate(R.layout.message_detail_actionbar, null);
		updater = new Updater(new Handler());
		setMessageRead();
		
		return fillFragment(v, customActionBarView);

			}
	
	private View fillFragment(View v, View customActionBarView) {
		Cursor message = getMessageCursor();

//		TextView annotation = (TextView) v.findViewById(R.id.message_annotation);
//		TextView messageId = (TextView) v.findViewById(R.id.message_id);
		TextView deliveryDateTV = (TextView) v.findViewById(R.id.message_delivery_date);
		TextView acceptanceDateTV = (TextView) v.findViewById(R.id.message_acceptance_date);
		TextView senderTV = (TextView) v.findViewById(R.id.message_sender);
		TextView senderAddressTV = (TextView) v.findViewById(R.id.message_sender_address);
		TextView messageStatusTV = (TextView) v.findViewById(R.id.message_type);
		TextView messageAttachmentSizeTV = (TextView) v.findViewById(R.id.message_attachment_size);
		TextView legalTitleTV = (TextView) v.findViewById(R.id.message_detail_legal_title);
		TextView personalDeliveryTV = (TextView) v.findViewById(R.id.message_detail_personal_delivery);
		TextView substDeliveryTV = (TextView) v.findViewById(R.id.message_detail_subst_delivery);
		TextView toHandsTV = (TextView) v.findViewById(R.id.message_detail_to_hands);
		TextView recipientRefNumTV = (TextView) v.findViewById(R.id.message_recipient_ref_num);
		TextView recipientIdentTV = (TextView) v.findViewById(R.id.message_recipient_ident);
		TextView senderRefNumTV = (TextView) v.findViewById(R.id.message_sender_ref_num);
		TextView senderIdentTV = (TextView) v.findViewById(R.id.message_sender_ident);
		
		TextView senderDetailTV = (TextView) v.findViewById(R.id.sender_details);
		TextView recipientDetailTV = (TextView) v.findViewById(R.id.recipient_details);
		TextView senderRecpTV = (TextView) v.findViewById(R.id.message_sender_recipient);

		int annotationColId;
		int messageIdColId;
		int messageDeliveryDateColId;
		int messageAcceptanceDateColId;
		int senderRecipientColId;
		int senderRecipientAddressColId;
		int messageStatusColId;
		int messageAttachmentSizeColId;
		int legalTitleLawColId;
		int legalTitleParColId;
		int legalTitlePointColId;
		int legalTitleSectColId;
		int legalTitleYearColId;
		int personalDeliveryColId;
		int substDeliveryColId;
		int toHandsColId;
		int recipientRefNumColId;
		int recipientIdentColId;
		int senderRefNumColId;
		int senderIdentColId;

		int folder = getArguments().getInt(FOLDER, 0);
		if (folder == INBOX) {
			senderRecpTV.setText(getString(R.string.sender));
			
			annotationColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ANNOTATION);
			messageIdColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ISDS_ID);
			messageDeliveryDateColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECEIVED_DATE);
			messageAcceptanceDateColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ACCEPTANCE_DATE);
			senderRecipientColId = message.getColumnIndex(DatabaseHelper.SENDER_NAME);
			senderRecipientAddressColId = message.getColumnIndex(DatabaseHelper.SENDER_ADDRESS);
			messageStatusColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_STATE);
			messageAttachmentSizeColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ATTACHMENT_SIZE);
			legalTitleLawColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_LAW);
			legalTitleParColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_PAR);
			legalTitlePointColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_POINT);
			legalTitleSectColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_SECT);
			legalTitleYearColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_LEGALTITLE_YEAR);
			personalDeliveryColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_PERSONAL_DELIVERY);
			substDeliveryColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_ALLOW_SUBST_DELIVERY);
			toHandsColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_TO_HANDS);
			recipientRefNumColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECIPIENT_REF_NUMBER);
			recipientIdentColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_RECIPIENT_IDENT);
			senderRefNumColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_SENDER_REF_NUMBER);
			senderIdentColId = message.getColumnIndex(DatabaseHelper.RECEIVED_MESSAGE_SENDER_IDENT); 
		} else { // OUTBOX
			senderRecpTV.setText(getString(R.string.recipient));
			
			annotationColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ANNOTATION);
			messageIdColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ISDS_ID);
			messageDeliveryDateColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENT_DATE);
			messageAcceptanceDateColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ACCEPTANCE_DATE);
			senderRecipientColId = message.getColumnIndex(DatabaseHelper.RECIPIENT_NAME);
			senderRecipientAddressColId = message.getColumnIndex(DatabaseHelper.RECIPIENT_ADDRESS);
			messageStatusColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_STATE);
			messageAttachmentSizeColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ATTACHMENT_SIZE);
			legalTitleLawColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_LAW);
			legalTitleParColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_PAR);
			legalTitlePointColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_POINT);
			legalTitleSectColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_SECT);
			legalTitleYearColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_LEGALTITLE_YEAR);
			personalDeliveryColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_PERSONAL_DELIVERY);
			substDeliveryColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_ALLOW_SUBST_DELIVERY);
			toHandsColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_TO_HANDS);
			recipientRefNumColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_RECIPIENT_REF_NUMBER);
			recipientIdentColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_RECIPIENT_IDENT);
			senderRefNumColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENDER_REF_NUMBER);
			senderIdentColId = message.getColumnIndex(DatabaseHelper.SENT_MESSAGE_SENDER_IDENT);
		}

		if(customActionBarView != null){
			ActionBar ab = getSherlockActivity().getSupportActionBar();
		    TextView annotationAB = (TextView) customActionBarView.findViewById(R.id.actionbar_annotation);
		    TextView idAB = (TextView) customActionBarView.findViewById(R.id.actionbar_id);
		    
		    annotationAB.setText(message.getString(annotationColId));
		    idAB.setText(getString(R.string.ID, message.getString(messageIdColId)));
		    ab.setDisplayShowTitleEnabled(false);
		    ab.setCustomView(customActionBarView);
		    ab.setDisplayShowCustomEnabled(true);
		}
		
		//getSherlockActivity().getSupportActionBar().setTitle(message.getString(annotationColId) +" "+getString(R.string.ID, message.getString(messageIdColId)));
		//annotation.setText(message.getString(annotationColId));
		//messageId.setText(getString(R.string.ID, message.getString(messageIdColId)));
	    	    
		deliveryDateTV.setText(getString(R.string.delivery_date, AndroidUtils.FromXmlToHumanReadableDateWithTime(message.getString(messageDeliveryDateColId))));
		
		String acceptanceDate = message.getString(messageAcceptanceDateColId);
		if (acceptanceDate != null && !acceptanceDate.equals(""))
			acceptanceDateTV.setText(getString(R.string.acceptance_date,AndroidUtils.FromXmlToHumanReadableDateWithTime(acceptanceDate)));
		else
			acceptanceDateTV.setText(getString(R.string.acceptance_date, getString(R.string.message_not_accepted_yet)));
		
		
		senderTV.setText(message.getString(senderRecipientColId));
		senderAddressTV.setText(message.getString(senderRecipientAddressColId));
		
		int status = message.getInt(messageStatusColId);
		switch(status) {
			case 1:  messageStatusTV.setText(R.string.message_status_1); break;
			case 2:  messageStatusTV.setText(R.string.message_status_2); break;
			case 3:  messageStatusTV.setText(R.string.message_status_3); break;
			case 4:  messageStatusTV.setText(R.string.message_status_4); break;
			case 5:  messageStatusTV.setText(R.string.message_status_5); break;
			case 6:  messageStatusTV.setText(R.string.message_status_6); break;
			case 7:  messageStatusTV.setText(R.string.message_status_7); break;
			case 8:  messageStatusTV.setText(R.string.message_status_8); break;
			case 9:  messageStatusTV.setText(R.string.message_status_9); break;
			case 10:  messageStatusTV.setText(R.string.message_status_10); break;
			default:	messageStatusTV.setText(R.string.message_status_unknown);
						logger.log(Level.WARNING, "Unknown message status: " + status);
						
		}
		messageAttachmentSizeTV.setText(getString(R.string.size_of_attachments, message.getInt(messageAttachmentSizeColId)));
		personalDeliveryTV.setText(getString(R.string.personal_delivery, translateIntAnswerToString(message.getString(personalDeliveryColId))));
		substDeliveryTV.setText(getString(R.string.subst_delivery, translateIntAnswerToString(message.getString(substDeliveryColId))));
		
		String toHands = message.getString(toHandsColId);
		if(toHands.equalsIgnoreCase(""))
			hideTextView(toHandsTV);
		else
			toHandsTV.setText(getString(R.string.to_hands, toHands));
		
		String legalTitle = createLegalTitle(message.getString(legalTitleLawColId), message.getString(legalTitleParColId), 
															message.getString(legalTitlePointColId), message.getString(legalTitleSectColId), 
															message.getString(legalTitleYearColId));
		if(legalTitle.equalsIgnoreCase(""))
			hideTextView(legalTitleTV);
		else
			legalTitleTV.setText(getString(R.string.legal_title, legalTitle));
		
		String recipientIdent = message.getString(recipientIdentColId);
		String recipientRefNum = message.getString(recipientRefNumColId);
		if((recipientIdent.equalsIgnoreCase("") || (recipientRefNum.equalsIgnoreCase("")))){
			hideTextView(recipientIdentTV);
			hideTextView(recipientRefNumTV);
			hideTextView(recipientDetailTV);
		} else {
			if(recipientIdent.equalsIgnoreCase(""))
				hideTextView(recipientIdentTV);
			else
				recipientIdentTV.setText(getString(R.string.doc_ident, recipientIdent));
			if(recipientRefNum.equalsIgnoreCase(""))
				hideTextView(recipientRefNumTV);
			else
				recipientRefNumTV.setText(getString(R.string.doc_ref_num, recipientRefNum));
		}
		
		String senderIdent = message.getString(senderIdentColId);
		String senderRefNum = message.getString(senderRefNumColId);
		if((senderIdent.equalsIgnoreCase("") || (senderRefNum.equalsIgnoreCase("")))){
			hideTextView(senderIdentTV);
			hideTextView(senderRefNumTV);
			hideTextView(senderDetailTV);
		} else {
			if(senderIdent.equalsIgnoreCase(""))
				hideTextView(senderIdentTV);
			else
				senderIdentTV.setText(getString(R.string.doc_ident, senderIdent));
			if(senderRefNum.equalsIgnoreCase(""))
				hideTextView(senderRefNumTV);
			else
				senderRefNumTV.setText(getString(R.string.doc_ref_num, senderRefNum));
		}
		message.close();
		return v;

	}

	private String createLegalTitle(String legalTitleLaw, String legalTitlePar, 
			String legalTitlePoint, String legalTitleSect, String legalTitleYear) {
		String legalTitle = "";
		
		if((legalTitleLaw != null) && (!legalTitleLaw.equalsIgnoreCase(""))){
			legalTitle += (legalTitleLaw + ", ");
		}
		if((legalTitleYear != null) && (!legalTitleYear.equalsIgnoreCase(""))){
			legalTitle += (legalTitleYear + ", ");
		}
		if((legalTitleSect != null) && (!legalTitleSect.equalsIgnoreCase(""))){
			legalTitle += (legalTitleSect + ", ");
		}
		if((legalTitlePar != null) && (!legalTitlePar.equalsIgnoreCase(""))){
			legalTitle += (legalTitlePar + ", ");
		}
		if((legalTitlePoint != null) && (!legalTitlePoint.equalsIgnoreCase(""))){
			legalTitle += legalTitlePoint;
		}
		
		return legalTitle;
	}
	
	private Cursor getMessageCursor() {
		long id = getArguments().getLong(ID, 0);
		int folder = getArguments().getInt(FOLDER, 0);

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
			value.put(DatabaseHelper.RECEIVED_MESSAGE_STATUS_CHANGED, NOT_CHANGED);
		} else { // if(folder == OUTBOX)
			singleUri = ContentUris.withAppendedId(SentMessagesContentProvider.CONTENT_URI, id);
			value.put(DatabaseHelper.SENT_MESSAGE_IS_READ, IS_READ);
			value.put(DatabaseHelper.SENT_MESSAGE_STATUS_CHANGED, NOT_CHANGED);
		}

		getActivity().getContentResolver().update(singleUri, value, null, null);
	}
	
	private class Updater extends ContentObserver {

		public Updater(Handler handler) {
			super(handler);
			
		}
		
		@Override
	    public boolean deliverSelfNotifications() {
	        return false;
	    }

	    @Override
	    public void onChange(boolean selfChange) {
	    	fillFragment(getView(), null);
	    }
	}
	
	private String translateIntAnswerToString(String param){
		if((param == null) || param.equalsIgnoreCase(""))
			return getString(R.string.no);
		else if(Integer.parseInt(param) == 1)
			return getString(R.string.yes);
		else if (Integer.parseInt(param) == 0)
			return getString(R.string.no);
		
		return getString(R.string.no);
	}
	
	private void hideTextView(TextView param) {
		param.setHeight(0);
		param.setVisibility(View.INVISIBLE);
	}
}
