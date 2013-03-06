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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import cz.nic.datovka.R;
import cz.nic.datovka.activities.AppUtils;
import cz.nic.datovka.connector.DatabaseHelper;
import cz.nic.datovka.contentProviders.MessagesContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class MessageDetailFragment extends SherlockFragment {
	public static final String ID = "id";
	private static final int NOT_CHANGED = 0;
	private static final int IS_READ = 1;
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private Updater updater;
	private Uri singleUri;

	public static MessageDetailFragment newInstance(long id) {
		MessageDetailFragment f = new MessageDetailFragment();
		Bundle args = new Bundle();
		args.putLong(ID, id);

		f.setArguments(args);
		return f;
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(updater);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getContentResolver().registerContentObserver(singleUri, false, updater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.message_detail_fragment, container, false);
		View customActionBarView = inflater.inflate(R.layout.message_detail_actionbar, null);
		updater = new Updater(new Handler());
		setMessageRead();

		return fillFragment(v, customActionBarView);

	}

	private View fillFragment(View v, View customActionBarView) {
		Cursor message = getMessageCursor();
		
		if(message == null){
			// message with given ID was not found, show error msg and close the activity
			Toast.makeText(AppUtils.ctx, R.string.message_not_found, Toast.LENGTH_LONG).show();
			getActivity().finish();
		}

		// TextView annotation = (TextView)
		// v.findViewById(R.id.message_annotation);
		// TextView messageId = (TextView) v.findViewById(R.id.message_id);
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

		int annotationColId = message.getColumnIndex(DatabaseHelper.MESSAGE_ANNOTATION);
		int messageIdColId = message.getColumnIndex(DatabaseHelper.MESSAGE_ISDS_ID);
		int messageDeliveryDateColId = message.getColumnIndex(DatabaseHelper.MESSAGE_SENT_DATE);
		int messageAcceptanceDateColId = message.getColumnIndex(DatabaseHelper.MESSAGE_ACCEPTANCE_DATE);
		int senderRecipientColId = message.getColumnIndex(DatabaseHelper.MESSAGE_OTHERSIDE_NAME);
		int senderRecipientAddressColId = message.getColumnIndex(DatabaseHelper.MESSAGE_OTHERSIDE_ADDRESS);
		int messageStatusColId = message.getColumnIndex(DatabaseHelper.MESSAGE_STATE);
		int messageAttachmentSizeColId = message.getColumnIndex(DatabaseHelper.MESSAGE_ATTACHMENT_SIZE);
		int legalTitleLawColId = message.getColumnIndex(DatabaseHelper.MESSAGE_LEGALTITLE_LAW);
		int legalTitleParColId = message.getColumnIndex(DatabaseHelper.MESSAGE_LEGALTITLE_PAR);
		int legalTitlePointColId = message.getColumnIndex(DatabaseHelper.MESSAGE_LEGALTITLE_POINT);
		int legalTitleSectColId = message.getColumnIndex(DatabaseHelper.MESSAGE_LEGALTITLE_SECT);
		int legalTitleYearColId = message.getColumnIndex(DatabaseHelper.MESSAGE_LEGALTITLE_YEAR);
		int personalDeliveryColId = message.getColumnIndex(DatabaseHelper.MESSAGE_PERSONAL_DELIVERY);
		int substDeliveryColId = message.getColumnIndex(DatabaseHelper.MESSAGE_ALLOW_SUBST_DELIVERY);
		int toHandsColId = message.getColumnIndex(DatabaseHelper.MESSAGE_TO_HANDS);
		int recipientRefNumColId = message.getColumnIndex(DatabaseHelper.MESSAGE_RECIPIENT_REF_NUMBER);
		int recipientIdentColId = message.getColumnIndex(DatabaseHelper.MESSAGE_RECIPIENT_IDENT);
		int senderRefNumColId = message.getColumnIndex(DatabaseHelper.MESSAGE_SENDER_REF_NUMBER);
		int senderIdentColId = message.getColumnIndex(DatabaseHelper.MESSAGE_SENDER_IDENT);
		
		int folder = message.getInt(message.getColumnIndex(DatabaseHelper.MESSAGE_FOLDER));
		
		if (folder == AppUtils.INBOX) {
			senderRecpTV.setText(getString(R.string.sender));
		} else { // OUTBOX
			senderRecpTV.setText(getString(R.string.recipient));
		}

		if (customActionBarView != null) {
			ActionBar ab = getSherlockActivity().getSupportActionBar();
			TextView annotationAB = (TextView) customActionBarView.findViewById(R.id.actionbar_annotation);
			TextView idAB = (TextView) customActionBarView.findViewById(R.id.actionbar_id);

			annotationAB.setText(message.getString(annotationColId));
			idAB.setText(getString(R.string.ID, message.getString(messageIdColId)));
			ab.setDisplayShowTitleEnabled(false);
			ab.setCustomView(customActionBarView);
			ab.setDisplayShowCustomEnabled(true);
		}

		// getSherlockActivity().getSupportActionBar().setTitle(message.getString(annotationColId)
		// +" "+getString(R.string.ID, message.getString(messageIdColId)));
		// annotation.setText(message.getString(annotationColId));
		// messageId.setText(getString(R.string.ID,
		// message.getString(messageIdColId)));

		deliveryDateTV.setText(getString(R.string.delivery_date, AndroidUtils.FromXmlToHumanReadableDateWithTime(message.getString(messageDeliveryDateColId))));

		String acceptanceDate = message.getString(messageAcceptanceDateColId);
		if (acceptanceDate != null && !acceptanceDate.equals(""))
			acceptanceDateTV.setText(getString(R.string.acceptance_date, AndroidUtils.FromXmlToHumanReadableDateWithTime(acceptanceDate)));
		else
			acceptanceDateTV.setText(getString(R.string.acceptance_date, getString(R.string.message_not_accepted_yet)));

		senderTV.setText(message.getString(senderRecipientColId));
		SpannableString senderAddress = new SpannableString(message.getString(senderRecipientAddressColId));
		senderAddress.setSpan(new UnderlineSpan(), 0, senderAddress.length(), 0);
		senderAddressTV.setText(senderAddress);

		int status = message.getInt(messageStatusColId);
		switch (status) {
		case 1:
			messageStatusTV.setText(R.string.message_status_1);
			break;
		case 2:
			messageStatusTV.setText(R.string.message_status_2);
			break;
		case 3:
			messageStatusTV.setText(R.string.message_status_3);
			break;
		case 4:
			messageStatusTV.setText(R.string.message_status_4);
			break;
		case 5:
			messageStatusTV.setText(R.string.message_status_5);
			break;
		case 6:
			messageStatusTV.setText(R.string.message_status_6);
			break;
		case 7:
			messageStatusTV.setText(R.string.message_status_7);
			break;
		case 8:
			messageStatusTV.setText(R.string.message_status_8);
			break;
		case 9:
			messageStatusTV.setText(R.string.message_status_9);
			break;
		case 10:
			messageStatusTV.setText(R.string.message_status_10);
			break;
		default:
			messageStatusTV.setText(R.string.message_status_unknown);
			logger.log(Level.WARNING, "Unknown message status: " + status);

		}
		messageAttachmentSizeTV.setText(getString(R.string.size_of_attachments, message.getInt(messageAttachmentSizeColId)));
		personalDeliveryTV.setText(getString(R.string.personal_delivery, translateIntAnswerToString(message.getString(personalDeliveryColId))));
		substDeliveryTV.setText(getString(R.string.subst_delivery, translateIntAnswerToString(message.getString(substDeliveryColId))));

		String toHands = message.getString(toHandsColId);
		if (toHands.equalsIgnoreCase(""))
			hideTextView(toHandsTV);
		else
			toHandsTV.setText(getString(R.string.to_hands, toHands));

		String legalTitle = createLegalTitle(message.getString(legalTitleLawColId), message.getString(legalTitleParColId),
				message.getString(legalTitlePointColId), message.getString(legalTitleSectColId), message.getString(legalTitleYearColId));
		if (legalTitle.equalsIgnoreCase(""))
			hideTextView(legalTitleTV);
		else
			legalTitleTV.setText(getString(R.string.legal_title, legalTitle));

		String recipientIdent = message.getString(recipientIdentColId);
		String recipientRefNum = message.getString(recipientRefNumColId);
		if ((recipientIdent.equalsIgnoreCase("") || (recipientRefNum.equalsIgnoreCase("")))) {
			hideTextView(recipientIdentTV);
			hideTextView(recipientRefNumTV);
			hideTextView(recipientDetailTV);
		} else {
			if (recipientIdent.equalsIgnoreCase(""))
				hideTextView(recipientIdentTV);
			else
				recipientIdentTV.setText(getString(R.string.doc_ident, recipientIdent));
			if (recipientRefNum.equalsIgnoreCase(""))
				hideTextView(recipientRefNumTV);
			else
				recipientRefNumTV.setText(getString(R.string.doc_ref_num, recipientRefNum));
		}

		String senderIdent = message.getString(senderIdentColId);
		String senderRefNum = message.getString(senderRefNumColId);
		if ((senderIdent.equalsIgnoreCase("") || (senderRefNum.equalsIgnoreCase("")))) {
			hideTextView(senderIdentTV);
			hideTextView(senderRefNumTV);
			hideTextView(senderDetailTV);
		} else {
			if (senderIdent.equalsIgnoreCase(""))
				hideTextView(senderIdentTV);
			else
				senderIdentTV.setText(getString(R.string.doc_ident, senderIdent));
			if (senderRefNum.equalsIgnoreCase(""))
				hideTextView(senderRefNumTV);
			else
				senderRefNumTV.setText(getString(R.string.doc_ref_num, senderRefNum));
		}
		message.close();
		return v;

	}

	private String createLegalTitle(String legalTitleLaw, String legalTitlePar, String legalTitlePoint, String legalTitleSect, String legalTitleYear) {
		String legalTitle = "";

		if ((legalTitleLaw != null) && (!legalTitleLaw.equalsIgnoreCase(""))) {
			legalTitle += (legalTitleLaw + ", ");
		}
		if ((legalTitleYear != null) && (!legalTitleYear.equalsIgnoreCase(""))) {
			legalTitle += (legalTitleYear + ", ");
		}
		if ((legalTitleSect != null) && (!legalTitleSect.equalsIgnoreCase(""))) {
			legalTitle += (legalTitleSect + ", ");
		}
		if ((legalTitlePar != null) && (!legalTitlePar.equalsIgnoreCase(""))) {
			legalTitle += (legalTitlePar + ", ");
		}
		if ((legalTitlePoint != null) && (!legalTitlePoint.equalsIgnoreCase(""))) {
			legalTitle += legalTitlePoint;
		}

		return legalTitle;
	}

	private Cursor getMessageCursor() {
		long id = getArguments().getLong(ID, 0);

		singleUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, id);
		String[] projection = DatabaseHelper.message_columns;
		Cursor cursor = getActivity().getContentResolver().query(singleUri, projection, null, null, null);

		if (cursor.moveToFirst()) {
			return cursor;
		}
		return null;
	}

	private void setMessageRead() {
		long id = getArguments().getLong(ID, 0);

		Uri singleUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, id);
		ContentValues value = new ContentValues();
		value.put(DatabaseHelper.MESSAGE_IS_READ, IS_READ);
		value.put(DatabaseHelper.MESSAGE_STATUS_CHANGED, NOT_CHANGED);

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

	private String translateIntAnswerToString(String param) {
		if ((param == null) || param.equalsIgnoreCase(""))
			return getString(R.string.no);
		else if (Integer.parseInt(param) == 1)
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
