package com.ninja.examples.utility.net;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

public class APIRequest {

	private static final int WS_TIMEOUT = 15000;
	public static DefaultHttpClient httpclient;


	public static DefaultHttpClient getClient() {
		if (httpclient == null) {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setUseExpectContinue(params, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, "utf-8");

			params.setBooleanParameter("http.protocol.expect-continue", false);

			HttpConnectionParams.setConnectionTimeout(params, WS_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, WS_TIMEOUT);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));

			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
					params, registry);

			httpclient = new DefaultHttpClient(manager, params);

			httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
				public long getKeepAliveDuration(HttpResponse response,
						HttpContext context) {
					return 50;
				}
			});

		}
		return httpclient;
	}

}
