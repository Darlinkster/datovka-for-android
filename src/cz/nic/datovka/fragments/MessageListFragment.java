package cz.nic.datovka.fragments;

import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.R;
import cz.nic.datovka.R.id;
import cz.nic.datovka.R.layout;
import cz.nic.datovka.connector.Connector;

public class MessageListFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//List<MessageEnvelope> messageList = Connector.getMessageList();
		//setListAdapter(new MessageListAdapter(messageList));
		
	}

	private class MessageListAdapter extends BaseAdapter {
		private List<MessageEnvelope> messageList;

		public MessageListAdapter(List<MessageEnvelope> messageList) {
			this.messageList = messageList;
		}

		public int getCount() {
			return messageList.size();
		}

		public MessageEnvelope getItem(int position) {
			return messageList.get(position);
		}

		public long getItemId(int position) {
			return Long.parseLong(messageList.get(position).getMessageID());
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View result;
			
			if (convertView == null) {
				result = getActivity().getLayoutInflater().inflate(
						R.layout.message_list_fragment, parent, false);
			} else {
				result = convertView;
			}

			String annotationText = getItem(position).getAnnotation();
			String senderText = getItem(position).getSender().getIdentity();
			String date = Integer.toString(getItem(position).getDeliveryTime().get(GregorianCalendar.DAY_OF_MONTH))
					+ ". "
					+ Integer.toString(getItem(position).getDeliveryTime().get(GregorianCalendar.MONTH))
					+ ". "
					+ Integer.toString(getItem(position).getDeliveryTime().get(GregorianCalendar.YEAR));

			TextView annotationView = (TextView) result
					.findViewById(R.id.message_item_annotation);
			TextView senderView = (TextView) result
					.findViewById(R.id.message_item_sender);
			TextView dateView = (TextView) result
					.findViewById(R.id.message_item_date);

			annotationView.setText(annotationText);
			senderView.setText(senderText);
			dateView.setText(date);

			result.setId(Integer.parseInt(messageList.get(position).getMessageID()));
			return result;

		}

	}
}
