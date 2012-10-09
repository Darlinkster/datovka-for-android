package cz.nic.datovka;


import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import cz.abclinuxu.datoveschranky.common.entities.MessageEnvelope;
import cz.nic.datovka.connector.Connector;

public class MessageListFragment extends ListFragment {

		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Context ctx = getActivity();
		
		Connector connector = new Connector("co55on", "Fx2MAt3u8wDRL5", ctx);
		try {
			connector.connectToTesting();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		List<MessageEnvelope> messageList = connector.getMessageList();
		String[] names = new String[messageList.size()];
		for(int i = 0; i < messageList.size(); i++){
			names[i] = messageList.get(i).getAnnotation();
		}
		
		
		
		
		ListAdapter aa = new ArrayAdapter<String>(ctx, R.layout.message_list_fragment, names);
		setListAdapter(aa);
	}

	
}
