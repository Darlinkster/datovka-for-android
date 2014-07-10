/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2014  CZ NIC z.s.p.o. <podpora at nic dot cz>

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
package cz.nic.datovka.tinyDB;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.res.Resources.NotFoundException;
import android.util.Log;
import cz.nic.datovka.R.raw;
import cz.nic.datovka.activities.AppUtils;

public class MyAndroidTrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		
		TrustManager[] tms = getTrustManagersFromBKS();
		Log.i("Datovka", "Number of trust managers: " + tms.length);
		
		if(tms.length < 1)
			throw new CertificateException("Empty BKS");
		
		try {
			for (TrustManager tm : getTrustManagersFromBKS()) {
				((X509TrustManager) tm).checkServerTrusted(certs, authType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CertificateException(e.getMessage());
		}

		Log.i("Datovka", "Certificate verified");
	}
	
	private TrustManager[] getTrustManagersFromBKS() {

		try {
			KeyStore keyStore = KeyStore.getInstance("BKS");
			keyStore.load(AppUtils.ctx.getResources().openRawResource(raw.mytruststore), "secret".toCharArray());
			return getTrustManagers(keyStore);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new TrustManager[0];
	}
	
	private TrustManager[] getTrustManagers(KeyStore keystore) {
		try {
			TrustManagerFactory tmf =
					TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) keystore);
			return tmf.getTrustManagers();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		
		return new TrustManager[0];
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}