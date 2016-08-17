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

import java.io.*;
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

import com.axibase.tsd.driver.jdbc.DriverConstants;
import com.axibase.tsd.driver.jdbc.enums.MetadataFormat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import static com.axibase.tsd.driver.jdbc.DriverConstants.*;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class SdkProtocolImpl implements IContentProtocol {
	private static final LoggingFacade logger = LoggingFacade.getLogger(SdkProtocolImpl.class);
	private static final int UNSUCCESSFUL_SQL_RESULT_CODE = 400;
	private static final int MILLIS = 1000;
	private final ContentDescription contentDescription;
	private HttpURLConnection conn;

	public SdkProtocolImpl(final ContentDescription cd)
			throws IOException, KeyManagementException, MalformedURLException, NoSuchAlgorithmException {
		this.contentDescription = cd;
	}

	@Override
	public void getContentSchema() throws AtsdException, GeneralSecurityException, IOException {
		executeRequest(HEAD_METHOD, 0);
	}

	@Override
	public InputStream readInfo() throws AtsdException, GeneralSecurityException, IOException {
		return executeRequest(GET_METHOD, 0);
	}

	@Override
	public InputStream readContent(int timeout) throws AtsdException, GeneralSecurityException, IOException {
		InputStream inputStream = executeRequest(POST_METHOD, timeout);
		if (MetadataFormat.EMBED.name().equals(DriverConstants.METADATA_FORMAT_PARAM_VALUE)
				&& StringUtils.isEmpty(contentDescription.getJsonScheme())) {
			inputStream = retrieveJsonSchemeAndSubstituteStream(inputStream);
		}
		return inputStream;
	}

	@Override
	public InputStream readContent() throws AtsdException, GeneralSecurityException, IOException {
		return readContent(0);
	}

	@Override
	public void close() throws Exception {
		if (this.conn != null) {
			this.conn.disconnect();
		}
	}

	private InputStream executeRequest(String method, int queryTimeout) throws AtsdException, IOException, GeneralSecurityException {
		boolean isHead = method.equals(HEAD_METHOD);
		boolean isPost = method.equals(POST_METHOD);
		String postParams = contentDescription.getPostParams();
		String url = contentDescription.getHost() + (isPost || StringUtils.isBlank(postParams) ? "" : '?' + postParams);
		if (logger.isDebugEnabled()) {
			logger.debug("[request] {} {}", method, url);
		}
		this.conn = getHttpURLConnection(url);
		if (contentDescription.isSsl()) {
			doTrustToCertificates((HttpsURLConnection) this.conn);
		}
		setBaseProperties(method, queryTimeout);
		if (MetadataFormat.HEADER.name().equals(DriverConstants.METADATA_FORMAT_PARAM_VALUE)
				&& StringUtils.isEmpty(contentDescription.getJsonScheme())) {
			retrieveJsonSchemeFromHeader(conn.getHeaderFields());
		}
		long contentLength = conn.getContentLengthLong();
		if (logger.isDebugEnabled()) {
			logger.debug("[response] " + contentLength);
		}
		contentDescription.setContentLength(contentLength);

		if (isHead) {
			return null;
		}

		final boolean gzipped = COMPRESSION_ENCODING.equals(conn.getContentEncoding());
		final int code = conn.getResponseCode();
		InputStream body;
		if (code != HttpsURLConnection.HTTP_OK) {
			if (logger.isDebugEnabled()) {
				logger.debug("Response code: " + code);
			}
			if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new AtsdException("Wrong credentials provided");
			}
			if (code == UNSUCCESSFUL_SQL_RESULT_CODE) {
				body = conn.getErrorStream();
			} else {
				throw new AtsdException("HTTP code " + code);
			}
		} else {
			body = conn.getInputStream();
		}
		return gzipped ? new GZIPInputStream(body) : body;
	}

	private void setBaseProperties(String method, int queryTimeout) throws IOException {
		boolean isHead = method.equals(HEAD_METHOD);
		boolean isPost = method.equals(POST_METHOD);
		String postParams = contentDescription.getPostParams();
		String login = contentDescription.getLogin();
		String password = contentDescription.getPassword();
		if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(password)) {
			final String basicCreds = login + ':' + password;
			final byte[] encoded = Base64.encodeBase64(basicCreds.getBytes());
			final String authHeader = AUTHORIZATION_TYPE + new String(encoded);
			conn.setRequestProperty(AUTHORIZATION_HEADER, authHeader);
		}
		conn.setAllowUserInteraction(false);
		conn.setChunkedStreamingMode(100);
		conn.setConnectTimeout(contentDescription.getConnectTimeout() * MILLIS);
		conn.setDoInput(true);
		conn.setDoOutput(!isHead);
		conn.setInstanceFollowRedirects(true);
		int timeoutInSeconds = queryTimeout == 0 ? contentDescription.getReadTimeout() : queryTimeout;
		conn.setReadTimeout(timeoutInSeconds * MILLIS);
		conn.setRequestMethod(method);
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
			try (OutputStream os = conn.getOutputStream();
				 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset().name()))) {
				writer.write(postParams);
				writer.flush();
			}
		}
	}

	public HttpURLConnection getHttpURLConnection(String uri) throws IOException {
		final URL url = new URL(uri);
		return (HttpURLConnection) url.openConnection();
	}

	public void doTrustToCertificates(final HttpsURLConnection sslConnection) {
		final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
			}
			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
			}
		}};
		final SSLContext sc;
		try {
			sc = SSLContext.getInstance(CONTEXT_INSTANCE_TYPE);
		} catch (NoSuchAlgorithmException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			return;
		}
		final Boolean trusted = contentDescription.isTrusted();
		if (logger.isDebugEnabled()) {
			logger.debug("[doTrustToCertificates] " + trusted);
		}
		try {
			sc.init(null, trusted != null && trusted ? trustAllCerts : null, new SecureRandom());
		} catch (KeyManagementException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			return;
		}
		sslConnection.setSSLSocketFactory(sc.getSocketFactory());
		final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String urlHostName, SSLSession session) {
				if (!urlHostName.equalsIgnoreCase(session.getPeerHost()) && logger.isDebugEnabled()) {
					logger.debug("[doTrustToCertificates] URL host {} is different to SSLSession host {}", urlHostName,
							session.getPeerHost());
				}
				return true;
			}

		};
		if (trusted != null && trusted) {
			sslConnection.setHostnameVerifier(hostnameVerifier);
		}
	}

	private void retrieveJsonSchemeFromHeader(Map<String, List<String>> map) throws UnsupportedEncodingException {
		printHeaders(map);
		List<String> list = map.get(SCHEME_HEADER);
		String value = list != null && !list.isEmpty() ? list.get(0) : null;
		if (value != null) {
			assert value.startsWith(START_LINK) && value.endsWith(END_LINK);
			final String enc = value.substring(START_LINK.length(), value.length() - END_LINK.length());
			String json = new String(Base64.decodeBase64(enc), Charset.defaultCharset());
			if (logger.isTraceEnabled()) {
				logger.trace("JSON schema: " + json);
			}
			contentDescription.setJsonScheme(json);
		}
	}

	private InputStream retrieveJsonSchemeAndSubstituteStream(InputStream inputStream) {
		try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			assert inputStream.read() == (int)'#';
			byte[] buffer = new byte[1024];
			int length;
			boolean headerNotProcessed = true;
			byte lineFeed = (byte)'\n';
			while ((length = inputStream.read(buffer)) != -1) {
				if (headerNotProcessed) {
					for (int i = 0; i < length; ++i) {
						if (buffer[i] == lineFeed) {
							result.write(buffer, 0, i);
							final byte[] decoded = Base64.decodeBase64(result.toByteArray());
							final String jsonScheme = new String(decoded, Charset.defaultCharset());
							contentDescription.setJsonScheme(jsonScheme);
							if (logger.isTraceEnabled()) {
								logger.trace("JSON schema: " + jsonScheme);
							}
							result.reset();
							final int newSize = length - i - 1;
							if (newSize > 0) {
								result.write(buffer, i + 1, newSize);
							}
							headerNotProcessed = false;
							break;
						}
					}
					if (headerNotProcessed) {
						result.write(buffer, 0, length);
					}
				} else {
					result.write(buffer, 0, length);
				}
			}
			inputStream.close();
			return new ByteArrayInputStream(result.toByteArray());
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Error while processing response body", e);
			}
			return inputStream;
		}
	}


	private void printHeaders(Map<String, List<String>> map) {
		if (logger.isTraceEnabled()) {
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				logger.trace("Key: {} Value: {} ", entry.getKey(), entry.getValue());
			}
		}
	}

}