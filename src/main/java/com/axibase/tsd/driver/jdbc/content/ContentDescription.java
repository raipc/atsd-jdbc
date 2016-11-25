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
package com.axibase.tsd.driver.jdbc.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.axibase.tsd.driver.jdbc.enums.MetadataFormat;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import org.apache.commons.lang3.StringUtils;

import static com.axibase.tsd.driver.jdbc.DriverConstants.*;

public class ContentDescription {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ContentDescription.class);

	private String host;
	private String query;
	private String login;
	private Map<String, String> paramsMap;
	private String password;
	private long contentLength;
	private String[] headers;
	private String jsonScheme;
	private final String metadataFormat;
	private long maxRowsCount;
	private final String queryId;
	private final boolean supportsCancel;

	public ContentDescription(String host, String query, String login, String password, String[] params) {
		this(host, query, login, password, 0, "", params);
	}

	public ContentDescription(String host, String query, String login, String password, StatementContext context, String[] params) {
		this(host, query, login, password, context.getVersion(), context.getQueryId(), params);
	}

	private ContentDescription(String host, String query, String login, String password, int atsdVersion,
	                           String queryId, String[] params) {
		this.host = host;
		this.query = query;
		this.login = login;
		this.password = password;
		this.metadataFormat = atsdVersion < ATSD_VERSION_SUPPORTING_BODY_METADATA ?
				MetadataFormat.HEADER.name() : MetadataFormat.EMBED.name();
		final int size = params == null ? 0 : params.length;
		this.paramsMap = new HashMap<>(size);
		this.queryId = queryId;
		this.supportsCancel = atsdVersion >= ATSD_VERSION_SUPPORTS_CANCEL_QUERIES;
		if (size > 0) {
			for (String param : params) {
				int delimiterPosition = param.indexOf('=');
				if (delimiterPosition >= 0) {
					paramsMap.put(param.substring(0, delimiterPosition),
							      param.substring(delimiterPosition + 1));
				}
			}
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getEncodedQuery() {
		try {
			return URLEncoder.encode(query, DEFAULT_CHARSET.name());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			return query;
		}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJsonScheme() {
		return jsonScheme != null ? jsonScheme : "";
	}

	public void setJsonScheme(String jsonScheme) {
		this.jsonScheme = jsonScheme;
	}

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public void setMaxRowsCount(long maxRowsCount) {
		this.maxRowsCount = maxRowsCount;
	}

	public String getPostParams() {
		if (StringUtils.isEmpty(query)) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		if (supportsCancel) {
			buffer.append(QUERY_ID_PARAM_NAME).append('=').append(queryId).append('&');
		}
		return buffer
				.append(Q_PARAM_NAME).append('=').append(getEncodedQuery()).append('&')
				.append(FORMAT_PARAM_NAME).append('=').append(FORMAT_PARAM_VALUE).append('&')
				.append(METADATA_FORMAT_PARAM_NAME).append('=').append(metadataFormat).append('&')
				.append(LIMIT_PARAM_NAME).append('=').append(maxRowsCount)
				.toString();
	}

	public String getCancelQueryUrl() {
		return host + CANCEL_METHOD + '?' + QUERY_ID_PARAM_NAME + '=' + queryId;
	}

	public String getMetadataFormat() {
		return metadataFormat;
	}

	public Map<String, String> getQueryParamsAsMap() {
		if (StringUtils.isEmpty(query)) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<>();
		map.put(Q_PARAM_NAME, query);
		map.put(FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
		map.put(METADATA_FORMAT_PARAM_NAME, metadataFormat);
		map.put(LIMIT_PARAM_NAME, Long.toString(maxRowsCount));
		return map;
	}

	public boolean isSsl() {
		return StringUtils.startsWithIgnoreCase(host, "https://");
	}

	public boolean isTrusted() {
		final String trustedAsString = paramsMap.get(TRUST_PARAM_NAME);
		return trustedAsString == null ? DEFAULT_TRUST_SERVER_CERTIFICATE : Boolean.valueOf(trustedAsString);
	}

	public int getConnectTimeout() {
		final String timeoutAsString = paramsMap.get(CONNECT_TIMEOUT_PARAM);
		return timeoutAsString == null ? DEFAULT_CONNECT_TIMEOUT_VALUE : Integer.parseInt(timeoutAsString);
	}

	public int getReadTimeout() {
		final String timeoutAsString = paramsMap.get(READ_TIMEOUT_PARAM);
		return timeoutAsString == null ? DEFAULT_READ_TIMEOUT_VALUE : Integer.parseInt(timeoutAsString);
	}

	public String getStrategyName() {
		final String strategy = paramsMap.get(STRATEGY_PARAM_NAME);
		return StringUtils.isNoneEmpty(strategy) ? strategy : null;
	}

	public String getQueryId() {
		return queryId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContentDescription other = (ContentDescription) obj;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ContentDescription [host=" + this.getHost() + ", params=" + this.getPostParams() + ", login=" + login
				+ ", headers=" + Arrays.toString(headers) + ", jsonScheme=" + jsonScheme + ", contentLength="
				+ contentLength + "]";
	}

}
