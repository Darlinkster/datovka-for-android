package cz.nic.datovka.fragments;

import java.util.GregorianCalendar;

import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.R;
import cz.nic.datovka.R.id;
import cz.nic.datovka.R.layout;
import cz.nic.datovka.connector.Connector;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageDetailFragment extends Fragment {
	public static final String ID = "id";

	public static MessageDetailFragment newInstance(int id) {
		MessageDetailFragment f = new MessageDetailFragment();
		Bundle args = new Bundle();
		args.putInt(ID, id);
		
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.message_detail_fragment, container, false);
		int id = getArguments().getInt(ID, 0);


		TextView annotation = (TextView) v.findViewById(R.id.message_annotation);
		TextView messageId = (TextView) v.findViewById(R.id.message_id);
		TextView date = (TextView) v.findViewById(R.id.message_date);
		TextView sender = (TextView) v.findViewById(R.id.message_sender);
		TextView senderAddress = (TextView) v.findViewById(R.id.message_sender_address);
		TextView messageType = (TextView) v.findViewById(R.id.message_type);
		/*
		MessageEnvelope message = Connector.getMessageById(id);
		
		annotation.setText(message.getAnnotation());
		messageId.setText(message.getMessageID());
		date.setText(message.getDeliveryTime().get(GregorianCalendar.DAY_OF_MONTH) + ". " +
				message.getDeliveryTime().get(GregorianCalendar.MONTH) + ". " +
				message.getDeliveryTime().get(GregorianCalendar.YEAR));
		sender.setText(message.getSender().getIdentity());
		senderAddress.setText(message.getSender().getAddress());
		messageType.setText(message.getType().toString());
*/
		return v;
		
	}
}
