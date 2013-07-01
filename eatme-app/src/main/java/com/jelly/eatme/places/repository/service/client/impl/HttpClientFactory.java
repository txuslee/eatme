package com.jelly.eatme.places.repository.service.client.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

public class HttpClientFactory {

    private static HttpClient client;

    public synchronized static HttpClient getThreadSafeClient(final HttpParams params) {
        if (client != null) {
            return client;
        }

        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        final ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(manager, params);
        return client;
    }

    public synchronized static void shutdown() {
        if (client != null) {
            if (client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
            client = null;
        }
    }

}