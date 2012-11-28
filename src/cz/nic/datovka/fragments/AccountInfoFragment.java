package cz.nic.datovka.fragments;

import android.content.ContentUris;
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
import cz.nic.datovka.contentProviders.MsgBoxContentProvider;
import cz.nic.datovka.tinyDB.AndroidUtils;

public class AccountInfoFragment extends SherlockFragment {
	public final static String MSGBOX_ID = "msgid";

	public static AccountInfoFragment newInstance(Long accountId) {
		AccountInfoFragment aif = new AccountInfoFragment();
		Bundle args = new Bundle();
		args.putLong(MSGBOX_ID, accountId);

		aif.setArguments(args);
		return aif;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View v = inflater.inflate(R.layout.account_info_fragment, container, false);
		long msgBoxId = getArguments().getLong(MSGBOX_ID);

		TextView usernameTv = (TextView) v.findViewById(R.id.account_info_username);
		TextView msgBoxIdTv = (TextView) v.findViewById(R.id.account_info_databox_id);
		TextView msgBoxTypeTv = (TextView) v.findViewById(R.id.account_info_databox_type);
		TextView ownerNameTv = (TextView) v.findViewById(R.id.account_info_owner_name);
		TextView ownerAddressCityTv = (TextView) v.findViewById(R.id.account_info_owner_address_city);
		TextView ownerAddressStreetTv = (TextView) v.findViewById(R.id.account_info_owner_address_street);
		TextView passwordExpirationTv = (TextView) v.findViewById(R.id.account_info_password_expiration);
		TextView ownerAddressStateTv = (TextView) v.findViewById(R.id.account_info_owner_address_state);

		Uri uri = ContentUris.withAppendedId(MsgBoxContentProvider.CONTENT_URI, msgBoxId);
		Cursor msgBoxCursor = getActivity().getContentResolver().query(uri, DatabaseHelper.msgbox_columns, null, null, null);
		if (msgBoxCursor.moveToFirst()) {

			int usernameColId = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_LOGIN);
			int msgBoxIdColId = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_ISDS_ID);
			int msgBoxTypeColId = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_TYPE);
			int ownerNameId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_NAME);
			int ownerAddressCityId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_ADDRESS_CITY);
			int ownerAddressStateId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_ADDRESS_STATE);
			int ownerAddressStreetId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_ADDRESS_STREET);
			int ownerAddressStreetNumId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_ADDRESS_MUNIC_NUMBER);
			int ownerAddressZipId = msgBoxCursor.getColumnIndex(DatabaseHelper.OWNER_ADDRESS_ZIP);
			int passwordExpirationId = msgBoxCursor.getColumnIndex(DatabaseHelper.MSGBOX_PASSWD_EXPIRATION);

			usernameTv.setText(getString(R.string.username) + ": " + msgBoxCursor.getString(usernameColId));
			msgBoxIdTv.setText(getString(R.string.ID, msgBoxCursor.getString(msgBoxIdColId)));

			long expirationDate = Long.parseLong(msgBoxCursor.getString(passwordExpirationId));
			if (expirationDate < 0)
				passwordExpirationTv.setText(getString(R.string.passwd_expir_date) + ": " + getString(R.string.not_set));
			else
				passwordExpirationTv.setText(getString(R.string.passwd_expir_date) + ": "
						+ AndroidUtils.FromEpochTimeToHumanReadableDateWithTime(expirationDate));

			msgBoxTypeTv.setText(getString(R.string.msgbox_type) + ": " + msgBoxCursor.getString(msgBoxTypeColId));

			ownerNameTv.setText(msgBoxCursor.getString(ownerNameId));
			ownerAddressStreetTv.setText(msgBoxCursor.getString(ownerAddressStreetId) + " " + msgBoxCursor.getString(ownerAddressStreetNumId));
			ownerAddressCityTv.setText(msgBoxCursor.getString(ownerAddressCityId) + " " + msgBoxCursor.getString(ownerAddressZipId));
			ownerAddressStateTv.setText(msgBoxCursor.getString(ownerAddressStateId));

		}
		msgBoxCursor.close();
		return v;
	}

}
