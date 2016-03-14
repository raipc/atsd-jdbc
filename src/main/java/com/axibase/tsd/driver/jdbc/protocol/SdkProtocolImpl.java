/*
* Copyright 2016 Axibase Corporation or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
* https://www.axibase.com/atsd/axibase-apache-2.0.pdf
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
package com.axibase.tsd.driver.jdbc.protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class SdkProtocolImpl implements IContentProtocol {
	private static final LoggingFacade logger = LoggingFacade.getLogger(SdkProtocolImpl.class);
	private final ContentDescription cd;
	private HttpURLConnection conn;

	public SdkProtocolImpl(final ContentDescription cd)
			throws IOException, KeyManagementException, MalformedURLException, NoSuchAlgorithmException {
		this.cd = cd;
	}

	@Override
	public void getContentSchema() throws AtsdException, GeneralSecurityException, IOException {
		executeRequest(HEAD_METHOD);
	}

	@Override
	public InputStream readInfo() throws AtsdException, GeneralSecurityException, IOException {
		return executeRequest(GET_METHOD);
	}

	@Override
	public InputStream readContent() throws AtsdException, GeneralSecurityException, IOException {
		return executeRequest(POST_METHOD);
	}

	@Override
	public void close() throws Exception {
		if (this.conn != null)
			this.conn.disconnect();
	}

	synchronized InputStream executeRequest(String method) throws AtsdException, IOException, GeneralSecurityException {
		boolean isHead = method.equals(HEAD_METHOD);
		boolean isPost = method.equals(POST_METHOD);
		String postParams = cd.getPostParams();
		String url = cd.getHost() + (isPost || StringUtils.isBlank(postParams) ? "" : '?' + postParams);
		if (logger.isDebugEnabled()) {
			logger.debug("[request] {} {}", method, url);
		}
		this.conn = getHttpURLConnection(url);
		if (cd.isSsl())
			doTrustToCertificates((HttpsURLConnection)this.conn);
		final String basicCreds = new StringBuilder(cd.getLogin()).append(':').append(cd.getPassword()).toString();
		final byte[] encoded = Base64.encodeBase64(basicCreds.getBytes());
		final String authHeader = AUTHORIZATION_TYPE + new String(encoded);
		conn.setAllowUserInteraction(false);
		conn.setChunkedStreamingMode(100);
		conn.setConnectTimeout(0);
		conn.setDoInput(true);
		conn.setDoOutput(isHead ? false : true);
		conn.setInstanceFollowRedirects(true);
		conn.setReadTimeout(0);
		conn.setRequestMethod(method);
		conn.setRequestProperty(AUTHORIZATION_HEADER, authHeader);
		conn.setRequestProperty(ACCEPT_ENCODING, isPost ? COMPRESSION_ENCODING : DEFAULT_ENCODING);
		conn.setRequestProperty(CONNECTION_HEADER, KEEP_ALIVE);
		conn.setRequestProperty(CONTENT_TYPE, FORM_URLENCODED_TYPE);
		conn.setRequestProperty(USER_AGENT, USER_AGENT_HEADER);
		conn.setUseCaches(false);
		if (isPost) {
			conn.setRequestProperty(ACCEPT_HEADER, CSV_MIME_TYPE);
			conn.setRequestProperty(CONTENT_LENGTH, Integer.toString(postParams.length()));
			if (logger.isDebugEnabled()) {
				logger.debug("[params] " + postParams);
			}
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset().name()));
			writer.write(postParams);
			writer.flush();
			writer.close();
			os.close();
		}
		if (StringUtils.isEmpty(cd.getJsonScheme()))
			processResponse(conn.getHeaderFields());
		long cl = conn.getContentLengthLong();
		if (logger.isDebugEnabled()) {
			logger.debug("[response] " + cl);
		}
		cd.setContentLength(cl);
		if (isHead)
			return null;
		int code = conn.getResponseCode();
		if (code != HttpsURLConnection.HTTP_OK) {
			if (logger.isDebugEnabled())
				logger.debug("Response code: " + code);
			throw new AtsdException("HTTP code " + code);
		}
		boolean gzipped = COMPRESSION_ENCODING.equals(conn.getContentEncoding());
		final InputStream is = conn.getInputStream();
		return (gzipped ? (InputStream) new GZIPInputStream(is) : is);
	}

	
	public HttpURLConnection getHttpURLConnection(String uri) throws IOException {
		final URL url = new URL(uri);
		return (HttpURLConnection) url.openConnection();
	}
	 
	public void doTrustToCertificates(final HttpsURLConnection sslConnection) {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				return;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				return;
			}
		} };
		final SSLContext sc;
		try {
			sc = SSLContext.getInstance(CONTEXT_INSTANCE_TYPE);
		} catch (NoSuchAlgorithmException e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage());
			return;
		}
		final Boolean trusted = cd.isTrusted();
		if (logger.isDebugEnabled())
			logger.debug("[doTrustToCertificates] " + trusted);
		try {
			sc.init(null, trusted != null && trusted ? trustAllCerts : null, new SecureRandom());
		} catch (KeyManagementException e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage());
			return;
		}
		sslConnection.setSSLSocketFactory(sc.getSocketFactory());
//		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
					if (logger.isDebugEnabled())
						logger.debug("[doTrustToCertificates] URL host {} is different to SSLSession host {}",
								urlHostName, session.getPeerHost());
				}
				return true;
			}

		};
		if (trusted != null && trusted)
			sslConnection.setHostnameVerifier(hostnameVerifier);
//		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
	}

	private void processResponse(Map<String, List<String>> map) throws UnsupportedEncodingException {
		printHeaders(map);
		List<String> list = map.get(SCHEME_HEADER);
		String value = list != null && list.size() != 0 ? list.get(0) : null;
		if (value == null)
			return;
		assert value.startsWith(START_LINK) && value.endsWith(END_LINK);
		final String enc = value.substring(START_LINK.length(), value.length() - END_LINK.length());
		String json = new String(Base64.decodeBase64(enc), Charset.defaultCharset());
		if (logger.isTraceEnabled())
			logger.trace("JSON schema: " + json);
		cd.setJsonScheme(json);
	}

	private void printHeaders(Map<String, List<String>> map) {
		if (!logger.isTraceEnabled())
			return;
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			logger.trace("Key: {} Value: {} ", entry.getKey(), entry.getValue());
		}
	}

	static final String ACCEPT_HEADER = "Accept";
	static final String AUTHORIZATION_HEADER = "Authorization";
	static final String AUTHORIZATION_TYPE = "Basic ";
	static final String ACCEPT_ENCODING = "Accept-Encoding";
	static final String CONNECTION_HEADER = "Connection";
	static final String CONTENT_TYPE = "Content-Type";
	static final String CONTENT_LENGTH = "Content-Length";
	static final String CONTEXT_INSTANCE_TYPE = "SSL";
	static final String CSV_MIME_TYPE = "text/csv";
	static final String COMPRESSION_ENCODING = "gzip";
	static final String DEFAULT_ENCODING = "identity";
	static final String END_LINK = ">; rel=\"describedBy\"; type=\"application/csvm+json\"";
	static final String GET_METHOD = "GET";
	static final String HEAD_METHOD = "HEAD";
	static final String POST_METHOD = "POST";
	static final String KEEP_ALIVE = "Keep-Alive";
	static final String FORM_URLENCODED_TYPE = "application/x-www-form-urlencoded";
	static final String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data;boundary=---------------------------1717271041";
	static final String SCHEME_HEADER = "Link";
	static final String START_LINK = "<data:application/csvm+json;base64,";
	static final String USER_AGENT = "User-Agent";
	static final String USER_AGENT_HEADER = "ATSD Client/1.0 axibase.com";
}