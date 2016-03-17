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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.axibase.tsd.driver.jdbc.DriverConstants;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class ContentDescription implements DriverConstants {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ContentDescription.class);

	private String host;
	private String query;
	private String login;
	private String[] params;
	private String password;
	private long contentLength;
	private String[] headers;
	private String jsonScheme;
	private String url;

	public ContentDescription(String host, String query, String login, String password, String[] params) {
		this.host = host;
		this.query = query;
		this.login = login;
		this.password = password;
		this.params = params;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
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

	public String getPostParams() {
		if (StringUtils.isEmpty(query)) {
			return "";
		}
		String q;
		try {
			q = URLEncoder.encode(query, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			q = query;
		}
		return String.format(QUERY_PARAM_NAME, Q_PARAM_NAME, q, FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
	}

	public Map<String, String> getPostParamsAsMap() {
		if (StringUtils.isEmpty(query)) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<>();
		map.put(Q_PARAM_NAME, query);
		map.put(FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
		return map;
	}

	public boolean isSsl() {
		return host.toLowerCase().startsWith("https://");
	}

	public Boolean isTrusted() {
		if (params == null || params.length == 0)
			return null;
		for (String param : params) {
			if (TRUST_PARAM_TRUE.equalsIgnoreCase(param))
				return true;
			if (TRUST_PARAM_FALSE.equalsIgnoreCase(param))
				return false;
		}
		return null;
	}

	public String getStrategyName() {
		if (params == null || params.length == 0)
			return null;
		for (final String param : params) {
			if (!param.toLowerCase().startsWith(STRATEGY_PARAM_NAME))
				continue;
			final String value = param.substring(STRATEGY_PARAM_NAME.length());
			return StringUtils.isNoneEmpty(value) ? value : null;
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
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
